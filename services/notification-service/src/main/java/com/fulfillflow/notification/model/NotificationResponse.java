package com.fulfillflow.notification.model;

import java.time.Instant;
import java.util.UUID;

public record NotificationResponse(
        UUID id,
        UUID orderId,
        UUID customerId,
        String channel,
        String template,
        String recipient,
        String subject,
        String status,
        Instant sentAt,
        String failureReason,
        long version,
        Instant createdAt,
        Instant updatedAt
) {
}
