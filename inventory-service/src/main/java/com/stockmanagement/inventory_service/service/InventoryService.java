package com.stockmanagement.inventory_service.service;

import com.stockmanagement.inventory_service.cache.InventoryCacheHelper;
import com.stockmanagement.inventory_service.client.ProductClient;
import com.stockmanagement.inventory_service.dto.*;
import com.stockmanagement.inventory_service.entity.Inventory;
import com.stockmanagement.inventory_service.entity.MovementType;
import com.stockmanagement.inventory_service.entity.StockMovement;
import com.stockmanagement.inventory_service.entity.Warehouse;
import com.stockmanagement.inventory_service.exception.InsufficientStockException;
import com.stockmanagement.inventory_service.exception.InventoryNotFoundException;
import com.stockmanagement.inventory_service.exception.WarehouseNotFoundException;
import com.stockmanagement.inventory_service.repository.InventoryRepository;
import com.stockmanagement.inventory_service.repository.StockMovementRepository;
import com.stockmanagement.inventory_service.repository.WarehouseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryService {
    
    private final InventoryRepository inventoryRepository;
    private final StockMovementRepository stockMovementRepository;
    private final WarehouseRepository warehouseRepository;
    private final ProductClient productClient;
    private final InventoryCacheHelper cacheHelper;
    
    @Transactional
    public InventoryResponse createInventory(InventoryRequest request) {
        Inventory inventory = new Inventory();
        inventory.setProductId(request.getProductId());
        inventory.setWarehouseId(request.getWarehouseId());
        inventory.setQuantity(request.getQuantity());
        inventory.setReservedQuantity(0);
        inventory.setMinimumQuantity(request.getMinimumQuantity());
        inventory.setShelfLocation(request.getShelfLocation());
        
        Inventory savedInventory = inventoryRepository.save(inventory);
        InventoryResponse response = mapToResponse(savedInventory);
        
        cacheHelper.cacheInventory(response);
        
        return response;
    }
    
    public InventoryResponse getInventoryById(Long id) {
        Inventory inventory = inventoryRepository.findById(id)
                .orElseThrow(() -> new InventoryNotFoundException("Inventory not found with id: " + id));
        return mapToResponse(inventory);
    }
    
    public InventoryResponse getInventoryByProductAndWarehouse(Long productId, Long warehouseId) {
        InventoryResponse cached = cacheHelper.getInventory(productId, warehouseId);
        if (cached != null) {
            log.debug("Inventory found in cache: product={}, warehouse={}", productId, warehouseId);
            return cached;
        }
        
        Inventory inventory = inventoryRepository.findByProductIdAndWarehouseId(productId, warehouseId)
                .orElseThrow(() -> new InventoryNotFoundException(
                        "Inventory not found for product: " + productId + " and warehouse: " + warehouseId));
        
        InventoryResponse response = mapToResponse(inventory);
        cacheHelper.cacheInventory(response);
        
        return response;
    }
    
    public List<InventoryResponse> getInventoryByProduct(Long productId) {
        return inventoryRepository.findByProductId(productId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    public List<InventoryResponse> getInventoryByWarehouse(Long warehouseId) {
        return inventoryRepository.findByWarehouseId(warehouseId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    public List<InventoryResponse> getLowStockItems() {
        return inventoryRepository.findLowStockItems().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    @Transactional
    public InventoryResponse processStockMovement(StockMovementRequest request) {
        Inventory inventory = inventoryRepository
                .findByProductIdAndWarehouseId(request.getProductId(), request.getWarehouseId())
                .orElseThrow(() -> new InventoryNotFoundException(
                        "Inventory not found for product: " + request.getProductId()));
        
        int newQuantity = calculateNewQuantity(inventory.getQuantity(), request.getType(), request.getQuantity());
        
        if (newQuantity < 0) {
            throw new InsufficientStockException("Insufficient stock for this operation");
        }
        
        inventory.setQuantity(newQuantity);
        Inventory updatedInventory = inventoryRepository.save(inventory);
        
        StockMovement movement = new StockMovement();
        movement.setProductId(request.getProductId());
        movement.setWarehouseId(request.getWarehouseId());
        movement.setType(request.getType());
        movement.setQuantity(request.getQuantity());
        movement.setReason(request.getReason());
        movement.setReferenceNo(request.getReferenceNo());
        stockMovementRepository.save(movement);
        
        InventoryResponse response = mapToResponse(updatedInventory);
        cacheHelper.cacheInventory(response);
        
        return response;
    }
    
    @Transactional
    public boolean reserveStock(Long productId, Long warehouseId, Integer quantity) {
        if (!cacheHelper.acquireLock(productId, warehouseId)) {
            log.warn("Failed to acquire lock for reservation: product={}, warehouse={}", productId, warehouseId);
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return reserveStock(productId, warehouseId, quantity);
        }
        
        try {
            Inventory inventory = inventoryRepository
                    .findByProductIdAndWarehouseId(productId, warehouseId)
                    .orElseThrow(() -> new InventoryNotFoundException(
                            "Inventory not found for product: " + productId));
            
            if (inventory.getAvailableQuantity() < quantity) {
                log.warn("Insufficient stock: product={}, available={}, requested={}", 
                        productId, inventory.getAvailableQuantity(), quantity);
                return false;
            }
            
            inventory.setReservedQuantity(inventory.getReservedQuantity() + quantity);
            inventoryRepository.save(inventory);
            
            cacheHelper.updateQuantityInCache(productId, warehouseId, 
                    inventory.getQuantity(), inventory.getReservedQuantity());
            
            log.info("Stock reserved: product={}, quantity={}", productId, quantity);
            return true;
            
        } finally {
            cacheHelper.releaseLock(productId, warehouseId);
        }
    }
    
    @Transactional
    public void releaseStock(Long productId, Long warehouseId, Integer quantity) {
        if (!cacheHelper.acquireLock(productId, warehouseId)) {
            log.warn("Failed to acquire lock for release: product={}, warehouse={}", productId, warehouseId);
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            releaseStock(productId, warehouseId, quantity);
            return;
        }
        
        try {
            Inventory inventory = inventoryRepository
                    .findByProductIdAndWarehouseId(productId, warehouseId)
                    .orElseThrow(() -> new InventoryNotFoundException(
                            "Inventory not found for product: " + productId));
            
            inventory.setReservedQuantity(Math.max(0, inventory.getReservedQuantity() - quantity));
            inventoryRepository.save(inventory);
            
            cacheHelper.updateQuantityInCache(productId, warehouseId, 
                    inventory.getQuantity(), inventory.getReservedQuantity());
            
            log.info("Stock released: product={}, quantity={}", productId, quantity);
            
        } finally {
            cacheHelper.releaseLock(productId, warehouseId);
        }
    }
    
    private int calculateNewQuantity(int currentQuantity, MovementType type, int quantity) {
        return switch (type) {
            case IN, RETURN -> currentQuantity + quantity;
            case OUT, TRANSFER -> currentQuantity - quantity;
            case ADJUSTMENT -> quantity;
        };
    }
    
    private InventoryResponse mapToResponse(Inventory inventory) {
        ProductDto product = productClient.getProductById(inventory.getProductId());
        Warehouse warehouse = warehouseRepository.findById(inventory.getWarehouseId())
                .orElseThrow(() -> new WarehouseNotFoundException("Warehouse not found"));
        
        InventoryResponse response = new InventoryResponse();
        response.setId(inventory.getId());
        response.setProductId(inventory.getProductId());
        response.setProductName(product.getName());
        response.setWarehouseId(inventory.getWarehouseId());
        response.setWarehouseName(warehouse.getName());
        response.setQuantity(inventory.getQuantity());
        response.setReservedQuantity(inventory.getReservedQuantity());
        response.setAvailableQuantity(inventory.getAvailableQuantity());
        response.setMinimumQuantity(inventory.getMinimumQuantity());
        response.setShelfLocation(inventory.getShelfLocation());
        response.setLowStock(inventory.getMinimumQuantity() != null && 
                            inventory.getAvailableQuantity() <= inventory.getMinimumQuantity());
        response.setCreatedAt(inventory.getCreatedAt());
        response.setUpdatedAt(inventory.getUpdatedAt());
        return response;
    }
}