package com.gokaycavdar.orderservice.service;

import com.gokaycavdar.orderservice.entity.Order;
import com.gokaycavdar.orderservice.entity.OrderStatus;
import com.gokaycavdar.orderservice.event.PaymentFailedEvent;
import com.gokaycavdar.orderservice.event.PaymentSucceededEvent;
import com.gokaycavdar.orderservice.repository.OrderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentEventListenerTest {

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private PaymentEventListener paymentEventListener;

    @Test
    void handlePaymentSucceeded_shouldMarkOrderAsPaid() {
        Order order = Order.builder()
                .id(1L)
                .status(OrderStatus.PENDING_PAYMENT)
                .build();

        PaymentSucceededEvent event = new PaymentSucceededEvent(
                1L,
                1L,
                "conv-123",
                "corr-123",
                new BigDecimal("2000.00"),
                "MOCKPAY123",
                LocalDateTime.now()
        );

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        paymentEventListener.handlePaymentSucceeded(event);

        assertEquals(OrderStatus.PAID, order.getStatus());
        assertEquals("conv-123", order.getPaymentConversationId());
        verify(orderRepository).save(order);
    }

    @Test
    void handlePaymentFailed_shouldMarkOrderAsPaymentFailed() {
        Order order = Order.builder()
                .id(1L)
                .status(OrderStatus.PENDING_PAYMENT)
                .build();

        PaymentFailedEvent event = new PaymentFailedEvent(
                1L,
                1L,
                "conv-999",
                "corr-999",
                "Mock payment declined",
                LocalDateTime.now()
        );

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        paymentEventListener.handlePaymentFailed(event);

        assertEquals(OrderStatus.PAYMENT_FAILED, order.getStatus());
        assertEquals("conv-999", order.getPaymentConversationId());
        verify(orderRepository).save(order);
    }

    @Test
    void handlePaymentSucceeded_shouldDoNothing_whenOrderNotFound() {
        PaymentSucceededEvent event = new PaymentSucceededEvent(
                1L,
                1L,
                "conv-123",
                "corr-123",
                new BigDecimal("2000.00"),
                "MOCKPAY123",
                LocalDateTime.now()
        );

        when(orderRepository.findById(1L)).thenReturn(Optional.empty());

        paymentEventListener.handlePaymentSucceeded(event);

        verify(orderRepository, never()).save(any());
    }

    @Test
    void handlePaymentFailed_shouldDoNothing_whenOrderNotPendingPayment() {
        Order order = Order.builder()
                .id(1L)
                .status(OrderStatus.PAID)
                .build();

        PaymentFailedEvent event = new PaymentFailedEvent(
                1L,
                1L,
                "conv-999",
                "corr-999",
                "Mock payment declined",
                LocalDateTime.now()
        );

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        paymentEventListener.handlePaymentFailed(event);

        verify(orderRepository, never()).save(any());
    }
}
