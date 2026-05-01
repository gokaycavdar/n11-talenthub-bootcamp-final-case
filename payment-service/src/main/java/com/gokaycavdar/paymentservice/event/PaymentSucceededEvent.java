package com.gokaycavdar.paymentservice.event;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PaymentSucceededEvent(
        Long orderId,
        Long userId,
        String conversationId,
        BigDecimal paidPrice,
        String externalPaymentId,
        LocalDateTime paidAt
) {
}
