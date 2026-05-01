package com.gokaycavdar.orderservice.dto.payment;

import java.math.BigDecimal;

public record PaymentInitiateRequest(
        Long orderId,
        BigDecimal paidPrice,
        String cardHolder,
        String cardNumber,
        String expireMonth,
        String expireYear,
        String cvc
) {
}
