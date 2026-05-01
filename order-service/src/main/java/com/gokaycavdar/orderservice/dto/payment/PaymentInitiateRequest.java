package com.gokaycavdar.orderservice.dto.payment;

import java.math.BigDecimal;
import java.util.List;

public record PaymentInitiateRequest(
        Long orderId,
        BigDecimal price,
        BigDecimal paidPrice,

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
