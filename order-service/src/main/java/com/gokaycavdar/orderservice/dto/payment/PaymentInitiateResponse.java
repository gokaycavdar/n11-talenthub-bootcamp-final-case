package com.gokaycavdar.orderservice.dto.payment;

public record PaymentInitiateResponse(
        String conversationId,
        String status,
        String threeDsHtmlContent
) {
}
