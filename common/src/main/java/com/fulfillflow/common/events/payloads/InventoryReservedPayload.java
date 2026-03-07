package com.fulfillflow.common.events.payloads;

import java.util.List;
import java.util.UUID;

public record InventoryReservedPayload(
        UUID orderId,
        UUID customerId,
        List<ReservedItem> items
) {
    public record ReservedItem(UUID productId, String sku, int quantity) {
    }
}
