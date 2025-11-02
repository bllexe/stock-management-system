package com.stockmanagement.supplier_service.controller;

import com.stockmanagement.supplier_service.dto.SupplierRequest;
import com.stockmanagement.supplier_service.dto.SupplierResponse;
import com.stockmanagement.supplier_service.entity.SupplierStatus;
import com.stockmanagement.supplier_service.service.SupplierService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/suppliers")
@RequiredArgsConstructor
public class SupplierController {
    
    private final SupplierService supplierService;
    
    @PostMapping
    public ResponseEntity<SupplierResponse> createSupplier(@Valid @RequestBody SupplierRequest request) {
        SupplierResponse response = supplierService.createSupplier(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<SupplierResponse> getSupplierById(@PathVariable Long id) {
        SupplierResponse response = supplierService.getSupplierById(id);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/code/{code}")
    public ResponseEntity<SupplierResponse> getSupplierByCode(@PathVariable String code) {
        SupplierResponse response = supplierService.getSupplierByCode(code);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping
    public ResponseEntity<List<SupplierResponse>> getAllSuppliers() {
        List<SupplierResponse> responses = supplierService.getAllSuppliers();
        return ResponseEntity.ok(responses);
    }
    
    @GetMapping("/status/{status}")
    public ResponseEntity<List<SupplierResponse>> getSuppliersByStatus(@PathVariable SupplierStatus status) {
        List<SupplierResponse> responses = supplierService.getSuppliersByStatus(status);
        return ResponseEntity.ok(responses);
    }
    
    @GetMapping("/top-rated")
    public ResponseEntity<List<SupplierResponse>> getTopRatedSuppliers(@RequestParam(defaultValue = "4") Integer minRating) {
        List<SupplierResponse> responses = supplierService.getTopRatedSuppliers(minRating);
        return ResponseEntity.ok(responses);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<SupplierResponse> updateSupplier(
            @PathVariable Long id,
            @Valid @RequestBody SupplierRequest request) {
        SupplierResponse response = supplierService.updateSupplier(id, request);
        return ResponseEntity.ok(response);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSupplier(@PathVariable Long id) {
        supplierService.deleteSupplier(id);
        return ResponseEntity.noContent().build();
    }
}
