package com.fulfillflow.common.events.payloads;

import java.util.UUID;

public record NotificationRequestedPayload(
        UUID orderId,
        UUID customerId,
        String channel,
        String template,
        String recipient,
        java.util.Map<String, String> variables
) {
}
