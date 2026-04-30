package com.gokaycavdar.orderservice.client;

import com.gokaycavdar.orderservice.dto.cart.CartResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "CART-SERVICE", path = "/api/v1/carts")
public interface CartClient {

    @GetMapping("/me")
    CartResponse getMyCart(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader);
}
