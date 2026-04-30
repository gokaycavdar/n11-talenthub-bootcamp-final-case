package com.gokaycavdar.orderservice.dto.order;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrderResponse(
        Long id,
        String orderNumber,
        Long userId,
        String status,
        BigDecimal totalAmount,
        String paymentConversationId,
        String shippingFullName,
        String shippingAddressLine,
        String city,
        String district,
        String postalCode,
        LocalDateTime createdAt,
        List<OrderItemResponse> items
) {
}
