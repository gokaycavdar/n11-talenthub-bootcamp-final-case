package com.gokaycavdar.paymentservice.dto.payment;

public record InitiatePaymentResponse(
        String conversationId,
        String status,
        String threeDsHtmlContent
) {
}
