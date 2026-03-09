package com.fulfillflow.delivery.model;

import java.time.Instant;
import java.util.UUID;

public record DeliveryResponse(
        UUID id,
        UUID orderId,
        UUID customerId,
        String courierId,
        String trackingNumber,
        String status,
        Instant scheduledAt,
        Instant pickedUpAt,
        Instant deliveredAt,
        Instant failedAt,
        Instant cancelledAt,
        String failureReason,
        long version,
        Instant createdAt,
        Instant updatedAt
) {
}
