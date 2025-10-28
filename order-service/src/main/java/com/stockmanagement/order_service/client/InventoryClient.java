package com.stockmanagement.order_service.client;


import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.stockmanagement.order_service.dto.InventoryDto;

@FeignClient(name = "inventory-service", fallback = InventoryClientFallback.class)
public interface InventoryClient {
    
    @GetMapping("/inventory/product/{productId}/warehouse/{warehouseId}")
    InventoryDto getInventory(@PathVariable Long productId, @PathVariable Long warehouseId);
    
    @PostMapping("/inventory/reserve")
    Boolean reserveStock(@RequestParam Long productId, 
                        @RequestParam Long warehouseId, 
                        @RequestParam Integer quantity);
    
    @PostMapping("/inventory/release")
    void releaseStock(@RequestParam Long productId, 
                     @RequestParam Long warehouseId, 
                     @RequestParam Integer quantity);
}