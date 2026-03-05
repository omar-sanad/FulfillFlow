package com.fulfillflow.inventory.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UuidGenerator;

@Entity
@Table(name = "stock_reservations")
public class StockReservation {

    public enum State { PENDING, CONFIRMED, RELEASED }

    @Id
    @UuidGenerator
    private UUID id;

    private UUID orderId;
    private UUID orderLineId;
    private UUID productId;
    private Integer quantity;

    @Enumerated(EnumType.STRING)
    private State state = State.PENDING;

    @CreationTimestamp
    private Instant createdAt;

    private Instant releasedAt;

    @Version
    private Long version;

    protected StockReservation() {
    }

    public StockReservation(UUID orderId, UUID orderLineId, UUID productId, int quantity) {
        this.orderId = orderId;
        this.orderLineId = orderLineId;
        this.productId = productId;
        this.quantity = quantity;
        this.state = State.PENDING;
    }

    public void confirm() {
        if (state != State.PENDING) {
            throw new IllegalStateException("Reservation " + id + " is not PENDING");
        }
        this.state = State.CONFIRMED;
    }

    public void release() {
        if (state == State.RELEASED) {
            throw new IllegalStateException("Reservation " + id + " already released");
        }
        this.state = State.RELEASED;
        this.releasedAt = Instant.now();
    }

    public UUID getId() { return id; }
    public UUID getOrderId() { return orderId; }
    public UUID getOrderLineId() { return orderLineId; }
    public UUID getProductId() { return productId; }
    public Integer getQuantity() { return quantity; }
    public State getState() { return state; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getReleasedAt() { return releasedAt; }
    public Long getVersion() { return version; }
}
