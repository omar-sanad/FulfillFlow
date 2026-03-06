package com.fulfillflow.order.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "order_status_history")
public class OrderStatusHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private UUID orderId;
    private String fromStatus;
    private String toStatus;
    private String reason;
    private String actor;

    @CreationTimestamp
    private Instant occurredAt;

    protected OrderStatusHistory() {
    }

    public OrderStatusHistory(UUID orderId, OrderStatus from, OrderStatus to, String reason, String actor) {
        this.orderId = orderId;
        this.fromStatus = from != null ? from.name() : null;
        this.toStatus = to.name();
        this.reason = reason;
        this.actor = actor;
    }

    public Long getId() { return id; }
    public UUID getOrderId() { return orderId; }
    public String getFromStatus() { return fromStatus; }
    public String getToStatus() { return toStatus; }
    public String getReason() { return reason; }
    public String getActor() { return actor; }
    public Instant getOccurredAt() { return occurredAt; }
}
