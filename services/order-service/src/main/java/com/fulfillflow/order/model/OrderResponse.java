package com.fulfillflow.order.model;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record OrderResponse(
        UUID id,
        UUID customerId,
        String status,
        Long totalCents,
        String currency,
        ShippingAddressRequest shippingAddress,
        String notes,
        Instant placedAt,
        Instant paidAt,
        Instant fulfilledAt,
        Instant cancelledAt,
        String cancelReason,
        Long version,
        Instant createdAt,
        Instant updatedAt,
        List<OrderLineResponse> lines
) {
}
