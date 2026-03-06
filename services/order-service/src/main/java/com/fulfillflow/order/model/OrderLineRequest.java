package com.fulfillflow.order.model;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record OrderLineRequest(
        @NotNull UUID productId,
        @NotBlank String sku,
        @NotBlank String name,
        @NotNull @Min(0) Long unitPriceCents,
        @NotNull @Min(1) Integer quantity
) {
}
