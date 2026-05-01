package com.gokaycavdar.paymentservice.service.provider;

import java.math.BigDecimal;

public record PaymentProviderInitRequest(
        Long orderId,
        Long userId,
        BigDecimal paidPrice,
        String conversationId,
        String callbackUrl,
        String cardHolder,
        String cardNumber,
        String expireMonth,
        String expireYear,
        String cvc
) {
}
