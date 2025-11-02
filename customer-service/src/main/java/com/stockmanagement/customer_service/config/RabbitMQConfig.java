package com.stockmanagement.customer_service.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {
    
    public static final String ORDER_EXCHANGE = "order.exchange";
    public static final String CUSTOMER_ORDER_CONFIRMED_QUEUE = "customer.order.confirmed.queue";
    public static final String CUSTOMER_ORDER_CANCELLED_QUEUE = "customer.order.cancelled.queue";
    
    public static final String ORDER_CONFIRMED_ROUTING_KEY = "order.confirmed";
    public static final String ORDER_CANCELLED_ROUTING_KEY = "order.cancelled";
    
    @Bean
    public TopicExchange orderExchange() {
        return new TopicExchange(ORDER_EXCHANGE);
    }
    
    @Bean
    public Queue customerOrderConfirmedQueue() {
        return new Queue(CUSTOMER_ORDER_CONFIRMED_QUEUE, true);
    }
    
    @Bean
    public Queue customerOrderCancelledQueue() {
        return new Queue(CUSTOMER_ORDER_CANCELLED_QUEUE, true);
    }
    
    @Bean
    public Binding customerOrderConfirmedBinding() {
        return BindingBuilder
                .bind(customerOrderConfirmedQueue())
                .to(orderExchange())
                .with(ORDER_CONFIRMED_ROUTING_KEY);
    }
    
    @Bean
    public Binding customerOrderCancelledBinding() {
        return BindingBuilder
                .bind(customerOrderCancelledQueue())
                .to(orderExchange())
                .with(ORDER_CANCELLED_ROUTING_KEY);
    }
    
    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }
    
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(messageConverter());
        return rabbitTemplate;
    }
}
