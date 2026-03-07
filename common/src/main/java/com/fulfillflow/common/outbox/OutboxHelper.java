package com.fulfillflow.common.outbox;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
import org.springframework.stereotype.Component;

/**
 * Convenience for writing outbox entries within the current transaction.
 * Services inject this and call {@link #enqueue} with a payload object; the
 * payload is serialised to JSON and a new {@link OutboxMessage} is persisted
 * in the same JPA transaction as the domain change.
 */
@Component
public class OutboxHelper {

    private final OutboxMessageRepository outboxRepository;
    private final ObjectMapper objectMapper;

    public OutboxHelper(OutboxMessageRepository outboxRepository, ObjectMapper objectMapper) {
        this.outboxRepository = outboxRepository;
        this.objectMapper = objectMapper;
    }

    public void enqueue(String aggregateType, String aggregateId, String eventType,
                        String topic, UUID correlationId, Object payload) {
        try {
            String json = objectMapper.writeValueAsString(payload);
            outboxRepository.save(new OutboxMessage(
                    aggregateType, aggregateId, eventType, topic, correlationId, json));
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialise outbox payload for " + eventType, e);
        }
    }
}
