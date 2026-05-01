package com.gokaycavdar.paymentservice.dto.payment;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;

public record InitiatePaymentRequest(
        @NotNull(message = "Order id is required")
        Long orderId,

        @NotNull(message = "Price is required")
        @DecimalMin(value = "0.01", message = "Price must be greater than 0")
        BigDecimal price,

        @NotNull(message = "Paid price is required")
        @DecimalMin(value = "0.01", message = "Paid price must be greater than 0")
        BigDecimal paidPrice,

        @NotBlank(message = "Buyer first name is required")
        @Size(max = 100, message = "Buyer first name must be at most 100 characters")
        String buyerFirstName,

        @NotBlank(message = "Buyer last name is required")
        @Size(max = 100, message = "Buyer last name must be at most 100 characters")
        String buyerLastName,

        @NotBlank(message = "Buyer email is required")
        @Email(message = "Buyer email must be valid")
        String buyerEmail,

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
        String cvc,

        @NotEmpty(message = "Basket items are required")
        List<@Valid PaymentBasketItemRequest> basketItems
) {
}
