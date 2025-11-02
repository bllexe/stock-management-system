package com.stockmanagement.supplier_service.controller;

import com.stockmanagement.supplier_service.dto.SupplierProductRequest;
import com.stockmanagement.supplier_service.dto.SupplierProductResponse;
import com.stockmanagement.supplier_service.service.SupplierProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/supplier-products")
@RequiredArgsConstructor
public class SupplierProductController {
    
    private final SupplierProductService supplierProductService;
    
    @PostMapping("/supplier/{supplierId}")
    public ResponseEntity<SupplierProductResponse> addProductToSupplier(
            @PathVariable Long supplierId,
            @Valid @RequestBody SupplierProductRequest request) {
        SupplierProductResponse response = supplierProductService.addProductToSupplier(supplierId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @GetMapping("/supplier/{supplierId}")
    public ResponseEntity<List<SupplierProductResponse>> getSupplierProducts(@PathVariable Long supplierId) {
        List<SupplierProductResponse> responses = supplierProductService.getSupplierProducts(supplierId);
        return ResponseEntity.ok(responses);
    }
    
    @GetMapping("/product/{productId}")
    public ResponseEntity<List<SupplierProductResponse>> getProductSuppliers(@PathVariable Long productId) {
        List<SupplierProductResponse> responses = supplierProductService.getProductSuppliers(productId);
        return ResponseEntity.ok(responses);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<SupplierProductResponse> updateSupplierProduct(
            @PathVariable Long id,
            @Valid @RequestBody SupplierProductRequest request) {
        SupplierProductResponse response = supplierProductService.updateSupplierProduct(id, request);
        return ResponseEntity.ok(response);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSupplierProduct(@PathVariable Long id) {
        supplierProductService.deleteSupplierProduct(id);
        return ResponseEntity.noContent().build();
    }
}
