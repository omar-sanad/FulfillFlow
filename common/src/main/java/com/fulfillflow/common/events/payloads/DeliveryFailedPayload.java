package com.fulfillflow.common.events.payloads;

import java.util.UUID;

/**
 * Payload for {@code delivery.failed}. Emitted when a delivery cannot be
 * scheduled or completed; lets the order saga mark the order as failed.
 */
public record DeliveryFailedPayload(
        UUID deliveryId,
        UUID orderId,
        UUID customerId,
        String reason
) {
}
