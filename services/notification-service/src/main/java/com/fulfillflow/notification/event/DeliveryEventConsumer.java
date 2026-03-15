package com.fulfillflow.notification.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fulfillflow.common.events.EventEnvelope;
import com.fulfillflow.common.events.EventTypes;
import com.fulfillflow.common.events.payloads.DeliveryCompletedPayload;
import com.fulfillflow.common.events.payloads.DeliveryFailedPayload;
import com.fulfillflow.common.events.payloads.DeliveryScheduledPayload;
import com.fulfillflow.notification.NotificationService;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

/**
 * Consumes delivery lifecycle events from {@code deliveries.events.v1} and
 * dispatches a customer SMS notification tracking the parcel. Idempotent via
 * the {@code consumed_events} table.
 */
@Component
public class DeliveryEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(DeliveryEventConsumer.class);

    private final NotificationService notificationService;
    private final ConsumedEventRepository consumedEventRepository;
    private final ObjectMapper objectMapper;

    public DeliveryEventConsumer(NotificationService notificationService,
                                  ConsumedEventRepository consumedEventRepository,
                                  ObjectMapper objectMapper) {
        this.notificationService = notificationService;
        this.consumedEventRepository = consumedEventRepository;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "deliveries.events.v1", groupId = "notification-service",
            containerFactory = "kafkaListenerContainerFactory")
    public void onEvent(EventEnvelope envelope, Acknowledgment ack) {
        log.debug("Received event {} ({})", envelope.eventType(), envelope.messageId());
        try {
            if (isDuplicate(envelope)) {
                log.info("Skipping duplicate event {} ({})", envelope.eventType(), envelope.messageId());
                ack.acknowledge();
                return;
            }
            handle(envelope);
            markConsumed(envelope);
            ack.acknowledge();
        } catch (Exception e) {
            log.error("Failed to process event {} ({}): {}", envelope.eventType(),
                    envelope.messageId(), e.getMessage(), e);
            throw e;
        }
    }

    private void handle(EventEnvelope envelope) {
        switch (envelope.eventType()) {
            case EventTypes.DELIVERY_SCHEDULED -> {
                DeliveryScheduledPayload p = convert(envelope, DeliveryScheduledPayload.class);
                Map<String, String> v = vars("orderId", shortId(p.orderId()));
                v.put("courierId", p.courierId());
                v.put("trackingNumber", p.trackingNumber());
                notificationService.sendForOrder(p.orderId(), p.customerId(), "SMS",
                        "delivery.scheduled", smsFor(p.customerId()), v);
            }
            case EventTypes.DELIVERY_COMPLETED -> {
                DeliveryCompletedPayload p = convert(envelope, DeliveryCompletedPayload.class);
                notificationService.sendForOrder(p.orderId(), p.customerId(), "EMAIL",
                        "delivery.completed", emailFor(p.customerId()), vars("orderId", shortId(p.orderId())));
            }
            case EventTypes.DELIVERY_FAILED -> {
                DeliveryFailedPayload p = convert(envelope, DeliveryFailedPayload.class);
                Map<String, String> v = vars("orderId", shortId(p.orderId()));
                v.put("reason", p.reason());
                notificationService.sendForOrder(p.orderId(), p.customerId(), "SMS",
                        "delivery.failed", smsFor(p.customerId()), v);
            }
            default -> log.debug("Ignoring event type {}", envelope.eventType());
        }
    }

    private boolean isDuplicate(EventEnvelope envelope) {
        return consumedEventRepository.existsById(envelope.messageId());
    }

    private void markConsumed(EventEnvelope envelope) {
        consumedEventRepository.save(new ConsumedEvent(envelope.messageId(), envelope.eventType()));
    }

    private <T> T convert(EventEnvelope envelope, Class<T> type) {
        return objectMapper.convertValue(envelope.payload(), type);
    }

    private String emailFor(UUID customerId) {
        return "customer-" + customerId.toString().substring(0, 8) + "@fulfillflow.dev";
    }

    private String smsFor(UUID customerId) {
        return "+201" + (customerId.hashCode() & 0x7FFFFFFF) % 100_000_000;
    }

    private String shortId(UUID id) {
        return id.toString().substring(0, 8).toUpperCase();
    }

    private Map<String, String> vars(String k, String v) {
        Map<String, String> m = new HashMap<>();
        m.put(k, v);
        return m;
    }
}
