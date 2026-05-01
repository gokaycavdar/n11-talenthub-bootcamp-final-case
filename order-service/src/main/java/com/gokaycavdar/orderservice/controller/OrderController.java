package com.gokaycavdar.orderservice.controller;

import com.gokaycavdar.orderservice.dto.order.CheckoutRequest;
import com.gokaycavdar.orderservice.dto.order.CheckoutResponse;
import com.gokaycavdar.orderservice.dto.order.OrderResponse;
import com.gokaycavdar.orderservice.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;


import java.util.List;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
@Tag(name = "Orders", description = "Checkout and order history endpoints")
public class OrderController {

    private final OrderService orderService;

    @PostMapping("/checkout")
    @Operation(
            summary = "Checkout",
            description = "Creates an order with PENDING_PAYMENT status and starts 3DS payment flow",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<CheckoutResponse> checkout(
            Authentication authentication,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader,
            @Valid @RequestBody CheckoutRequest request
    ) {
        return ResponseEntity.ok(
                orderService.checkout(getUserId(authentication), authorizationHeader, request)
        );
    }

    @GetMapping("/me")
    @Operation(
            summary = "List my orders",
            description = "Returns authenticated user's orders",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<List<OrderResponse>> getMyOrders(Authentication authentication) {
        return ResponseEntity.ok(orderService.getMyOrders(getUserId(authentication)));
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Order detail",
            description = "Returns authenticated user's order detail by id",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<OrderResponse> getOrderById(
            Authentication authentication,
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(orderService.getOrderById(getUserId(authentication), id));
    }

    private Long getUserId(Authentication authentication) {
        return Long.parseLong(authentication.getName());
    }
}
