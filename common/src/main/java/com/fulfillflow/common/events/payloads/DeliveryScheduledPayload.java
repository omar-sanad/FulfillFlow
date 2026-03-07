package com.fulfillflow.common.events.payloads;

import java.util.UUID;

public record DeliveryScheduledPayload(
        UUID deliveryId,
        UUID orderId,
        UUID customerId,
        String courierId,
        String trackingNumber
) {
}
