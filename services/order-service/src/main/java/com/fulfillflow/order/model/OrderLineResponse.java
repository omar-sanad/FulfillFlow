package com.fulfillflow.order.model;

import java.time.Instant;
import java.util.UUID;

public record OrderLineResponse(
        UUID id,
        UUID productId,
        String sku,
        String name,
        Long unitPriceCents,
        Integer quantity,
        Long lineTotalCents,
        String currency,
        Instant createdAt
) {
}
