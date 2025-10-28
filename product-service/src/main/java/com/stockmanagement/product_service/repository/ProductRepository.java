package com.stockmanagement.product_service.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.stockmanagement.product_service.entity.Product;

public interface ProductRepository extends JpaRepository<Product,Long>{

    Optional<Product> findBySku(String sku);
    Optional<Product> findByBarcode(String barcode);
    List<Product> findByCategory(String category);
    List<Product> findByActiveTrue();
    boolean existsBySku(String sku);
    boolean existsByBarcode(String barcode);

}
