package com.stockmanagement.supplier_service.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.stockmanagement.supplier_service.dto.ProductDto;
import com.stockmanagement.supplier_service.dto.PurchaseOrderItemRequest;
import com.stockmanagement.supplier_service.dto.PurchaseOrderItemResponse;
import com.stockmanagement.supplier_service.dto.PurchaseOrderRequest;
import com.stockmanagement.supplier_service.dto.PurchaseOrderResponse;
import com.stockmanagement.supplier_service.entity.PurchaseOrder;
import com.stockmanagement.supplier_service.entity.PurchaseOrderItem;
import com.stockmanagement.supplier_service.entity.PurchaseOrderStatus;
import com.stockmanagement.supplier_service.entity.Supplier;
import com.stockmanagement.supplier_service.exception.PurchaseOrderNotFoundException;
import com.stockmanagement.supplier_service.exception.SupplierNotFoundException;
import com.stockmanagement.supplier_service.repository.PurchaseOrderRepository;
import com.stockmanagement.supplier_service.repository.SupplierRepository;
import com.stockmanagement.supplier_service.client.ProductClient;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PurchaseOrderService {
    
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final SupplierRepository supplierRepository;
    private final ProductClient productClient;
    
    @Transactional
    public PurchaseOrderResponse createPurchaseOrder(PurchaseOrderRequest request) {
        Supplier supplier = supplierRepository.findById(request.getSupplierId())
                .orElseThrow(() -> new SupplierNotFoundException("Supplier not found with id: " + request.getSupplierId()));
        
        PurchaseOrder po = new PurchaseOrder();
        po.setPoNumber(generatePoNumber());
        po.setSupplier(supplier);
        po.setOrderDate(request.getOrderDate());
        po.setExpectedDeliveryDate(request.getExpectedDeliveryDate());
        po.setStatus(PurchaseOrderStatus.DRAFT);
        po.setWarehouseId(request.getWarehouseId());
        po.setNotes(request.getNotes());
        
        BigDecimal totalAmount = BigDecimal.ZERO;
        
        for (PurchaseOrderItemRequest itemRequest : request.getItems()) {
            ProductDto product = productClient.getProductById(itemRequest.getProductId());
            
            PurchaseOrderItem item = new PurchaseOrderItem();
            item.setProductId(product.getId());
            item.setProductName(product.getName());
            item.setProductSku(product.getSku());
            item.setOrderedQuantity(itemRequest.getQuantity());
            item.setReceivedQuantity(0);
            item.setUnitPrice(product.getPrice());
            item.setTotalPrice(product.getPrice().multiply(BigDecimal.valueOf(itemRequest.getQuantity())));
            
            po.addItem(item);
            totalAmount = totalAmount.add(item.getTotalPrice());
        }
        
        po.setTotalAmount(totalAmount);
        PurchaseOrder saved = purchaseOrderRepository.save(po);
        
        log.info("Purchase order created: {}", saved.getPoNumber());
        return mapToResponse(saved);
    }
    
    public PurchaseOrderResponse getPurchaseOrderById(Long id) {
        PurchaseOrder po = purchaseOrderRepository.findById(id)
                .orElseThrow(() -> new PurchaseOrderNotFoundException("Purchase order not found with id: " + id));
        return mapToResponse(po);
    }
    
    public PurchaseOrderResponse getPurchaseOrderByNumber(String poNumber) {
        PurchaseOrder po = purchaseOrderRepository.findByPoNumber(poNumber)
                .orElseThrow(() -> new PurchaseOrderNotFoundException("Purchase order not found with number: " + poNumber));
        return mapToResponse(po);
    }
    
    public List<PurchaseOrderResponse> getAllPurchaseOrders() {
        return purchaseOrderRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    public List<PurchaseOrderResponse> getPurchaseOrdersBySupplier(Long supplierId) {
        return purchaseOrderRepository.findBySupplierId(supplierId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    public List<PurchaseOrderResponse> getPurchaseOrdersByStatus(PurchaseOrderStatus status) {
        return purchaseOrderRepository.findByStatus(status).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    @Transactional
    public PurchaseOrderResponse updatePurchaseOrderStatus(Long id, PurchaseOrderStatus status) {
        PurchaseOrder po = purchaseOrderRepository.findById(id)
                .orElseThrow(() -> new PurchaseOrderNotFoundException("Purchase order not found with id: " + id));
        
        po.setStatus(status);
        PurchaseOrder updated = purchaseOrderRepository.save(po);
        
        log.info("Purchase order {} status updated to {}", po.getPoNumber(), status);
        return mapToResponse(updated);
    }
    
    @Transactional
    public PurchaseOrderResponse receiveItems(Long id, Long itemId, Integer receivedQuantity) {
        PurchaseOrder po = purchaseOrderRepository.findById(id)
                .orElseThrow(() -> new PurchaseOrderNotFoundException("Purchase order not found with id: " + id));
        
        PurchaseOrderItem item = po.getItems().stream()
                .filter(i -> i.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Item not found in purchase order"));
        
        item.setReceivedQuantity(item.getReceivedQuantity() + receivedQuantity);
        
        boolean allReceived = po.getItems().stream()
                .allMatch(i -> i.getReceivedQuantity().equals(i.getOrderedQuantity()));
        
        boolean partiallyReceived = po.getItems().stream()
                .anyMatch(i -> i.getReceivedQuantity() > 0);
        
        if (allReceived) {
            po.setStatus(PurchaseOrderStatus.RECEIVED);
            po.setActualDeliveryDate(java.time.LocalDate.now());
        } else if (partiallyReceived) {
            po.setStatus(PurchaseOrderStatus.PARTIALLY_RECEIVED);
        }
        
        PurchaseOrder updated = purchaseOrderRepository.save(po);
        
        log.info("Purchase order {} items received", po.getPoNumber());
        return mapToResponse(updated);
    }
    
    @Transactional
    public void cancelPurchaseOrder(Long id) {
        PurchaseOrder po = purchaseOrderRepository.findById(id)
                .orElseThrow(() -> new PurchaseOrderNotFoundException("Purchase order not found with id: " + id));
        
        if (po.getStatus() == PurchaseOrderStatus.RECEIVED || po.getStatus() == PurchaseOrderStatus.PARTIALLY_RECEIVED) {
            throw new IllegalStateException("Cannot cancel purchase order in status: " + po.getStatus());
        }
        
        po.setStatus(PurchaseOrderStatus.CANCELLED);
        purchaseOrderRepository.save(po);
        
        log.info("Purchase order cancelled: {}", po.getPoNumber());
    }
    
    private String generatePoNumber() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        return "PO-" + timestamp;
    }
    
    private PurchaseOrderResponse mapToResponse(PurchaseOrder po) {
        List<PurchaseOrderItemResponse> itemResponses = po.getItems().stream()
                .map(item -> new PurchaseOrderItemResponse(
                        item.getId(),
                        item.getProductId(),
                        item.getProductName(),
                        item.getProductSku(),
                        item.getOrderedQuantity(),
                        item.getReceivedQuantity(),
                        item.getUnitPrice(),
                        item.getTotalPrice()
                ))
                .collect(Collectors.toList());
        
        return new PurchaseOrderResponse(
                po.getId(),
                po.getPoNumber(),
                po.getSupplier().getId(),
                po.getSupplier().getName(),
                po.getOrderDate(),
                po.getExpectedDeliveryDate(),
                po.getActualDeliveryDate(),
                po.getStatus(),
                po.getTotalAmount(),
                po.getWarehouseId(),
                po.getNotes(),
                itemResponses,
                po.getCreatedAt(),
                po.getUpdatedAt()
        );
    }
}
