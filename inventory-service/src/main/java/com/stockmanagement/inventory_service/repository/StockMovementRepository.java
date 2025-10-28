package com.stockmanagement.inventory_service.repository;

import com.stockmanagement.inventory_service.entity.StockMovement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface StockMovementRepository extends JpaRepository<StockMovement, Long> {
    List<StockMovement> findByProductId(Long productId);
    List<StockMovement> findByWarehouseId(Long warehouseId);
    List<StockMovement> findByProductIdAndWarehouseId(Long productId, Long warehouseId);
}