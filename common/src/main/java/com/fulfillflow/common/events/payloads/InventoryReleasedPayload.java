package com.fulfillflow.common.events.payloads;

import java.util.UUID;

public record InventoryReleasedPayload(
        UUID orderId,
        String reason
) {
}
