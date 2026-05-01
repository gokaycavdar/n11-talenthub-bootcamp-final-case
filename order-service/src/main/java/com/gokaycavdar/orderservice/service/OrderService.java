package com.gokaycavdar.orderservice.service;

import com.gokaycavdar.orderservice.client.CartClient;
import com.gokaycavdar.orderservice.client.PaymentClient;
import com.gokaycavdar.orderservice.dto.cart.CartItemClientResponse;
import com.gokaycavdar.orderservice.dto.cart.CartResponse;
import com.gokaycavdar.orderservice.dto.order.CheckoutRequest;
import com.gokaycavdar.orderservice.dto.order.CheckoutResponse;
import com.gokaycavdar.orderservice.dto.order.OrderResponse;
import com.gokaycavdar.orderservice.dto.payment.PaymentInitiateRequest;
import com.gokaycavdar.orderservice.dto.payment.PaymentInitiateResponse;
import com.gokaycavdar.orderservice.entity.Order;
import com.gokaycavdar.orderservice.entity.OrderItem;
import com.gokaycavdar.orderservice.entity.OrderStatus;
import com.gokaycavdar.orderservice.exception.BusinessException;
import com.gokaycavdar.orderservice.exception.ResourceNotFoundException;
import com.gokaycavdar.orderservice.mapper.OrderMapper;
import com.gokaycavdar.orderservice.repository.OrderRepository;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gokaycavdar.orderservice.client.UserClient;
import com.gokaycavdar.orderservice.dto.payment.PaymentBasketItemRequest;
import com.gokaycavdar.orderservice.dto.user.UserClientResponse;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    private final CartClient cartClient;
    private final PaymentClient paymentClient;
    private final UserClient userClient;


    @Transactional
    public CheckoutResponse checkout(Long userId, String authorizationHeader, CheckoutRequest request) {
        log.info("Checkout started. userId={}", userId);
        CartResponse cart = getCartOrThrow(authorizationHeader);

        if (cart.items() == null || cart.items().isEmpty()) {
            throw new BusinessException("Cart is empty");
        }

        UserClientResponse currentUser = getCurrentUserOrThrow(authorizationHeader);


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

        log.info("Order created with PENDING_PAYMENT status. orderId={}, userId={}, totalAmount={}",
                savedOrder.getId(), savedOrder.getUserId(), savedOrder.getTotalAmount());

        PaymentInitiateResponse paymentResponse = initiatePayment(savedOrder, cart, currentUser, authorizationHeader, request);



        log.info("3DS payment initiated for order. orderId={}, conversationId={}",
                savedOrder.getId(), paymentResponse.conversationId());

        savedOrder.setPaymentConversationId(paymentResponse.conversationId());
        Order updatedOrder = orderRepository.save(savedOrder);

        return new CheckoutResponse(
                updatedOrder.getId(),
                updatedOrder.getOrderNumber(),
                updatedOrder.getStatus().name(),
                updatedOrder.getTotalAmount(),
                paymentResponse.conversationId(),
                paymentResponse.threeDsHtmlContent()
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
            log.error("Cart service call failed during checkout", ex);
            throw new BusinessException("Cart service is unavailable");
        }
    }

    private PaymentInitiateResponse initiatePayment(
            Order order,
            CartResponse cart,
            UserClientResponse currentUser,
            String authorizationHeader,
            CheckoutRequest request
    ) {
        try {
            return paymentClient.initiateThreeDsPayment(
                    authorizationHeader,
                    new PaymentInitiateRequest(
                            order.getId(),
                            order.getTotalAmount(),
                            order.getTotalAmount(),
                            currentUser.firstName(),
                            currentUser.lastName(),
                            currentUser.email(),
                            request.shippingFullName(),
                            request.shippingAddressLine(),
                            request.city(),
                            request.district(),
                            request.postalCode(),
                            request.cardHolder(),
                            request.cardNumber(),
                            request.expireMonth(),
                            request.expireYear(),
                            request.cvc(),
                            cart.items().stream()
                                    .map(item -> new PaymentBasketItemRequest(
                                            item.productId(),
                                            item.productName(),
                                            item.unitPrice(),
                                            item.quantity(),
                                            item.lineTotal()
                                    ))
                                    .toList()
                    )
            );
        } catch (FeignException ex) {
            log.error("Payment service call failed during checkout. orderId={}", order.getId(), ex);
            throw new BusinessException("Payment service is unavailable");
        }
    }


    private String generateOrderNumber() {
        return "ORD-" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();
    }

    private UserClientResponse getCurrentUserOrThrow(String authorizationHeader) {
        try {
            return userClient.getCurrentUser(authorizationHeader);
        } catch (FeignException ex) {
            log.error("User service call failed during checkout", ex);
            throw new BusinessException("User service is unavailable");
        }
    }
}