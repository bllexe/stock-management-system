package com.stockmanagement.order_service.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import com.stockmanagement.order_service.dto.InventoryDto;

@Component
@Slf4j
public class InventoryClientFallback implements InventoryClient {
    
    @Override
    public InventoryDto getInventory(Long productId, Long warehouseId) {
        log.error("Fallback: Unable to fetch inventory for product: {} warehouse: {}", productId, warehouseId);
        return new InventoryDto(null, productId, warehouseId, 0);
    }
    
    @Override
    public Boolean reserveStock(Long productId, Long warehouseId, Integer quantity) {
        log.error("Fallback: Unable to reserve stock for product: {}", productId);
        return false;
    }
    
    @Override
    public void releaseStock(Long productId, Long warehouseId, Integer quantity) {
        log.error("Fallback: Unable to release stock for product: {}", productId);
    }
}