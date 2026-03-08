package com.fulfillflow.inventory.event;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.CreationTimestamp;

/**
 * Record of a consumed event, keyed by the event's {@code messageId}.
 * Enables idempotent consumption: if the same event is redelivered (at-least-once),
 * the consumer detects the duplicate and skips it.
 */
@Entity
@Table(name = "consumed_events")
public class ConsumedEvent {

    @Id
    private UUID eventId;

    @Column(nullable = false)
    private String eventType;

    @CreationTimestamp
    @Column(name = "consumed_at", updatable = false)
    private Instant consumedAt;

    protected ConsumedEvent() {
    }

    public ConsumedEvent(UUID eventId, String eventType) {
        this.eventId = eventId;
        this.eventType = eventType;
    }

    public UUID getEventId() { return eventId; }
    public String getEventType() { return eventType; }
    public Instant getConsumedAt() { return consumedAt; }
}
