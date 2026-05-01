package com.gokaycavdar.paymentservice.event;

import java.time.LocalDateTime;

public record PaymentFailedEvent(
        Long orderId,
        Long userId,
        String conversationId,
        String reason,
        LocalDateTime failedAt
) {
}
