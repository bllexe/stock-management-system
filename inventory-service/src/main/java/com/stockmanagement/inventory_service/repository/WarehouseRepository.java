package com.stockmanagement.inventory_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import com.stockmanagement.inventory_service.entity.Warehouse;

@Repository
public interface WarehouseRepository extends JpaRepository<Warehouse, Long> {
    Optional<Warehouse> findByCode(String code);
    List<Warehouse> findByActiveTrue();
    boolean existsByCode(String code);
}