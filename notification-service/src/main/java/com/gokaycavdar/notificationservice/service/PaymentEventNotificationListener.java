package com.gokaycavdar.notificationservice.service;

import com.gokaycavdar.notificationservice.event.PaymentFailedEvent;
import com.gokaycavdar.notificationservice.event.PaymentSucceededEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentEventNotificationListener {

    private final NotificationService notificationService;

    @RabbitListener(queues = "${notification.payment.success-queue}")
    public void handlePaymentSucceeded(PaymentSucceededEvent event) {
        putCorrelationId(event.correlationId());

        try {
            log.info("Payment succeeded event received. orderId={}, userId={}, conversationId={}",
                    event.orderId(), event.userId(), event.conversationId());

            notificationService.createPaymentSucceededNotification(event);
        } finally {
            MDC.remove("correlationId");
        }
    }

    @RabbitListener(queues = "${notification.payment.failed-queue}")
    public void handlePaymentFailed(PaymentFailedEvent event) {
        putCorrelationId(event.correlationId());

        try {
            log.info("Payment failed event received. orderId={}, userId={}, conversationId={}",
                    event.orderId(), event.userId(), event.conversationId());

            notificationService.createPaymentFailedNotification(event);
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
