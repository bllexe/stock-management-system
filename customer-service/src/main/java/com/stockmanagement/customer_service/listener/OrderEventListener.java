package com.stockmanagement.customer_service.listener;

import com.stockmanagement.customer_service.config.RabbitMQConfig;
import com.stockmanagement.customer_service.event.OrderCancelledEvent;
import com.stockmanagement.customer_service.event.OrderConfirmedEvent;
import com.stockmanagement.customer_service.service.CustomerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderEventListener {
    
    private final CustomerService customerService;
    
    @RabbitListener(queues = RabbitMQConfig.CUSTOMER_ORDER_CONFIRMED_QUEUE)
    public void handleOrderConfirmedEvent(OrderConfirmedEvent event) {
        log.info("Received order confirmed event for order: {}", event.getOrderNumber());
        
        try {
            customerService.addLoyaltyPoints(event.getCustomerId(), 10);
            log.info("Added 10 loyalty points to customer: {}", event.getCustomerId());
        } catch (Exception e) {
            log.error("Error processing order confirmed event", e);
        }
    }
    
    @RabbitListener(queues = RabbitMQConfig.CUSTOMER_ORDER_CANCELLED_QUEUE)
    public void handleOrderCancelledEvent(OrderCancelledEvent event) {
        log.info("Received order cancelled event for order: {}", event.getOrderNumber());
        
        try {
            log.info("Order cancelled for customer: {}", event.getCustomerId());
        } catch (Exception e) {
            log.error("Error processing order cancelled event", e);
        }
    }
}
