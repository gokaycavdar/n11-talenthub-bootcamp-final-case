package com.gokaycavdar.orderservice.dto.cart;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record CartResponse(
        Long userId,
        List<CartItemClientResponse> items,
        BigDecimal totalPrice,
        LocalDateTime updatedAt
) {
}
