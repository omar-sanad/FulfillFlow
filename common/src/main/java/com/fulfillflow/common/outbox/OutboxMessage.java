package com.fulfillflow.common.outbox;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * A single message written transactionally alongside a domain change, to be
 * published to Kafka asynchronously. Implements the transactional outbox
 * pattern: the business write and the outbox insert commit in the same DB
 * transaction, so an event is never lost (and never published speculatively).
 */
@Entity
@Table(name = "outbox_messages")
public class OutboxMessage {

    public enum Status {
        NEW,
        IN_PROGRESS,
        SENT,
        FAILED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "aggregate_type", nullable = false)
    private String aggregateType;

    @Column(name = "aggregate_id", nullable = false)
    private String aggregateId;

    @Column(name = "event_type", nullable = false)
    private String eventType;

    @Column(name = "event_version", nullable = false)
    private String eventVersion = "v1";

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "payload", nullable = false, columnDefinition = "jsonb")
    private String payload;

    @Column(name = "topic", nullable = false)
    private String topic;

    @Column(name = "correlation_id")
    private UUID correlationId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status = Status.NEW;

    @Column(name = "retry_count")
    private int retryCount = 0;

    @Column(name = "last_error")
    private String lastError;

    @Column(name = "next_attempt_at")
    private Instant nextAttemptAt = Instant.now();

    @Column(name = "published_at")
    private Instant publishedAt;

    @Version
    private Long version;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    protected OutboxMessage() {
    }

    public OutboxMessage(String aggregateType, String aggregateId, String eventType,
                        String topic, UUID correlationId, String payloadJson) {
        this.aggregateType = aggregateType;
        this.aggregateId = aggregateId;
        this.eventType = eventType;
        this.topic = topic;
        this.correlationId = correlationId;
        this.payload = payloadJson;
        this.status = Status.NEW;
        this.nextAttemptAt = Instant.now();
    }

    public Long getId() { return id; }
    public String getAggregateType() { return aggregateType; }
    public String getAggregateId() { return aggregateId; }
    public String getEventType() { return eventType; }
    public String getEventVersion() { return eventVersion; }
    public String getPayload() { return payload; }
    public String getTopic() { return topic; }
    public UUID getCorrelationId() { return correlationId; }
    public Status getStatus() { return status; }
    public int getRetryCount() { return retryCount; }
    public String getLastError() { return lastError; }
    public Instant getNextAttemptAt() { return nextAttemptAt; }
    public Instant getPublishedAt() { return publishedAt; }
    public Long getVersion() { return version; }
    public Instant getCreatedAt() { return createdAt; }

    public void markInProgress() { this.status = Status.IN_PROGRESS; }
    public void markSent() {
        this.status = Status.SENT;
        this.publishedAt = Instant.now();
    }
    public void markFailed(String error) {
        this.status = Status.FAILED;
        this.lastError = error;
    }
    public void scheduleRetry(int maxRetries) {
        this.retryCount++;
        this.status = Status.NEW;
        long backoffSeconds = (long) Math.min(30, Math.pow(2, retryCount));
        this.nextAttemptAt = Instant.now().plusSeconds(backoffSeconds);
        if (this.retryCount > maxRetries) {
            this.status = Status.FAILED;
        }
    }
}
