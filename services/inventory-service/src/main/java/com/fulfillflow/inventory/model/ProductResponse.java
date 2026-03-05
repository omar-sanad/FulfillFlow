package com.fulfillflow.inventory.model;

import java.time.Instant;
import java.util.UUID;

public record ProductResponse(
        UUID id,
        String sku,
        String name,
        String description,
        Long priceCents,
        String currency,
        Integer weightGrams,
        Boolean active,
        Long version,
        Instant createdAt,
        Instant updatedAt,
        Integer availableQuantity,
        Integer reservedQuantity
) {
}
