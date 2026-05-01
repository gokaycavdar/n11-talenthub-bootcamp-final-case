package com.gokaycavdar.paymentservice.service.provider;

import com.gokaycavdar.paymentservice.dto.payment.PaymentBasketItemRequest;

import java.math.BigDecimal;
import java.util.List;

public record PaymentProviderInitRequest(
        Long orderId,
        Long userId,
        BigDecimal price,
        BigDecimal paidPrice,
        String conversationId,
        String callbackUrl,

        String buyerFirstName,
        String buyerLastName,
        String buyerEmail,

        String shippingFullName,
        String shippingAddressLine,
        String city,
        String district,
        String postalCode,

        String cardHolder,
        String cardNumber,
        String expireMonth,
        String expireYear,
        String cvc,

        List<PaymentBasketItemRequest> basketItems
) {
}
