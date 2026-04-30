package com.gokaycavdar.cartservice.controller;

import com.gokaycavdar.cartservice.dto.cart.AddCartItemRequest;
import com.gokaycavdar.cartservice.dto.cart.CartResponse;
import com.gokaycavdar.cartservice.dto.cart.UpdateCartItemQuantityRequest;
import com.gokaycavdar.cartservice.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/carts")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @GetMapping("/me")
    public ResponseEntity<CartResponse> getMyCart(Authentication authentication) {
        return ResponseEntity.ok(cartService.getMyCart(getUserId(authentication)));
    }

    @PostMapping("/items")
    public ResponseEntity<CartResponse> addItem(
            Authentication authentication,
            @Valid @RequestBody AddCartItemRequest request
    ) {
        return ResponseEntity.ok(cartService.addItem(getUserId(authentication), request));
    }

    @PatchMapping("/items/{productId}")
    public ResponseEntity<CartResponse> updateItemQuantity(
            Authentication authentication,
            @PathVariable Long productId,
            @Valid @RequestBody UpdateCartItemQuantityRequest request
    ) {
        return ResponseEntity.ok(
                cartService.updateItemQuantity(getUserId(authentication), productId, request)
        );
    }

    @DeleteMapping("/items/{productId}")
    public ResponseEntity<Void> removeItem(
            Authentication authentication,
            @PathVariable Long productId
    ) {
        cartService.removeItem(getUserId(authentication), productId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/me/clear")
    public ResponseEntity<Void> clearCart(Authentication authentication) {
        cartService.clearCart(getUserId(authentication));
        return ResponseEntity.noContent().build();
    }

    private Long getUserId(Authentication authentication) {
        return Long.parseLong(authentication.getName());
    }
}
