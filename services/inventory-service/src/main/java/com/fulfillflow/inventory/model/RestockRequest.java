package com.fulfillflow.inventory.model;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record RestockRequest(
        @NotNull @Min(0) Integer quantity
) {
}
