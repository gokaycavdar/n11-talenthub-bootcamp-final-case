package com.gokaycavdar.cartservice.dto.product;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ProductDetailResponse(
        Long id,
        String name,
        String description,
        String category,
        BigDecimal price,
        Integer stock,
        String imageUrl,
        Boolean active
) {
}
