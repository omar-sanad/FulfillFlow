package com.fulfillflow.common.events.payloads;

import java.util.UUID;

public record OrderFulfilledPayload(
        UUID orderId,
        UUID customerId
) {
}
