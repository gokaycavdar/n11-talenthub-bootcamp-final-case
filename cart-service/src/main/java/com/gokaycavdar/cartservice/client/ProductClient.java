package com.gokaycavdar.cartservice.client;

import com.gokaycavdar.cartservice.dto.product.ProductDetailResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "PRODUCT-SERVICE", path = "/api/v1/products")
public interface ProductClient {

    @GetMapping("/{id}")
    ProductDetailResponse getProductById(@PathVariable Long id);
}
