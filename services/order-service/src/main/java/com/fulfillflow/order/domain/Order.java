package com.fulfillflow.order.domain;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "orders")
public class Order {

    @Id
    @UuidGenerator
    private UUID id;

    @Column(name = "customer_id")
    private UUID customerId;

    @Enumerated(EnumType.STRING)
    private OrderStatus status = OrderStatus.CREATED;

    @Column(name = "total_cents")
    private Long totalCents = 0L;
    private String currency;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "shipping_address", nullable = false)
    private ShippingAddress shippingAddress;

    private String notes;

    @Column(name = "placed_at")
    private Instant placedAt;

    @Column(name = "paid_at")
    private Instant paidAt;

    @Column(name = "fulfilled_at")
    private Instant fulfilledAt;

    @Column(name = "cancelled_at")
    private Instant cancelledAt;

    @Column(name = "cancel_reason")
    private String cancelReason;

    @Version
    private Long version;

    @CreationTimestamp
    @Column(name = "created_at")
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;

    @OneToMany(mappedBy = "orderId", fetch = FetchType.LAZY)
    private List<OrderLine> lines = new ArrayList<>();

    protected Order() {
    }

    public Order(UUID customerId, ShippingAddress shippingAddress, String notes, String currency) {
        this.customerId = customerId;
        this.shippingAddress = shippingAddress;
        this.notes = notes;
        this.currency = currency;
        this.status = OrderStatus.CREATED;
        this.placedAt = Instant.now();
        this.totalCents = 0L;
    }

    void recomputeTotal() {
        long total = 0L;
        for (OrderLine line : lines) {
            total += line.getLineTotalCents();
        }
        this.totalCents = total;
    }

    public void transitionTo(OrderStatus target, String reason) {
        status.requireTransitionTo(target, reason);
        Instant now = Instant.now();
        switch (target) {
            case PAID -> { this.status = OrderStatus.PAID; this.paidAt = now; }
            case FULFILLED -> { this.status = OrderStatus.FULFILLED; this.fulfilledAt = now; }
            case CANCELLED -> { this.status = OrderStatus.CANCELLED; this.cancelledAt = now; this.cancelReason = reason; }
            case FAILED -> { this.status = OrderStatus.FAILED; this.cancelReason = reason; }
            default -> throw new IllegalStateException("Unexpected target: " + target);
        }
    }

    public UUID getId() { return id; }
    public UUID getCustomerId() { return customerId; }
    public OrderStatus getStatus() { return status; }
    public Long getTotalCents() { return totalCents; }
    public String getCurrency() { return currency; }
    public ShippingAddress getShippingAddress() { return shippingAddress; }
    public String getNotes() { return notes; }
    public Instant getPlacedAt() { return placedAt; }
    public Instant getPaidAt() { return paidAt; }
    public Instant getFulfilledAt() { return fulfilledAt; }
    public Instant getCancelledAt() { return cancelledAt; }
    public String getCancelReason() { return cancelReason; }
    public Long getVersion() { return version; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public List<OrderLine> getLines() { return lines; }
}
