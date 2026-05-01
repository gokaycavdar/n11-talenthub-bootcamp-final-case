package com.gokaycavdar.orderservice.dto.payment;

import java.math.BigDecimal;

public record PaymentBasketItemRequest(
        Long productId,
        String productName,
        BigDecimal unitPrice,
        Integer quantity,
        BigDecimal lineTotal
) {
}
