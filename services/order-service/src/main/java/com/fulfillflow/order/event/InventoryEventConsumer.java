package com.fulfillflow.order.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fulfillflow.common.events.EventEnvelope;
import com.fulfillflow.common.events.EventTypes;
import com.fulfillflow.common.events.payloads.InventoryReservationFailedPayload;
import com.fulfillflow.common.events.payloads.InventoryReleasedPayload;
import com.fulfillflow.common.events.payloads.InventoryReservedPayload;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

/**
 * Consumes inventory events from {@code inventory.events.v1} and drives the
 * order saga. Idempotent: duplicate events are skipped via the
 * {@code consumed_events} table.
 */
@Component
public class InventoryEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(InventoryEventConsumer.class);

    private final OrderSagaService sagaService;
    private final ConsumedEventRepository consumedEventRepository;
    private final ObjectMapper objectMapper;

    public InventoryEventConsumer(OrderSagaService sagaService,
                                  ConsumedEventRepository consumedEventRepository,
                                  ObjectMapper objectMapper) {
        this.sagaService = sagaService;
        this.consumedEventRepository = consumedEventRepository;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "inventory.events.v1", groupId = "order-service",
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
            case EventTypes.INVENTORY_RESERVED -> {
                InventoryReservedPayload payload = convert(envelope, InventoryReservedPayload.class);
                sagaService.onReserved(payload.orderId());
            }
            case EventTypes.INVENTORY_RESERVATION_FAILED -> {
                InventoryReservationFailedPayload payload =
                        convert(envelope, InventoryReservationFailedPayload.class);
                sagaService.onReservationFailed(payload);
            }
            case EventTypes.INVENTORY_RELEASED -> {
                InventoryReleasedPayload payload = convert(envelope, InventoryReleasedPayload.class);
                sagaService.onReleased(payload.orderId());
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
}
