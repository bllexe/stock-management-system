package com.stockmanagement.inventory_service.repository;

import com.stockmanagement.inventory_service.entity.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Long> {
    Optional<Inventory> findByProductIdAndWarehouseId(Long productId, Long warehouseId);
    List<Inventory> findByProductId(Long productId);
    List<Inventory> findByWarehouseId(Long warehouseId);
    
    @Query("SELECT i FROM Inventory i WHERE i.availableQuantity <= i.minimumQuantity")
    List<Inventory> findLowStockItems();
}