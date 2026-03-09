package com.fulfillflow.order.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fulfillflow.common.events.EventEnvelope;
import com.fulfillflow.common.events.EventTypes;
import com.fulfillflow.common.events.payloads.DeliveryCompletedPayload;
import com.fulfillflow.common.events.payloads.DeliveryFailedPayload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

/**
 * Consumes delivery events from {@code deliveries.events.v1} and drives the
 * order saga: auto-fulfils on delivery.completed and marks the order failed on
 * delivery.failed. Idempotent via the {@code consumed_events} table.
 */
@Component
public class DeliveryEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(DeliveryEventConsumer.class);

    private final OrderSagaService sagaService;
    private final ConsumedEventRepository consumedEventRepository;
    private final ObjectMapper objectMapper;

    public DeliveryEventConsumer(OrderSagaService sagaService,
                                 ConsumedEventRepository consumedEventRepository,
                                 ObjectMapper objectMapper) {
        this.sagaService = sagaService;
        this.consumedEventRepository = consumedEventRepository;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "deliveries.events.v1", groupId = "order-service",
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
            case EventTypes.DELIVERY_COMPLETED -> {
                DeliveryCompletedPayload payload = convert(envelope, DeliveryCompletedPayload.class);
                sagaService.onDeliveryCompleted(payload);
            }
            case EventTypes.DELIVERY_FAILED -> {
                DeliveryFailedPayload payload = convert(envelope, DeliveryFailedPayload.class);
                sagaService.onDeliveryFailed(payload);
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
