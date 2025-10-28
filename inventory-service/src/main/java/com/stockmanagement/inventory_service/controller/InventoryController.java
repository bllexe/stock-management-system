package com.stockmanagement.inventory_service.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import com.stockmanagement.inventory_service.service.InventoryService;
import com.stockmanagement.inventory_service.dto.InventoryRequest;
import com.stockmanagement.inventory_service.dto.InventoryResponse;
import com.stockmanagement.inventory_service.dto.StockMovementRequest;

@RestController
@RequestMapping("/inventory")
@RequiredArgsConstructor
public class InventoryController {
    
    private final InventoryService inventoryService;
    
    @PostMapping
    public ResponseEntity<InventoryResponse> createInventory(@Valid @RequestBody InventoryRequest request) {
        InventoryResponse response = inventoryService.createInventory(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<InventoryResponse> getInventoryById(@PathVariable Long id) {
        InventoryResponse response = inventoryService.getInventoryById(id);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/product/{productId}")
    public ResponseEntity<List<InventoryResponse>> getInventoryByProduct(@PathVariable Long productId) {
        List<InventoryResponse> responses = inventoryService.getInventoryByProduct(productId);
        return ResponseEntity.ok(responses);
    }
    
    @GetMapping("/warehouse/{warehouseId}")
    public ResponseEntity<List<InventoryResponse>> getInventoryByWarehouse(@PathVariable Long warehouseId) {
        List<InventoryResponse> responses = inventoryService.getInventoryByWarehouse(warehouseId);
        return ResponseEntity.ok(responses);
    }
    
    @GetMapping("/product/{productId}/warehouse/{warehouseId}")
    public ResponseEntity<InventoryResponse> getInventoryByProductAndWarehouse(
            @PathVariable Long productId,
            @PathVariable Long warehouseId) {
        InventoryResponse response = inventoryService.getInventoryByProductAndWarehouse(productId, warehouseId);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/low-stock")
    public ResponseEntity<List<InventoryResponse>> getLowStockItems() {
        List<InventoryResponse> responses = inventoryService.getLowStockItems();
        return ResponseEntity.ok(responses);
    }
    
    @PostMapping("/movement")
    public ResponseEntity<InventoryResponse> processStockMovement(@Valid @RequestBody StockMovementRequest request) {
        InventoryResponse response = inventoryService.processStockMovement(request);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/reserve")
    public ResponseEntity<Boolean> reserveStock(
            @RequestParam Long productId,
            @RequestParam Long warehouseId,
            @RequestParam Integer quantity) {
        boolean reserved = inventoryService.reserveStock(productId, warehouseId, quantity);
        return ResponseEntity.ok(reserved);
    }
    
    @PostMapping("/release")
    public ResponseEntity<Void> releaseStock(
            @RequestParam Long productId,
            @RequestParam Long warehouseId,
            @RequestParam Integer quantity) {
        inventoryService.releaseStock(productId, warehouseId, quantity);
        return ResponseEntity.ok().build();
    }
}