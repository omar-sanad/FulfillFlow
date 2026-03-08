package com.fulfillflow.order.event;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.CreationTimestamp;

/**
 * Record of a consumed event, keyed by the event's {@code messageId}.
 * Enables idempotent consumption under at-least-once delivery.
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
