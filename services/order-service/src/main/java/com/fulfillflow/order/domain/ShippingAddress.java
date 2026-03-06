package com.fulfillflow.order.domain;

import jakarta.validation.constraints.NotBlank;

public record ShippingAddress(
        @NotBlank String fullName,
        @NotBlank String line1,
        String line2,
        @NotBlank String city,
        @NotBlank String postalCode,
        @NotBlank String country,
        String phone
) {
}
