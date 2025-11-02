package com.stockmanagement.supplier_service.controller;

import com.stockmanagement.supplier_service.dto.PurchaseOrderRequest;
import com.stockmanagement.supplier_service.dto.PurchaseOrderResponse;
import com.stockmanagement.supplier_service.entity.PurchaseOrderStatus;
import com.stockmanagement.supplier_service.service.PurchaseOrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/purchase-orders")
@RequiredArgsConstructor
public class PurchaseOrderController {
    
    private final PurchaseOrderService purchaseOrderService;
    
    @PostMapping
    public ResponseEntity<PurchaseOrderResponse> createPurchaseOrder(@Valid @RequestBody PurchaseOrderRequest request) {
        PurchaseOrderResponse response = purchaseOrderService.createPurchaseOrder(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<PurchaseOrderResponse> getPurchaseOrderById(@PathVariable Long id) {
        PurchaseOrderResponse response = purchaseOrderService.getPurchaseOrderById(id);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/number/{poNumber}")
    public ResponseEntity<PurchaseOrderResponse> getPurchaseOrderByNumber(@PathVariable String poNumber) {
        PurchaseOrderResponse response = purchaseOrderService.getPurchaseOrderByNumber(poNumber);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping
    public ResponseEntity<List<PurchaseOrderResponse>> getAllPurchaseOrders() {
        List<PurchaseOrderResponse> responses = purchaseOrderService.getAllPurchaseOrders();
        return ResponseEntity.ok(responses);
    }
    
    @GetMapping("/supplier/{supplierId}")
    public ResponseEntity<List<PurchaseOrderResponse>> getPurchaseOrdersBySupplier(@PathVariable Long supplierId) {
        List<PurchaseOrderResponse> responses = purchaseOrderService.getPurchaseOrdersBySupplier(supplierId);
        return ResponseEntity.ok(responses);
    }
    
    @GetMapping("/status/{status}")
    public ResponseEntity<List<PurchaseOrderResponse>> getPurchaseOrdersByStatus(@PathVariable PurchaseOrderStatus status) {
        List<PurchaseOrderResponse> responses = purchaseOrderService.getPurchaseOrdersByStatus(status);
        return ResponseEntity.ok(responses);
    }
    
    @PutMapping("/{id}/status")
    public ResponseEntity<PurchaseOrderResponse> updatePurchaseOrderStatus(
            @PathVariable Long id,
            @RequestParam PurchaseOrderStatus status) {
        PurchaseOrderResponse response = purchaseOrderService.updatePurchaseOrderStatus(id, status);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/{id}/receive")
    public ResponseEntity<PurchaseOrderResponse> receiveItems(
            @PathVariable Long id,
            @RequestParam Long itemId,
            @RequestParam Integer receivedQuantity) {
        PurchaseOrderResponse response = purchaseOrderService.receiveItems(id, itemId, receivedQuantity);
        return ResponseEntity.ok(response);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancelPurchaseOrder(@PathVariable Long id) {
        purchaseOrderService.cancelPurchaseOrder(id);
        return ResponseEntity.noContent().build();
    }
}
