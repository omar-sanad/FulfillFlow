package com.fulfillflow.notification.domain;

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

@Entity
@Table(name = "notifications")
public class Notification {

    @Id
    private UUID id;

    @Column(name = "order_id")
    private UUID orderId;

    @Column(name = "customer_id", nullable = false)
    private UUID customerId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 24)
    private NotificationChannel channel;

    @Column(nullable = false, length = 64)
    private String template;

    @Column(nullable = false, length = 256)
    private String recipient;

    @Column(length = 256)
    private String subject;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String body;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private NotificationStatus status = NotificationStatus.PENDING;

    @Column(name = "sent_at")
    private Instant sentAt;

    @Column(name = "failure_reason", columnDefinition = "TEXT")
    private String failureReason;

    @Version
    private Long version;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;

    protected Notification() {
    }

    public Notification(UUID id, UUID orderId, UUID customerId, NotificationChannel channel,
                        String template, String recipient, String subject, String body) {
        this.id = id;
        this.orderId = orderId;
        this.customerId = customerId;
        this.channel = channel;
        this.template = template;
        this.recipient = recipient;
        this.subject = subject;
        this.body = body;
    }

    public void markSent() {
        this.status = NotificationStatus.SENT;
        this.sentAt = Instant.now();
    }

    public void markFailed(String reason) {
        this.status = NotificationStatus.FAILED;
        this.failureReason = reason;
    }

    public UUID getId() { return id; }
    public UUID getOrderId() { return orderId; }
    public UUID getCustomerId() { return customerId; }
    public NotificationChannel getChannel() { return channel; }
    public String getTemplate() { return template; }
    public String getRecipient() { return recipient; }
    public String getSubject() { return subject; }
    public String getBody() { return body; }
    public NotificationStatus getStatus() { return status; }
    public Instant getSentAt() { return sentAt; }
    public String getFailureReason() { return failureReason; }
    public Long getVersion() { return version; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
