package com.stockmanagement.inventory_service.listener;

import com.stockmanagement.inventory_service.config.RabbitMQConfig;
import com.stockmanagement.inventory_service.event.LowStockEvent;
import com.stockmanagement.inventory_service.event.OrderCreatedEvent;
import com.stockmanagement.inventory_service.publisher.InventoryEventPublisher;
import com.stockmanagement.inventory_service.service.InventoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderEventListener {
    
    private final InventoryService inventoryService;
    private final InventoryEventPublisher eventPublisher;
    
    @RabbitListener(queues = RabbitMQConfig.INVENTORY_ORDER_CREATED_QUEUE)
    public void handleOrderCreatedEvent(OrderCreatedEvent event) {
        log.info("Received order created event for order: {}", event.getOrderNumber());
        
        try {
            event.getItems().forEach(item -> {
                log.info("Processing inventory for product: {}, quantity: {}", 
                        item.getProductName(), item.getQuantity());
                
                checkLowStock(item.getProductId(), event.getWarehouseId());
            });
        } catch (Exception e) {
            log.error("Error processing order created event", e);
        }
    }
    
    private void checkLowStock(Long productId, Long warehouseId) {
        try {
            var inventory = inventoryService.getInventoryByProductAndWarehouse(productId, warehouseId);
            
            if (inventory.getLowStock()) {
                LowStockEvent lowStockEvent = new LowStockEvent(
                        inventory.getProductId(),
                        inventory.getProductName(),
                        inventory.getWarehouseId(),
                        inventory.getWarehouseName(),
                        inventory.getAvailableQuantity(),
                        inventory.getMinimumQuantity()
                );
                
                eventPublisher.publishLowStockEvent(lowStockEvent);
            }
        } catch (Exception e) {
            log.error("Error checking low stock for product: {}", productId, e);
        }
    }
}
