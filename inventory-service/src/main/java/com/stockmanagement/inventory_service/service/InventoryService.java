package com.stockmanagement.inventory_service.service;

import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.transaction.annotation.Transactional;
import com.stockmanagement.inventory_service.entity.Inventory;
import com.stockmanagement.inventory_service.entity.MovementType;
import com.stockmanagement.inventory_service.entity.StockMovement;
import com.stockmanagement.inventory_service.entity.Warehouse;
import com.stockmanagement.inventory_service.exception.InsufficientStockException;
import com.stockmanagement.inventory_service.exception.InventoryNotFoundException;
import com.stockmanagement.inventory_service.exception.WarehouseNotFoundException;
import com.stockmanagement.inventory_service.client.ProductClient;
import com.stockmanagement.inventory_service.dto.InventoryRequest;
import com.stockmanagement.inventory_service.dto.InventoryResponse;
import com.stockmanagement.inventory_service.dto.StockMovementRequest;
import com.stockmanagement.inventory_service.dto.ProductDto;
import com.stockmanagement.inventory_service.repository.InventoryRepository;
import com.stockmanagement.inventory_service.repository.StockMovementRepository;
import com.stockmanagement.inventory_service.repository.WarehouseRepository;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryService {
    
    private final InventoryRepository inventoryRepository;
    private final StockMovementRepository stockMovementRepository;
    private final WarehouseRepository warehouseRepository;
    private final ProductClient productClient;
    
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
        return mapToResponse(savedInventory);
    }
    
    public InventoryResponse getInventoryById(Long id) {
        Inventory inventory = inventoryRepository.findById(id)
                .orElseThrow(() -> new InventoryNotFoundException("Inventory not found with id: " + id));
        return mapToResponse(inventory);
    }
    
    public InventoryResponse getInventoryByProductAndWarehouse(Long productId, Long warehouseId) {
        Inventory inventory = inventoryRepository.findByProductIdAndWarehouseId(productId, warehouseId)
                .orElseThrow(() -> new InventoryNotFoundException(
                        "Inventory not found for product: " + productId + " and warehouse: " + warehouseId));
        return mapToResponse(inventory);
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
        
        return mapToResponse(updatedInventory);
    }
    
    @Transactional
    public boolean reserveStock(Long productId, Long warehouseId, Integer quantity) {
        Inventory inventory = inventoryRepository
                .findByProductIdAndWarehouseId(productId, warehouseId)
                .orElseThrow(() -> new InventoryNotFoundException(
                        "Inventory not found for product: " + productId));
        
        if (inventory.getAvailableQuantity() < quantity) {
            return false;
        }
        
        inventory.setReservedQuantity(inventory.getReservedQuantity() + quantity);
        inventoryRepository.save(inventory);
        return true;
    }
    
    @Transactional
    public void releaseStock(Long productId, Long warehouseId, Integer quantity) {
        Inventory inventory = inventoryRepository
                .findByProductIdAndWarehouseId(productId, warehouseId)
                .orElseThrow(() -> new InventoryNotFoundException(
                        "Inventory not found for product: " + productId));
        
        inventory.setReservedQuantity(Math.max(0, inventory.getReservedQuantity() - quantity));
        inventoryRepository.save(inventory);
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