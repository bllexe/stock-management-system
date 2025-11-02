package com.stockmanagement.supplier_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.stockmanagement.supplier_service.entity.Supplier;
import com.stockmanagement.supplier_service.entity.SupplierStatus;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface SupplierRepository extends JpaRepository<Supplier, Long> {
    Optional<Supplier> findByCode(String code);
    List<Supplier> findByStatus(SupplierStatus status);
    List<Supplier> findByRatingGreaterThanEqual(Integer rating);
    boolean existsByCode(String code);
    boolean existsByTaxNumber(String taxNumber);
}
