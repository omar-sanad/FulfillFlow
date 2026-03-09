package com.fulfillflow.delivery.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.UuidGenerator;

/**
 * A scheduled delivery for an order, assigned to a courier. Follows the
 * lifecycle {@code SCHEDULED → IN_TRANSIT → COMPLETED} with compensation
 * paths to FAILED / CANCELLED.
 */
@Entity
@Table(name = "deliveries")
public class Delivery {

    @Id
    @UuidGenerator
    private UUID id;

    @Column(name = "order_id")
    private UUID orderId;

    @Column(name = "customer_id")
    private UUID customerId;

    @Column(name = "courier_id")
    private String courierId;

    @Column(name = "tracking_number")
    private String trackingNumber;

    @Enumerated(EnumType.STRING)
    private DeliveryStatus status = DeliveryStatus.SCHEDULED;

    @Column(name = "scheduled_at")
    private Instant scheduledAt;

    @Column(name = "picked_up_at")
    private Instant pickedUpAt;

    @Column(name = "delivered_at")
    private Instant deliveredAt;

    @Column(name = "failed_at")
    private Instant failedAt;

    @Column(name = "cancelled_at")
    private Instant cancelledAt;

    @Column(name = "failure_reason")
    private String failureReason;

    @Version
    private Long version;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;

    protected Delivery() {
    }

    public Delivery(UUID orderId, UUID customerId, String courierId, String trackingNumber) {
        this.orderId = orderId;
        this.customerId = customerId;
        this.courierId = courierId;
        this.trackingNumber = trackingNumber;
        this.status = DeliveryStatus.SCHEDULED;
        this.scheduledAt = Instant.now();
    }

    public void markInTransit() {
        status.requireTransitionTo(DeliveryStatus.IN_TRANSIT);
        this.status = DeliveryStatus.IN_TRANSIT;
        this.pickedUpAt = Instant.now();
    }

    public void markCompleted() {
        status.requireTransitionTo(DeliveryStatus.COMPLETED);
        this.status = DeliveryStatus.COMPLETED;
        this.deliveredAt = Instant.now();
    }

    public void markFailed(String reason) {
        status.requireTransitionTo(DeliveryStatus.FAILED);
        this.status = DeliveryStatus.FAILED;
        this.failureReason = reason;
        this.failedAt = Instant.now();
    }

    public void cancel() {
        status.requireTransitionTo(DeliveryStatus.CANCELLED);
        this.status = DeliveryStatus.CANCELLED;
        this.cancelledAt = Instant.now();
    }

    public UUID getId() { return id; }
    public UUID getOrderId() { return orderId; }
    public UUID getCustomerId() { return customerId; }
    public String getCourierId() { return courierId; }
    public String getTrackingNumber() { return trackingNumber; }
    public DeliveryStatus getStatus() { return status; }
    public Instant getScheduledAt() { return scheduledAt; }
    public Instant getPickedUpAt() { return pickedUpAt; }
    public Instant getDeliveredAt() { return deliveredAt; }
    public Instant getFailedAt() { return failedAt; }
    public Instant getCancelledAt() { return cancelledAt; }
    public String getFailureReason() { return failureReason; }
    public Long getVersion() { return version; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
