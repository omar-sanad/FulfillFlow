package com.fulfillflow.common.events.payloads;

import java.util.UUID;

public record DeliveryCompletedPayload(
        UUID deliveryId,
        UUID orderId,
        UUID customerId
) {
}
