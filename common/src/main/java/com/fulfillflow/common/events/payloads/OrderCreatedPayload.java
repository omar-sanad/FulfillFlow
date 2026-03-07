package com.fulfillflow.common.events.payloads;

import java.util.List;
import java.util.UUID;

/**
 * Payload for {@code order.created}. Carries the full order snapshot so that
 * downstream services (inventory, delivery) can act without calling back.
 */
public record OrderCreatedPayload(
        UUID orderId,
        UUID customerId,
        List<OrderLineItem> lines,
        long totalCents,
        String currency,
        String shippingFullName
) {
    public record OrderLineItem(UUID productId, String sku, String name, int quantity, long unitPriceCents) {
    }
}
