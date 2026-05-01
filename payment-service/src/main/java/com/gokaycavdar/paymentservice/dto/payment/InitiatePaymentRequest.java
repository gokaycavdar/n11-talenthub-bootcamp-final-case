package com.gokaycavdar.paymentservice.dto.payment;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record InitiatePaymentRequest(
        @NotNull(message = "Order id is required")
        Long orderId,

        @NotNull(message = "Paid price is required")
        @DecimalMin(value = "0.01", message = "Paid price must be greater than 0")
        BigDecimal paidPrice,

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
