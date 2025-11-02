package com.stockmanagement.supplier_service.service;

import com.stockmanagement.supplier_service.client.ProductClient;
import com.stockmanagement.supplier_service.dto.ProductDto;
import com.stockmanagement.supplier_service.dto.SupplierProductRequest;
import com.stockmanagement.supplier_service.dto.SupplierProductResponse;
import com.stockmanagement.supplier_service.entity.Supplier;
import com.stockmanagement.supplier_service.entity.SupplierProduct;
import com.stockmanagement.supplier_service.exception.ProductNotFoundException;
import com.stockmanagement.supplier_service.exception.SupplierNotFoundException;
import com.stockmanagement.supplier_service.exception.SupplierProductAlreadyExistsException;
import com.stockmanagement.supplier_service.exception.SupplierProductNotFoundException;
import com.stockmanagement.supplier_service.repository.SupplierProductRepository;
import com.stockmanagement.supplier_service.repository.SupplierRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SupplierProductService {
    
    private final SupplierProductRepository supplierProductRepository;
    private final SupplierRepository supplierRepository;
    private final ProductClient productClient;
    
    @Transactional
    public SupplierProductResponse addProductToSupplier(Long supplierId, SupplierProductRequest request) {
        Supplier supplier = supplierRepository.findById(supplierId)
                .orElseThrow(() -> new SupplierNotFoundException("Supplier not found with id: " + supplierId));
        
        ProductDto product = productClient.getProductById(request.getProductId());
        if (product == null || product.getId() == null) {
            throw new ProductNotFoundException("Product not found with id: " + request.getProductId());
        }
        
        if (supplierProductRepository.findBySupplierIdAndProductId(supplierId, request.getProductId()).isPresent()) {
            throw new SupplierProductAlreadyExistsException("Product already exists for this supplier");
        }
        
        SupplierProduct supplierProduct = new SupplierProduct();
        supplierProduct.setSupplier(supplier);
        supplierProduct.setProductId(request.getProductId());
        supplierProduct.setSupplierSku(request.getSupplierSku());
        supplierProduct.setSupplierProductName(request.getSupplierProductName());
        supplierProduct.setUnitPrice(request.getUnitPrice());
        supplierProduct.setMinimumOrderQuantity(request.getMinimumOrderQuantity());
        supplierProduct.setLeadTimeDays(request.getLeadTimeDays());
        supplierProduct.setActive(request.getActive() != null ? request.getActive() : true);
        
        SupplierProduct saved = supplierProductRepository.save(supplierProduct);
        
        log.info("Product {} added to supplier {}", request.getProductId(), supplierId);
        return mapToResponse(saved, product);
    }
    
    public List<SupplierProductResponse> getSupplierProducts(Long supplierId) {
        return supplierProductRepository.findBySupplierId(supplierId).stream()
                .map(sp -> {
                    ProductDto product = productClient.getProductById(sp.getProductId());
                    return mapToResponse(sp, product);
                })
                .collect(Collectors.toList());
    }
    
    public List<SupplierProductResponse> getProductSuppliers(Long productId) {
        return supplierProductRepository.findByProductId(productId).stream()
                .map(sp -> {
                    ProductDto product = productClient.getProductById(sp.getProductId());
                    return mapToResponse(sp, product);
                })
                .collect(Collectors.toList());
    }
    
    @Transactional
    public SupplierProductResponse updateSupplierProduct(Long id, SupplierProductRequest request) {
        SupplierProduct supplierProduct = supplierProductRepository.findById(id)
                .orElseThrow(() -> new SupplierProductNotFoundException("Supplier product not found with id: " + id));
        
        supplierProduct.setSupplierSku(request.getSupplierSku());
        supplierProduct.setSupplierProductName(request.getSupplierProductName());
        supplierProduct.setUnitPrice(request.getUnitPrice());
        supplierProduct.setMinimumOrderQuantity(request.getMinimumOrderQuantity());
        supplierProduct.setLeadTimeDays(request.getLeadTimeDays());
        if (request.getActive() != null) {
            supplierProduct.setActive(request.getActive());
        }
        
        SupplierProduct updated = supplierProductRepository.save(supplierProduct);
        ProductDto product = productClient.getProductById(updated.getProductId());
        
        log.info("Supplier product updated: {}", id);
        return mapToResponse(updated, product);
    }
    
    @Transactional
    public void deleteSupplierProduct(Long id) {
        if (!supplierProductRepository.existsById(id)) {
            throw new SupplierProductNotFoundException("Supplier product not found with id: " + id);
        }
        supplierProductRepository.deleteById(id);
        log.info("Supplier product deleted: {}", id);
    }
    
    private SupplierProductResponse mapToResponse(SupplierProduct sp, ProductDto product) {
        return new SupplierProductResponse(
                sp.getId(),
                sp.getSupplier().getId(),
                sp.getSupplier().getName(),
                sp.getProductId(),
                product.getName(),
                product.getSku(),
                sp.getSupplierSku(),
                sp.getSupplierProductName(),
                sp.getUnitPrice(),
                sp.getMinimumOrderQuantity(),
                sp.getLeadTimeDays(),
                sp.getActive(),
                sp.getCreatedAt(),
                sp.getUpdatedAt()
        );
    }
}
