package com.gokaycavdar.cartservice.service;

import com.gokaycavdar.cartservice.client.ProductClient;
import com.gokaycavdar.cartservice.dto.cart.AddCartItemRequest;
import com.gokaycavdar.cartservice.dto.cart.CartItemResponse;
import com.gokaycavdar.cartservice.dto.cart.CartResponse;
import com.gokaycavdar.cartservice.dto.cart.UpdateCartItemQuantityRequest;
import com.gokaycavdar.cartservice.dto.product.ProductDetailResponse;
import com.gokaycavdar.cartservice.exception.BusinessException;
import com.gokaycavdar.cartservice.exception.ResourceNotFoundException;
import com.gokaycavdar.cartservice.model.Cart;
import com.gokaycavdar.cartservice.model.CartItem;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CartService {

    private final RedisTemplate<String, Cart> redisTemplate;
    private final ProductClient productClient;

    @Value("${cart.ttl-seconds}")
    private long cartTtlSeconds;

    public CartResponse getMyCart(Long userId) {
        return toResponse(getOrCreateCart(userId));
    }

    public CartResponse addItem(Long userId, AddCartItemRequest request) {
        ProductDetailResponse product = getProductOrThrow(request.productId());
        validateProductAvailability(product);

        Cart cart = getOrCreateCart(userId);
        CartItem existingItem = findItem(cart, request.productId()).orElse(null);

        int newQuantity = request.quantity();
        if (existingItem != null) {
            newQuantity += existingItem.getQuantity();
        }

        validateStock(product, newQuantity);

        if (existingItem == null) {
            cart.getItems().add(
                    CartItem.builder()
                            .productId(product.id())
                            .productName(product.name())
                            .unitPrice(product.price())
                            .quantity(request.quantity())
                            .imageUrl(product.imageUrl())
                            .build()
            );
        } else {
            existingItem.setProductName(product.name());
            existingItem.setUnitPrice(product.price());
            existingItem.setQuantity(newQuantity);
            existingItem.setImageUrl(product.imageUrl());
        }

        recalculate(cart);
        saveCart(cart);
        return toResponse(cart);
    }

    public CartResponse updateItemQuantity(Long userId, Long productId, UpdateCartItemQuantityRequest request) {
        ProductDetailResponse product = getProductOrThrow(productId);
        validateProductAvailability(product);
        validateStock(product, request.quantity());

        Cart cart = getOrCreateCart(userId);
        CartItem item = findItem(cart, productId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found"));

        item.setProductName(product.name());
        item.setUnitPrice(product.price());
        item.setQuantity(request.quantity());
        item.setImageUrl(product.imageUrl());

        recalculate(cart);
        saveCart(cart);
        return toResponse(cart);
    }

    public void removeItem(Long userId, Long productId) {
        Cart cart = getOrCreateCart(userId);

        boolean removed = cart.getItems().removeIf(item -> item.getProductId().equals(productId));
        if (!removed) {
            throw new ResourceNotFoundException("Cart item not found");
        }

        if (cart.getItems().isEmpty()) {
            redisTemplate.delete(getCartKey(userId));
            return;
        }

        recalculate(cart);
        saveCart(cart);
    }

    public void clearCart(Long userId) {
        redisTemplate.delete(getCartKey(userId));
    }

    private Cart getOrCreateCart(Long userId) {
        Cart cart = redisTemplate.opsForValue().get(getCartKey(userId));

        if (cart == null) {
            return Cart.builder()
                    .userId(userId)
                    .totalPrice(BigDecimal.ZERO)
                    .updatedAt(LocalDateTime.now())
                    .build();
        }

        if (cart.getItems() == null) {
            cart.setItems(new java.util.ArrayList<>());
        }

        return cart;
    }

    private ProductDetailResponse getProductOrThrow(Long productId) {
        try {
            return productClient.getProductById(productId);
        } catch (FeignException.NotFound ex) {
            throw new ResourceNotFoundException("Product not found");
        } catch (FeignException ex) {
            throw new BusinessException("Product service is unavailable");
        }
    }

    private void validateProductAvailability(ProductDetailResponse product) {
        if (!Boolean.TRUE.equals(product.active())) {
            throw new BusinessException("Product is not active");
        }
    }

    private void validateStock(ProductDetailResponse product, int quantity) {
        if (product.stock() == null || quantity > product.stock()) {
            throw new BusinessException("Insufficient stock for product: " + product.name());
        }
    }

    private Optional<CartItem> findItem(Cart cart, Long productId) {
        return cart.getItems().stream()
                .filter(item -> item.getProductId().equals(productId))
                .findFirst();
    }

    private void recalculate(Cart cart) {
        BigDecimal totalPrice = BigDecimal.ZERO;

        for (CartItem item : cart.getItems()) {
            BigDecimal lineTotal = item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
            item.setLineTotal(lineTotal);
            totalPrice = totalPrice.add(lineTotal);
        }

        cart.setTotalPrice(totalPrice);
        cart.setUpdatedAt(LocalDateTime.now());
    }

    private void saveCart(Cart cart) {
        redisTemplate.opsForValue().set(
                getCartKey(cart.getUserId()),
                cart,
                Duration.ofSeconds(cartTtlSeconds)
        );
    }

    private String getCartKey(Long userId) {
        return "cart:user:" + userId;
    }

    private CartResponse toResponse(Cart cart) {
        return new CartResponse(
                cart.getUserId(),
                cart.getItems().stream()
                        .map(item -> new CartItemResponse(
                                item.getProductId(),
                                item.getProductName(),
                                item.getUnitPrice(),
                                item.getQuantity(),
                                item.getLineTotal(),
                                item.getImageUrl()
                        ))
                        .toList(),
                cart.getTotalPrice(),
                cart.getUpdatedAt()
        );
    }
}
