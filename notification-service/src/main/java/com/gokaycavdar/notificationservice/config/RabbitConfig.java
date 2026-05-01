package com.gokaycavdar.notificationservice.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableRabbit
public class RabbitConfig {

    @Bean
    public TopicExchange paymentExchange(@Value("${payment.exchange}") String exchangeName) {
        return new TopicExchange(exchangeName, true, false);
    }

    @Bean
    public Queue paymentSuccessQueue(@Value("${notification.payment.success-queue}") String queueName) {
        return new Queue(queueName, true);
    }

    @Bean
    public Queue paymentFailedQueue(@Value("${notification.payment.failed-queue}") String queueName) {
        return new Queue(queueName, true);
    }

    @Bean
    public Binding paymentSuccessBinding(
            Queue paymentSuccessQueue,
            TopicExchange paymentExchange,
            @Value("${payment.routing.success}") String routingKey
    ) {
        return BindingBuilder.bind(paymentSuccessQueue).to(paymentExchange).with(routingKey);
    }

    @Bean
    public Binding paymentFailedBinding(
            Queue paymentFailedQueue,
            TopicExchange paymentExchange,
            @Value("${payment.routing.failed}") String routingKey
    ) {
        return BindingBuilder.bind(paymentFailedQueue).to(paymentExchange).with(routingKey);
    }

    @Bean
    public JacksonJsonMessageConverter jacksonJsonMessageConverter() {
        return new JacksonJsonMessageConverter();
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory,
            JacksonJsonMessageConverter messageConverter
    ) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(messageConverter);
        factory.setDefaultRequeueRejected(false);
        return factory;
    }
}
