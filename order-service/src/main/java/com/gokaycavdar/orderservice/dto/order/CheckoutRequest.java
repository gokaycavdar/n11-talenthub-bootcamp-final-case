package com.gokaycavdar.orderservice.dto.order;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CheckoutRequest(
        @NotBlank(message = "Shipping full name is required")
        @Size(max = 150, message = "Shipping full name must be at most 150 characters")
        String shippingFullName,

        @NotBlank(message = "Shipping address line is required")
        @Size(max = 255, message = "Shipping address line must be at most 255 characters")
        String shippingAddressLine,

        @NotBlank(message = "City is required")
        @Size(max = 100, message = "City must be at most 100 characters")
        String city,

        @NotBlank(message = "District is required")
        @Size(max = 100, message = "District must be at most 100 characters")
        String district,

        @NotBlank(message = "Postal code is required")
        @Size(max = 20, message = "Postal code must be at most 20 characters")
        String postalCode
) {
}
