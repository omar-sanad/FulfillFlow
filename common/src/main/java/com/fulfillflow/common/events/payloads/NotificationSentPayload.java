package com.fulfillflow.common.events.payloads;

import java.time.Instant;
import java.util.UUID;

public record NotificationSentPayload(
        UUID notificationId,
        UUID orderId,
        UUID customerId,
        String channel,
        String template,
        String status,
        Instant sentAt
) {
}
