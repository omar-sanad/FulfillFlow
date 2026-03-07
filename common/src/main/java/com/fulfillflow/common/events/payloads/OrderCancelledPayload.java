package com.fulfillflow.common.events.payloads;

import java.util.UUID;

public record OrderCancelledPayload(
        UUID orderId,
        UUID customerId,
        String reason
) {
}
