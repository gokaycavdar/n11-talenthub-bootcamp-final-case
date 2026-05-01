package com.gokaycavdar.paymentservice.service;

import com.gokaycavdar.paymentservice.event.PaymentFailedEvent;
import com.gokaycavdar.paymentservice.event.PaymentSucceededEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PaymentEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    @Value("${payment.exchange}")
    private String exchangeName;

    @Value("${payment.routing.success}")
    private String successRoutingKey;

    @Value("${payment.routing.failed}")
    private String failedRoutingKey;

    public void publishPaymentSucceeded(PaymentSucceededEvent event) {
        rabbitTemplate.convertAndSend(exchangeName, successRoutingKey, event);
    }

    public void publishPaymentFailed(PaymentFailedEvent event) {
        rabbitTemplate.convertAndSend(exchangeName, failedRoutingKey, event);
    }
}
