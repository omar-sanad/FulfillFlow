package com.fulfillflow.order.model;

import java.time.Instant;

public record OrderStatusHistoryResponse(
        String fromStatus,
        String toStatus,
        String reason,
        String actor,
        Instant occurredAt
) {
}
