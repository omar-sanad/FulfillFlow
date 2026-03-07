package com.fulfillflow.common.events.payloads;

import java.util.UUID;

public record InventoryReservationFailedPayload(
        UUID orderId,
        UUID customerId,
        String reason
) {
}
