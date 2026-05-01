package com.gokaycavdar.cartservice.service;

import com.gokaycavdar.cartservice.client.ProductClient;
import com.gokaycavdar.cartservice.dto.cart.AddCartItemRequest;
import com.gokaycavdar.cartservice.dto.cart.CartResponse;
import com.gokaycavdar.cartservice.dto.cart.UpdateCartItemQuantityRequest;
import com.gokaycavdar.cartservice.dto.product.ProductDetailResponse;
import com.gokaycavdar.cartservice.exception.BusinessException;
import com.gokaycavdar.cartservice.exception.ResourceNotFoundException;
import com.gokaycavdar.cartservice.model.Cart;
import com.gokaycavdar.cartservice.model.CartItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import java.time.Duration;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock
    private RedisTemplate<String, Cart> redisTemplate;

    @Mock
    private ValueOperations<String, Cart> valueOperations;

    @Mock
    private ProductClient productClient;

    @InjectMocks
    private CartService cartService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(cartService, "cartTtlSeconds", 604800L);
    }

    @Test
    void getMyCart_shouldReturnEmptyCart_whenRedisHasNoCart() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("cart:user:1")).thenReturn(null);

        CartResponse response = cartService.getMyCart(1L);

        assertEquals(1L, response.userId());
        assertTrue(response.items().isEmpty());
        assertEquals(0, response.totalPrice().compareTo(BigDecimal.ZERO));
        assertNotNull(response.updatedAt());
    }

    @Test
    void addItem_shouldCreateNewCartItem() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("cart:user:1")).thenReturn(null);
        when(productClient.getProductById(1L)).thenReturn(activeProduct(1L, "iPhone 15", 5, "1000.00"));

        CartResponse response = cartService.addItem(1L, new AddCartItemRequest(1L, 2));

        assertEquals(1, response.items().size());
        assertEquals(2, response.items().get(0).quantity());
        assertEquals(0, response.totalPrice().compareTo(new BigDecimal("2000.00")));

        verify(valueOperations).set(eq("cart:user:1"), any(Cart.class), eq(Duration.ofSeconds(604800L)));
    }

    @Test
    void addItem_shouldIncreaseQuantity_whenSameProductAlreadyExists() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        Cart cart = Cart.builder()
                .userId(1L)
                .items(new ArrayList<>())
                .totalPrice(BigDecimal.valueOf(1000))
                .build();

        cart.getItems().add(
                CartItem.builder()
                        .productId(1L)
                        .productName("iPhone 15")
                        .unitPrice(BigDecimal.valueOf(1000))
                        .quantity(1)
                        .lineTotal(BigDecimal.valueOf(1000))
                        .imageUrl("/images/products/iphone-15.jpg")
                        .build()
        );

        when(valueOperations.get("cart:user:1")).thenReturn(cart);
        when(productClient.getProductById(1L)).thenReturn(activeProduct(1L, "iPhone 15", 5, "1000.00"));

        CartResponse response = cartService.addItem(1L, new AddCartItemRequest(1L, 2));

        assertEquals(1, response.items().size());
        assertEquals(3, response.items().get(0).quantity());
        assertEquals(0, response.totalPrice().compareTo(new BigDecimal("3000.00")));
    }

    @Test
    void addItem_shouldThrowBusinessException_whenStockInsufficient() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("cart:user:1")).thenReturn(null);
        when(productClient.getProductById(1L)).thenReturn(activeProduct(1L, "iPhone 15", 1, "1000.00"));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> cartService.addItem(1L, new AddCartItemRequest(1L, 2))
        );

        assertEquals("Insufficient stock for product: iPhone 15", exception.getMessage());
    }

    @Test
    void updateItemQuantity_shouldUpdateExistingItem() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        Cart cart = Cart.builder()
                .userId(1L)
                .items(new ArrayList<>())
                .totalPrice(BigDecimal.valueOf(1000))
                .build();

        cart.getItems().add(
                CartItem.builder()
                        .productId(1L)
                        .productName("iPhone 15")
                        .unitPrice(BigDecimal.valueOf(1000))
                        .quantity(1)
                        .lineTotal(BigDecimal.valueOf(1000))
                        .imageUrl("/images/products/iphone-15.jpg")
                        .build()
        );

        when(valueOperations.get("cart:user:1")).thenReturn(cart);
        when(productClient.getProductById(1L)).thenReturn(activeProduct(1L, "iPhone 15", 10, "1000.00"));

        CartResponse response = cartService.updateItemQuantity(
                1L,
                1L,
                new UpdateCartItemQuantityRequest(4)
        );

        assertEquals(4, response.items().get(0).quantity());
        assertEquals(0, response.totalPrice().compareTo(new BigDecimal("4000.00")));
    }

    @Test
    void removeItem_shouldDeleteCartKey_whenLastItemRemoved() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        Cart cart = Cart.builder()
                .userId(1L)
                .items(new ArrayList<>())
                .totalPrice(BigDecimal.valueOf(1000))
                .build();

        cart.getItems().add(
                CartItem.builder()
                        .productId(1L)
                        .productName("iPhone 15")
                        .unitPrice(BigDecimal.valueOf(1000))
                        .quantity(1)
                        .lineTotal(BigDecimal.valueOf(1000))
                        .build()
        );

        when(valueOperations.get("cart:user:1")).thenReturn(cart);

        cartService.removeItem(1L, 1L);

        verify(redisTemplate).delete("cart:user:1");
    }

    @Test
    void removeItem_shouldThrow_whenItemNotFound() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        Cart cart = Cart.builder()
                .userId(1L)
                .items(new ArrayList<>())
                .totalPrice(BigDecimal.ZERO)
                .build();

        when(valueOperations.get("cart:user:1")).thenReturn(cart);

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> cartService.removeItem(1L, 999L)
        );

        assertEquals("Cart item not found", exception.getMessage());
    }

    @Test
    void clearCart_shouldDeleteRedisKey() {
        cartService.clearCart(1L);

        verify(redisTemplate).delete("cart:user:1");
    }

    private ProductDetailResponse activeProduct(Long id, String name, int stock, String price) {
        return new ProductDetailResponse(
                id,
                name,
                "Description",
                "Category",
                new BigDecimal(price),
                stock,
                "/images/products/sample.jpg",
                true
        );
    }
}
