package com.gokaycavdar.cartservice.dto.cart;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record CartResponse(
        Long userId,
        List<CartItemResponse> items,
        BigDecimal totalPrice,
        LocalDateTime updatedAt
) {
}
