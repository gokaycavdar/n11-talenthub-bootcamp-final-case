package com.gokaycavdar.orderservice.service;

import com.gokaycavdar.orderservice.client.CartClient;
import com.gokaycavdar.orderservice.client.PaymentClient;
import com.gokaycavdar.orderservice.dto.cart.CartItemClientResponse;
import com.gokaycavdar.orderservice.dto.cart.CartResponse;
import com.gokaycavdar.orderservice.dto.order.CheckoutRequest;
import com.gokaycavdar.orderservice.dto.order.CheckoutResponse;
import com.gokaycavdar.orderservice.dto.order.OrderItemResponse;
import com.gokaycavdar.orderservice.dto.order.OrderResponse;
import com.gokaycavdar.orderservice.dto.payment.PaymentInitiateResponse;
import com.gokaycavdar.orderservice.entity.Order;
import com.gokaycavdar.orderservice.entity.OrderStatus;
import com.gokaycavdar.orderservice.exception.BusinessException;
import com.gokaycavdar.orderservice.exception.ResourceNotFoundException;
import com.gokaycavdar.orderservice.mapper.OrderMapper;
import com.gokaycavdar.orderservice.repository.OrderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderMapper orderMapper;

    @Mock
    private CartClient cartClient;

    @Mock
    private PaymentClient paymentClient;

    @InjectMocks
    private OrderService orderService;

    @Test
    void checkout_shouldCreateOrderAndInitiatePayment() {
        CartResponse cartResponse = new CartResponse(
                1L,
                List.of(
                        new CartItemClientResponse(
                                10L,
                                "iPhone 15",
                                new BigDecimal("1000.00"),
                                2,
                                new BigDecimal("2000.00"),
                                "/images/products/iphone-15.jpg"
                        )
                ),
                new BigDecimal("2000.00"),
                LocalDateTime.now()
        );

        CheckoutRequest request = new CheckoutRequest(
                "Gokay Cavdar",
                "Ataturk Caddesi No 10",
                "Istanbul",
                "Kadikoy",
                "34710",
                "Gokay Cavdar",
                "5555444433331111",
                "12",
                "30",
                "123"
        );

        when(cartClient.getMyCart("Bearer token")).thenReturn(cartResponse);
        when(orderRepository.save(any(Order.class)))
                .thenAnswer(invocation -> {
                    Order order = invocation.getArgument(0);
                    if (order.getId() == null) {
                        order.setId(1L);
                    }
                    return order;
                });
        when(paymentClient.initiateThreeDsPayment(eq("Bearer token"), any()))
                .thenReturn(new PaymentInitiateResponse(
                        "conv-123",
                        "INITIATED",
                        "<html>3ds</html>"
                ));

        CheckoutResponse response = orderService.checkout(1L, "Bearer token", request);

        assertEquals(1L, response.orderId());
        assertEquals(OrderStatus.PENDING_PAYMENT.name(), response.status());
        assertEquals("conv-123", response.conversationId());
        assertEquals("<html>3ds</html>", response.threeDsHtmlContent());
        assertEquals(0, response.totalAmount().compareTo(new BigDecimal("2000.00")));

        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository, atLeastOnce()).save(orderCaptor.capture());

        Order savedOrder = orderCaptor.getAllValues().get(0);
        assertEquals(1L, savedOrder.getUserId());
        assertEquals(OrderStatus.PENDING_PAYMENT, savedOrder.getStatus());
        assertEquals(1, savedOrder.getItems().size());
        assertEquals("iPhone 15", savedOrder.getItems().get(0).getProductName());
    }

    @Test
    void checkout_shouldThrowBusinessException_whenCartIsEmpty() {
        CartResponse emptyCart = new CartResponse(
                1L,
                List.of(),
                BigDecimal.ZERO,
                LocalDateTime.now()
        );

        CheckoutRequest request = new CheckoutRequest(
                "Gokay Cavdar",
                "Ataturk Caddesi No 10",
                "Istanbul",
                "Kadikoy",
                "34710",
                "Gokay Cavdar",
                "5555444433331111",
                "12",
                "30",
                "123"
        );

        when(cartClient.getMyCart("Bearer token")).thenReturn(emptyCart);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> orderService.checkout(1L, "Bearer token", request)
        );

        assertEquals("Cart is empty", exception.getMessage());
        verify(orderRepository, never()).save(any());
    }

    @Test
    void getMyOrders_shouldReturnMappedOrders() {
        Order order = Order.builder()
                .id(1L)
                .orderNumber("ORD-123")
                .userId(1L)
                .status(OrderStatus.PAID)
                .totalAmount(new BigDecimal("2000.00"))
                .createdAt(LocalDateTime.now())
                .build();

        OrderResponse orderResponse = new OrderResponse(
                1L,
                "ORD-123",
                1L,
                "PAID",
                new BigDecimal("2000.00"),
                "conv-123",
                "Gokay Cavdar",
                "Ataturk Caddesi No 10",
                "Istanbul",
                "Kadikoy",
                "34710",
                LocalDateTime.now(),
                List.of()
        );

        when(orderRepository.findByUserIdOrderByCreatedAtDesc(1L)).thenReturn(List.of(order));
        when(orderMapper.toOrderResponse(order)).thenReturn(orderResponse);

        List<OrderResponse> result = orderService.getMyOrders(1L);

        assertEquals(1, result.size());
        assertEquals("ORD-123", result.get(0).orderNumber());
    }

    @Test
    void getOrderById_shouldReturnMappedOrder() {
        Order order = Order.builder()
                .id(1L)
                .orderNumber("ORD-123")
                .userId(1L)
                .status(OrderStatus.PAID)
                .totalAmount(new BigDecimal("2000.00"))
                .createdAt(LocalDateTime.now())
                .build();

        OrderResponse orderResponse = new OrderResponse(
                1L,
                "ORD-123",
                1L,
                "PAID",
                new BigDecimal("2000.00"),
                "conv-123",
                "Gokay Cavdar",
                "Ataturk Caddesi No 10",
                "Istanbul",
                "Kadikoy",
                "34710",
                LocalDateTime.now(),
                List.of()
        );

        when(orderRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(order));
        when(orderMapper.toOrderResponse(order)).thenReturn(orderResponse);

        OrderResponse result = orderService.getOrderById(1L, 1L);

        assertEquals("ORD-123", result.orderNumber());
    }

    @Test
    void getOrderById_shouldThrow_whenOrderNotFound() {
        when(orderRepository.findByIdAndUserId(99L, 1L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> orderService.getOrderById(1L, 99L)
        );

        assertEquals("Order not found", exception.getMessage());
    }
}
