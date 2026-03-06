package com.fulfillflow.order.model;

import jakarta.validation.constraints.NotBlank;

public record ShippingAddressRequest(
        @NotBlank String fullName,
        @NotBlank String line1,
        String line2,
        @NotBlank String city,
        @NotBlank String postalCode,
        @NotBlank String country,
        String phone
) {
}
