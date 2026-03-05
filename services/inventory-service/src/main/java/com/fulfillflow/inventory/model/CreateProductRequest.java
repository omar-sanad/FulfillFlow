package com.fulfillflow.inventory.model;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateProductRequest(
        @NotBlank @Size(max = 64) String sku,
        @NotBlank @Size(max = 256) String name,
        @Size(max = 5000) String description,
        @NotNull @Min(0) Long priceCents,
        @NotBlank @Size(max = 3) String currency,
        @Min(0) Integer weightGrams
) {
}
