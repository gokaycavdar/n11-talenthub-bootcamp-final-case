package com.gokaycavdar.orderservice.service;

import com.gokaycavdar.orderservice.entity.Order;
import com.gokaycavdar.orderservice.entity.OrderStatus;
import com.gokaycavdar.orderservice.event.PaymentFailedEvent;
import com.gokaycavdar.orderservice.event.PaymentSucceededEvent;
import com.gokaycavdar.orderservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentEventListener {

    private final OrderRepository orderRepository;

    @Transactional
    @RabbitListener(queues = "${order.payment.success-queue}")
    public void handlePaymentSucceeded(PaymentSucceededEvent event) {
        putCorrelationId(event.correlationId());

        try {
            log.info("Payment success event received. orderId={}, userId={}, conversationId={}",
                    event.orderId(), event.userId(), event.conversationId());

            Order order = orderRepository.findById(event.orderId()).orElse(null);

            if (order == null || order.getStatus() != OrderStatus.PENDING_PAYMENT) {
                log.info("Payment success event skipped. orderId={}, currentOrderState={}",
                        event.orderId(),
                        order != null ? order.getStatus() : null);
                return;
            }

            order.setStatus(OrderStatus.PAID);
            order.setPaymentConversationId(event.conversationId());
            orderRepository.save(order);

            log.info("Order marked as PAID. orderId={}, conversationId={}",
                    order.getId(), order.getPaymentConversationId());
        } finally {
            MDC.remove("correlationId");
        }
    }

    @Transactional
    @RabbitListener(queues = "${order.payment.failed-queue}")
    public void handlePaymentFailed(PaymentFailedEvent event) {
        putCorrelationId(event.correlationId());

        try {
            log.info("Payment failed event received. orderId={}, userId={}, conversationId={}",
                    event.orderId(), event.userId(), event.conversationId());

            Order order = orderRepository.findById(event.orderId()).orElse(null);

            if (order == null || order.getStatus() != OrderStatus.PENDING_PAYMENT) {
                log.info("Payment failed event skipped. orderId={}, currentOrderState={}",
                        event.orderId(),
                        order != null ? order.getStatus() : null);
                return;
            }

            order.setStatus(OrderStatus.PAYMENT_FAILED);
            order.setPaymentConversationId(event.conversationId());
            orderRepository.save(order);

            log.info("Order marked as PAYMENT_FAILED. orderId={}, conversationId={}",
                    order.getId(), order.getPaymentConversationId());
        } finally {
            MDC.remove("correlationId");
        }
    }

    private void putCorrelationId(String correlationId) {
        if (StringUtils.hasText(correlationId)) {
            MDC.put("correlationId", correlationId);
        }
    }
}
