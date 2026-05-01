package com.gokaycavdar.paymentservice.dto.payment;

public record PaymentCallbackResponse(
        String conversationId,
        String status,
        String message
) {
}
