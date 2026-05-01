package com.gokaycavdar.orderservice.dto.order;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
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
        String postalCode,

        @NotBlank(message = "Card holder is required")
        @Size(max = 100, message = "Card holder must be at most 100 characters")
        String cardHolder,

        @NotBlank(message = "Card number is required")
        @Pattern(regexp = "\\d{12,19}", message = "Card number must contain 12 to 19 digits")
        String cardNumber,

        @NotBlank(message = "Expire month is required")
        @Pattern(regexp = "^(0[1-9]|1[0-2])$", message = "Expire month must be between 01 and 12")
        String expireMonth,

        @NotBlank(message = "Expire year is required")
        @Pattern(regexp = "\\d{2,4}", message = "Expire year must be 2 or 4 digits")
        String expireYear,

        @NotBlank(message = "CVC is required")
        @Pattern(regexp = "\\d{3,4}", message = "CVC must be 3 or 4 digits")
        String cvc
) {
}
