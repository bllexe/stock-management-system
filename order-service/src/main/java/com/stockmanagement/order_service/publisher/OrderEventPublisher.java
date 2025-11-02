package com.stockmanagement.order_service.publisher;


import com.stockmanagement.order_service.config.RabbitMQConfig;
import com.stockmanagement.order_service.event.OrderCancelledEvent;
import com.stockmanagement.order_service.event.OrderConfirmedEvent;
import com.stockmanagement.order_service.event.OrderCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderEventPublisher {
    
    private final RabbitTemplate rabbitTemplate;
    
    public void publishOrderCreatedEvent(OrderCreatedEvent event) {
        log.info("Publishing order created event: {}", event.getOrderNumber());
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.ORDER_EXCHANGE,
                RabbitMQConfig.ORDER_CREATED_ROUTING_KEY,
                event
        );
    }
    
    public void publishOrderCancelledEvent(OrderCancelledEvent event) {
        log.info("Publishing order cancelled event: {}", event.getOrderNumber());
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.ORDER_EXCHANGE,
                RabbitMQConfig.ORDER_CANCELLED_ROUTING_KEY,
                event
        );
    }
    
    public void publishOrderConfirmedEvent(OrderConfirmedEvent event) {
        log.info("Publishing order confirmed event: {}", event.getOrderNumber());
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.ORDER_EXCHANGE,
                RabbitMQConfig.ORDER_CONFIRMED_ROUTING_KEY,
                event
        );
    }
}