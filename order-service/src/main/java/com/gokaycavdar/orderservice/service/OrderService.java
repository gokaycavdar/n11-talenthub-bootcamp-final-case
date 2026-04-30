package com.gokaycavdar.orderservice.service;

import com.gokaycavdar.orderservice.client.CartClient;
import com.gokaycavdar.orderservice.dto.cart.CartItemClientResponse;
import com.gokaycavdar.orderservice.dto.cart.CartResponse;
import com.gokaycavdar.orderservice.dto.order.CheckoutRequest;
import com.gokaycavdar.orderservice.dto.order.CheckoutResponse;
import com.gokaycavdar.orderservice.dto.order.OrderResponse;
import com.gokaycavdar.orderservice.entity.Order;
import com.gokaycavdar.orderservice.entity.OrderItem;
import com.gokaycavdar.orderservice.entity.OrderStatus;
import com.gokaycavdar.orderservice.exception.BusinessException;
import com.gokaycavdar.orderservice.exception.ResourceNotFoundException;
import com.gokaycavdar.orderservice.mapper.OrderMapper;
import com.gokaycavdar.orderservice.repository.OrderRepository;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    private final CartClient cartClient;

    @Transactional
    public CheckoutResponse checkout(Long userId, String authorizationHeader, CheckoutRequest request) {
        CartResponse cart = getCartOrThrow(authorizationHeader);

        if (cart.items() == null || cart.items().isEmpty()) {
            throw new BusinessException("Cart is empty");
        }

        Order order = Order.builder()
                .orderNumber(generateOrderNumber())
                .userId(userId)
                .status(OrderStatus.PENDING_PAYMENT)
                .totalAmount(cart.totalPrice())
                .shippingFullName(request.shippingFullName())
                .shippingAddressLine(request.shippingAddressLine())
                .city(request.city())
                .district(request.district())
                .postalCode(request.postalCode())
                .build();

        for (CartItemClientResponse item : cart.items()) {
            order.addItem(
                    OrderItem.builder()
                            .productId(item.productId())
                            .productName(item.productName())
                            .unitPrice(item.unitPrice())
                            .quantity(item.quantity())
                            .lineTotal(item.lineTotal())
                            .build()
            );
        }

        Order savedOrder = orderRepository.save(order);

        return new CheckoutResponse(
                savedOrder.getId(),
                savedOrder.getOrderNumber(),
                savedOrder.getStatus().name(),
                savedOrder.getTotalAmount()
        );
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getMyOrders(Long userId) {
        return orderRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(orderMapper::toOrderResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrderById(Long userId, Long orderId) {
        Order order = orderRepository.findByIdAndUserId(orderId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        return orderMapper.toOrderResponse(order);
    }

    private CartResponse getCartOrThrow(String authorizationHeader) {
        try {
            return cartClient.getMyCart(authorizationHeader);
        } catch (FeignException ex) {
            throw new BusinessException("Cart service is unavailable");
        }
    }

    private String generateOrderNumber() {
        return "ORD-" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();
    }
}
