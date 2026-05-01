package com.gokaycavdar.notificationservice.event;

import java.time.LocalDateTime;

public record PaymentFailedEvent(
        Long orderId,
        Long userId,
        String conversationId,
        String correlationId,
        String reason,
        LocalDateTime failedAt
) {
}

