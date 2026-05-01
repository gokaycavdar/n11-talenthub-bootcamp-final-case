package com.gokaycavdar.orderservice.event;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PaymentSucceededEvent(
        Long orderId,
        Long userId,
        String conversationId,
        String correlationId,
        BigDecimal paidPrice,
        String externalPaymentId,
        LocalDateTime paidAt
) {
}
