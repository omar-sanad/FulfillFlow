package com.fulfillflow.notification.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fulfillflow.common.events.EventEnvelope;
import com.fulfillflow.common.events.EventTypes;
import com.fulfillflow.common.events.payloads.OrderCancelledPayload;
import com.fulfillflow.common.events.payloads.OrderCreatedPayload;
import com.fulfillflow.common.events.payloads.OrderFulfilledPayload;
import com.fulfillflow.common.events.payloads.OrderPaidPayload;
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
 * Consumes order lifecycle events from {@code orders.events.v1} and dispatches
 * a customer email notification for each notable transition. Idempotent via the
 * {@code consumed_events} table.
 */
@Component
public class OrderEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(OrderEventConsumer.class);

    private final NotificationService notificationService;
    private final ConsumedEventRepository consumedEventRepository;
    private final ObjectMapper objectMapper;

    public OrderEventConsumer(NotificationService notificationService,
                               ConsumedEventRepository consumedEventRepository,
                               ObjectMapper objectMapper) {
        this.notificationService = notificationService;
        this.consumedEventRepository = consumedEventRepository;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "orders.events.v1", groupId = "notification-service",
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
            case EventTypes.ORDER_CREATED -> {
                OrderCreatedPayload p = convert(envelope, OrderCreatedPayload.class);
                notificationService.sendForOrder(p.orderId(), p.customerId(), "EMAIL",
                        "order.created", emailFor(p.customerId()), vars("orderId", shortId(p.orderId())));
            }
            case EventTypes.ORDER_PAID -> {
                OrderPaidPayload p = convert(envelope, OrderPaidPayload.class);
                notificationService.sendForOrder(p.orderId(), p.customerId(), "EMAIL",
                        "order.paid", emailFor(p.customerId()), vars("orderId", shortId(p.orderId())));
            }
            case EventTypes.ORDER_FULFILLED -> {
                OrderFulfilledPayload p = convert(envelope, OrderFulfilledPayload.class);
                notificationService.sendForOrder(p.orderId(), p.customerId(), "EMAIL",
                        "order.fulfilled", emailFor(p.customerId()), vars("orderId", shortId(p.orderId())));
            }
            case EventTypes.ORDER_CANCELLED -> {
                OrderCancelledPayload p = convert(envelope, OrderCancelledPayload.class);
                Map<String, String> v = vars("orderId", shortId(p.orderId()));
                v.put("reason", p.reason());
                notificationService.sendForOrder(p.orderId(), p.customerId(), "EMAIL",
                        "order.cancelled", emailFor(p.customerId()), v);
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

    private String shortId(UUID id) {
        return id.toString().substring(0, 8).toUpperCase();
    }

    private Map<String, String> vars(String k, String v) {
        Map<String, String> m = new HashMap<>();
        m.put(k, v);
        return m;
    }
}
