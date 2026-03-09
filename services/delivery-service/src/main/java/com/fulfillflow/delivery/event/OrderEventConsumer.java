package com.fulfillflow.delivery.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fulfillflow.common.events.EventEnvelope;
import com.fulfillflow.common.events.EventTypes;
import com.fulfillflow.common.events.payloads.OrderCancelledPayload;
import com.fulfillflow.common.events.payloads.OrderPaidPayload;
import com.fulfillflow.delivery.DeliveryService;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

/**
 * Consumes order lifecycle events from {@code orders.events.v1} and drives the
 * delivery saga: schedules a delivery when an order is paid and cancels a
 * scheduled delivery when the order is cancelled. Idempotent via the
 * {@code consumed_events} table.
 */
@Component
public class OrderEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(OrderEventConsumer.class);

    private final DeliveryService deliveryService;
    private final ConsumedEventRepository consumedEventRepository;
    private final ObjectMapper objectMapper;

    public OrderEventConsumer(DeliveryService deliveryService,
                              ConsumedEventRepository consumedEventRepository,
                              ObjectMapper objectMapper) {
        this.deliveryService = deliveryService;
        this.consumedEventRepository = consumedEventRepository;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "orders.events.v1", groupId = "delivery-service",
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
        }
    }

    private void handle(EventEnvelope envelope) {
        switch (envelope.eventType()) {
            case EventTypes.ORDER_PAID -> {
                OrderPaidPayload payload = convert(envelope, OrderPaidPayload.class);
                deliveryService.scheduleForOrder(payload.orderId(), payload.customerId());
            }
            case EventTypes.ORDER_CANCELLED -> {
                UUID orderId = extractOrderId(envelope);
                deliveryService.cancelForOrder(orderId);
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

    private UUID extractOrderId(EventEnvelope envelope) {
        var node = objectMapper.convertValue(envelope.payload(),
                com.fasterxml.jackson.databind.node.ObjectNode.class);
        return UUID.fromString(node.get("orderId").asText());
    }
}
