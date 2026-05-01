package com.gokaycavdar.paymentservice.service;

import com.gokaycavdar.paymentservice.event.PaymentFailedEvent;
import com.gokaycavdar.paymentservice.event.PaymentSucceededEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    @Value("${payment.exchange}")
    private String exchangeName;

    @Value("${payment.routing.success}")
    private String successRoutingKey;

    @Value("${payment.routing.failed}")
    private String failedRoutingKey;

    public void publishPaymentSucceeded(PaymentSucceededEvent event) {
        log.info(
                "Publishing payment success event. orderId={}, userId={}, conversationId={}",
                event.orderId(),
                event.userId(),
                event.conversationId()
        );
        rabbitTemplate.convertAndSend(exchangeName, successRoutingKey, event);
    }

    public void publishPaymentFailed(PaymentFailedEvent event) {
        log.info(
                "Publishing payment failed event. orderId={}, userId={}, conversationId={}",
                event.orderId(),
                event.userId(),
                event.conversationId()
        );
        rabbitTemplate.convertAndSend(exchangeName, failedRoutingKey, event);
    }
}
