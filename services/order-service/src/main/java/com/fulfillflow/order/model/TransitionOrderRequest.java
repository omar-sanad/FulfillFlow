package com.fulfillflow.order.model;

import jakarta.validation.constraints.NotBlank;

public record TransitionOrderRequest(
        @NotBlank String action,
        String reason
) {
}
