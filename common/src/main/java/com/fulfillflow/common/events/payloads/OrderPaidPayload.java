package com.fulfillflow.common.events.payloads;

import java.util.UUID;

public record OrderPaidPayload(
        UUID orderId,
        UUID customerId,
        long totalCents,
        String currency
) {
}
