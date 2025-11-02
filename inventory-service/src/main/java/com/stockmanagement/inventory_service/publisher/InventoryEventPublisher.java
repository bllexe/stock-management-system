package com.stockmanagement.inventory_service.publisher;

import com.stockmanagement.inventory_service.config.RabbitMQConfig;
import com.stockmanagement.inventory_service.event.LowStockEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryEventPublisher {
    
    private final RabbitTemplate rabbitTemplate;
    
    public void publishLowStockEvent(LowStockEvent event) {
        log.info("Publishing low stock event for product: {}", event.getProductName());
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.INVENTORY_EXCHANGE,
                RabbitMQConfig.LOW_STOCK_ROUTING_KEY,
                event
        );
    }
}
