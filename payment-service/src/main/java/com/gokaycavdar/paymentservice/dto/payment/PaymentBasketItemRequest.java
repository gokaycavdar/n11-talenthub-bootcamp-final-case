package com.gokaycavdar.paymentservice.dto.payment;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record PaymentBasketItemRequest(
        @NotNull(message = "Product id is required")
        Long productId,

        @NotBlank(message = "Product name is required")
        String productName,

        @NotNull(message = "Unit price is required")
        @DecimalMin(value = "0.01", message = "Unit price must be greater than 0")
        BigDecimal unitPrice,

        @NotNull(message = "Quantity is required")
        Integer quantity,

        @NotNull(message = "Line total is required")
        @DecimalMin(value = "0.01", message = "Line total must be greater than 0")
        BigDecimal lineTotal
) {
}
