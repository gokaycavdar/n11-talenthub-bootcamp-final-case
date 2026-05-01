package com.gokaycavdar.orderservice.dto.order;

import java.math.BigDecimal;

public record CheckoutResponse(
        Long orderId,
        String orderNumber,
        String status,
        BigDecimal totalAmount,
        String conversationId,
        String threeDsHtmlContent
) {
}
