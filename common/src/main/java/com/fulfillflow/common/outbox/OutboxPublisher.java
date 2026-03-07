package com.fulfillflow.common.outbox;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fulfillflow.common.events.EventEnvelope;
import java.time.Instant;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Polls the transactional outbox and publishes due messages to Kafka.
 * <p>
 * Each candidate is marked IN_PROGRESS, then published within a new
 * transaction. On success it is marked SENT; on failure it is rescheduled
 * with exponential backoff, or moved to FAILED after exhausting retries.
 */
@Component
public class OutboxPublisher {

    private static final Logger log = LoggerFactory.getLogger(OutboxPublisher.class);

    private final OutboxMessageRepository outboxRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final int batchSize;
    private final int maxRetries;

    public OutboxPublisher(OutboxMessageRepository outboxRepository,
                          KafkaTemplate<String, Object> kafkaTemplate,
                          ObjectMapper objectMapper,
                          @Value("${fulfillflow.outbox.batch-size:50}") int batchSize,
                          @Value("${fulfillflow.outbox.max-retries:10}") int maxRetries) {
        this.outboxRepository = outboxRepository;
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
        this.batchSize = batchSize;
        this.maxRetries = maxRetries;
    }

    @Scheduled(fixedDelayString = "${fulfillflow.outbox.poll-interval-ms:2000}")
    public void publishPending() {
        List<OutboxMessage> due = outboxRepository.findDue(Instant.now(), PageRequest.of(0, batchSize));
        if (due.isEmpty()) {
            return;
        }
        log.debug("Publishing {} outbox messages", due.size());
        for (OutboxMessage message : due) {
            publishOne(message);
        }
    }

    @Transactional
    public void publishOne(OutboxMessage message) {
        message.markInProgress();
        outboxRepository.saveAndFlush(message);
        try {
            Object payload = objectMapper.readValue(message.getPayload(), Object.class);
            EventEnvelope envelope = EventEnvelope.builder()
                    .eventType(message.getEventType())
                    .eventVersion(message.getEventVersion())
                    .correlationId(message.getCorrelationId())
                    .occurredAt(Instant.now())
                    .payload(payload)
                    .build();
            kafkaTemplate.send(message.getTopic(), message.getAggregateId(), envelope)
                    .whenComplete((result, ex) -> {
                        if (ex != null) {
                            handleFailure(message, ex);
                        } else {
                            handleSuccess(message);
                        }
                    });
        } catch (Exception e) {
            handleFailure(message, e);
        }
    }

    @Transactional
    public void handleSuccess(OutboxMessage message) {
        message.markSent();
        outboxRepository.save(message);
    }

    @Transactional
    public void handleFailure(OutboxMessage message, Throwable ex) {
        log.warn("Failed to publish outbox message {} (retry {}): {}",
                message.getId(), message.getRetryCount(), ex.toString());
        message.scheduleRetry(maxRetries);
        outboxRepository.save(message);
    }
}
