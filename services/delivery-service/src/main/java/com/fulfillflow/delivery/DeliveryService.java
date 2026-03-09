package com.fulfillflow.delivery;

import com.fulfillflow.common.error.NotFoundException;
import com.fulfillflow.common.events.EventTypes;
import com.fulfillflow.common.events.payloads.DeliveryCompletedPayload;
import com.fulfillflow.common.events.payloads.DeliveryFailedPayload;
import com.fulfillflow.common.events.payloads.DeliveryScheduledPayload;
import com.fulfillflow.common.outbox.OutboxHelper;
import com.fulfillflow.delivery.domain.Delivery;
import com.fulfillflow.delivery.domain.DeliveryRepository;
import com.fulfillflow.delivery.domain.DeliveryStatus;
import com.fulfillflow.delivery.model.DeliveryResponse;
import java.security.SecureRandom;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Application service for deliveries. Schedules a delivery when an order is
 * paid (saga trigger), and drives the delivery lifecycle through the REST API.
 * Every state change emits the corresponding event via the transactional
 * outbox so the order saga can react.
 */
@Service
@Transactional
public class DeliveryService {

    private static final Logger log = LoggerFactory.getLogger(DeliveryService.class);
    private static final List<String> COURIER_POOL =
            List.of("COURIER-ACME-01", "COURIER-ACME-02", "COURIER-SPEEDY-01", "COURIER-SPEEDY-02");
    private static final SecureRandom RANDOM = new SecureRandom();

    private final DeliveryRepository deliveryRepository;
    private final OutboxHelper outboxHelper;

    public DeliveryService(DeliveryRepository deliveryRepository, OutboxHelper outboxHelper) {
        this.deliveryRepository = deliveryRepository;
        this.outboxHelper = outboxHelper;
    }

    /**
     * Saga trigger: an order has been paid, so schedule its delivery.
     * Idempotent on orderId: a re-delivered order.paid does not create a
     * duplicate delivery.
     */
    public Delivery scheduleForOrder(UUID orderId, UUID customerId) {
        if (deliveryRepository.existsByOrderId(orderId)) {
            log.info("Delivery already exists for order {}; skipping", orderId);
            return deliveryRepository.findByOrderId(orderId).orElseThrow();
        }
        String courier = assignCourier();
        String tracking = generateTrackingNumber();
        Delivery delivery = new Delivery(orderId, customerId, courier, tracking);
        delivery = deliveryRepository.save(delivery);
        emitScheduled(delivery);
        log.info("Scheduled delivery {} for order {} via courier {} ({})",
                delivery.getId(), orderId, courier, tracking);
        return delivery;
    }

    public void cancelForOrder(UUID orderId) {
        deliveryRepository.findByOrderId(orderId).ifPresent(delivery -> {
            if (delivery.getStatus() == DeliveryStatus.SCHEDULED
                    || delivery.getStatus() == DeliveryStatus.IN_TRANSIT) {
                delivery.cancel();
                deliveryRepository.save(delivery);
                log.info("Cancelled delivery {} for order {}", delivery.getId(), orderId);
            }
        });
    }

    public DeliveryResponse markInTransit(UUID deliveryId) {
        Delivery delivery = load(deliveryId);
        delivery.markInTransit();
        return toResponse(deliveryRepository.save(delivery));
    }

    public DeliveryResponse markCompleted(UUID deliveryId) {
        Delivery delivery = load(deliveryId);
        if (delivery.getStatus() == DeliveryStatus.COMPLETED) {
            return toResponse(delivery);
        }
        delivery.markCompleted();
        delivery = deliveryRepository.save(delivery);
        emitCompleted(delivery);
        return toResponse(delivery);
    }

    public DeliveryResponse markFailed(UUID deliveryId, String reason) {
        Delivery delivery = load(deliveryId);
        if (delivery.getStatus() == DeliveryStatus.FAILED) {
            return toResponse(delivery);
        }
        delivery.markFailed(reason);
        delivery = deliveryRepository.save(delivery);
        emitFailed(delivery, reason);
        return toResponse(delivery);
    }

    @Transactional(readOnly = true)
    public DeliveryResponse getDelivery(UUID deliveryId) {
        return toResponse(load(deliveryId));
    }

    @Transactional(readOnly = true)
    public DeliveryResponse getByOrder(UUID orderId) {
        Delivery delivery = deliveryRepository.findByOrderId(orderId)
                .orElseThrow(() -> new NotFoundException(
                        "DELIVERY_NOT_FOUND", "No delivery for order " + orderId));
        return toResponse(delivery);
    }

    @Transactional(readOnly = true)
    public List<DeliveryResponse> listAll() {
        return deliveryRepository.findAll().stream().map(this::toResponse).toList();
    }

    private String assignCourier() {
        return COURIER_POOL.get(RANDOM.nextInt(COURIER_POOL.size()));
    }

    private String generateTrackingNumber() {
        byte[] bytes = new byte[6];
        RANDOM.nextBytes(bytes);
        StringBuilder sb = new StringBuilder("FF-");
        for (byte b : bytes) {
            sb.append(String.format("%02X", b));
        }
        return sb.toString();
    }

    private void emitScheduled(Delivery delivery) {
        outboxHelper.enqueue("Delivery", delivery.getId().toString(),
                EventTypes.DELIVERY_SCHEDULED, "deliveries.events.v1", delivery.getOrderId(),
                new DeliveryScheduledPayload(delivery.getId(), delivery.getOrderId(),
                        delivery.getCustomerId(), delivery.getCourierId(), delivery.getTrackingNumber()));
    }

    private void emitCompleted(Delivery delivery) {
        outboxHelper.enqueue("Delivery", delivery.getId().toString(),
                EventTypes.DELIVERY_COMPLETED, "deliveries.events.v1", delivery.getOrderId(),
                new DeliveryCompletedPayload(delivery.getId(), delivery.getOrderId(),
                        delivery.getCustomerId()));
    }

    private void emitFailed(Delivery delivery, String reason) {
        outboxHelper.enqueue("Delivery", delivery.getId().toString(),
                EventTypes.DELIVERY_FAILED, "deliveries.events.v1", delivery.getOrderId(),
                new DeliveryFailedPayload(delivery.getId(), delivery.getOrderId(),
                        delivery.getCustomerId(), reason));
    }

    private Delivery load(UUID id) {
        return deliveryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(
                        "DELIVERY_NOT_FOUND", "Delivery " + id + " not found"));
    }

    private DeliveryResponse toResponse(Delivery d) {
        return new DeliveryResponse(d.getId(), d.getOrderId(), d.getCustomerId(),
                d.getCourierId(), d.getTrackingNumber(), d.getStatus().name(),
                d.getScheduledAt(), d.getPickedUpAt(), d.getDeliveredAt(), d.getFailedAt(),
                d.getCancelledAt(), d.getFailureReason(), d.getVersion() == null ? 0 : d.getVersion(),
                d.getCreatedAt(), d.getUpdatedAt());
    }
}
