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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/v1/carts")
@RequiredArgsConstructor
@Tag(name = "Cart", description = "Authenticated shopping cart endpoints")
public class CartController {

    private final CartService cartService;

    @GetMapping("/me")
    @Operation(
            summary = "Get my cart",
            description = "Returns authenticated user's current cart",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<CartResponse> getMyCart(Authentication authentication) {
        return ResponseEntity.ok(cartService.getMyCart(getUserId(authentication)));
    }

    @PostMapping("/items")
    @Operation(
            summary = "Add item to cart",
            description = "Adds product to authenticated user's cart. If product already exists, quantity increases.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<CartResponse> addItem(
            Authentication authentication,
            @Valid @RequestBody AddCartItemRequest request
    ) {
        return ResponseEntity.ok(cartService.addItem(getUserId(authentication), request));
    }

    @PatchMapping("/items/{productId}")
    @Operation(
            summary = "Update cart item quantity",
            description = "Updates quantity of an existing cart item by product id",
            security = @SecurityRequirement(name = "bearerAuth")
    )
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
    @Operation(
            summary = "Remove item from cart",
            description = "Removes a cart item by product id",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<Void> removeItem(
            Authentication authentication,
            @PathVariable Long productId
    ) {
        cartService.removeItem(getUserId(authentication), productId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/me/clear")
    @Operation(
            summary = "Clear cart",
            description = "Deletes all items from authenticated user's cart",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<Void> clearCart(Authentication authentication) {
        cartService.clearCart(getUserId(authentication));
        return ResponseEntity.noContent().build();
    }

    private Long getUserId(Authentication authentication) {
        return Long.parseLong(authentication.getName());
    }
}
