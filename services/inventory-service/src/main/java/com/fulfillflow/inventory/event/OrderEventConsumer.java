package com.fulfillflow.inventory.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fulfillflow.common.events.EventEnvelope;
import com.fulfillflow.common.events.EventTypes;
import com.fulfillflow.common.events.payloads.OrderCreatedPayload;
import com.fulfillflow.inventory.domain.InsufficientStockException;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.serializer.DeserializationException;
import org.springframework.stereotype.Component;

/**
 * Consumes order lifecycle events from the {@code orders.events.v1} topic and
 * drives the inventory saga participant. Idempotent: duplicate events (same
 * {@code messageId}) are skipped via the {@code consumed_events} table.
 */
@Component
public class OrderEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(OrderEventConsumer.class);

    private final ReservationService reservationService;
    private final ConsumedEventRepository consumedEventRepository;
    private final ObjectMapper objectMapper;

    public OrderEventConsumer(ReservationService reservationService,
                              ConsumedEventRepository consumedEventRepository,
                              ObjectMapper objectMapper) {
        this.reservationService = reservationService;
        this.consumedEventRepository = consumedEventRepository;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "orders.events.v1", groupId = "inventory-service",
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
            // Don't ack — Kafka will redeliver
        }
    }

    private void handle(EventEnvelope envelope) {
        switch (envelope.eventType()) {
            case EventTypes.ORDER_CREATED -> {
                OrderCreatedPayload payload = convertPayload(envelope, OrderCreatedPayload.class);
                try {
                    reservationService.reserveForOrder(payload);
                } catch (InsufficientStockException e) {
                    log.warn("Insufficient stock for order {}: {}",
                            payload.orderId(), e.getMessage());
                    reservationService.emitReservationFailure(payload, e.getMessage());
                }
            }
            case EventTypes.ORDER_CANCELLED -> {
                UUID orderId = extractOrderId(envelope);
                reservationService.releaseForOrder(orderId, "order.cancelled");
            }
            case EventTypes.ORDER_FULFILLED -> {
                UUID orderId = extractOrderId(envelope);
                reservationService.confirmForOrder(orderId);
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

    private <T> T convertPayload(EventEnvelope envelope, Class<T> type) {
        return objectMapper.convertValue(envelope.payload(), type);
    }

    private UUID extractOrderId(EventEnvelope envelope) {
        var node = objectMapper.convertValue(envelope.payload(),
                com.fasterxml.jackson.databind.node.ObjectNode.class);
        return UUID.fromString(node.get("orderId").asText());
    }
}
