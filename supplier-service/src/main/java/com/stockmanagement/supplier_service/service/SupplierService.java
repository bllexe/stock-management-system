package com.stockmanagement.supplier_service.service;

import com.stockmanagement.supplier_service.dto.SupplierRequest;
import com.stockmanagement.supplier_service.dto.SupplierResponse;
import com.stockmanagement.supplier_service.entity.Supplier;
import com.stockmanagement.supplier_service.entity.SupplierStatus;
import com.stockmanagement.supplier_service.exception.SupplierAlreadyExistsException;
import com.stockmanagement.supplier_service.exception.SupplierNotFoundException;
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
public class SupplierService {
    
    private final SupplierRepository supplierRepository;
    
    @Transactional
    public SupplierResponse createSupplier(SupplierRequest request) {
        if (supplierRepository.existsByCode(request.getCode())) {
            throw new SupplierAlreadyExistsException("Supplier with code " + request.getCode() + " already exists");
        }
        
        if (request.getTaxNumber() != null && supplierRepository.existsByTaxNumber(request.getTaxNumber())) {
            throw new SupplierAlreadyExistsException("Supplier with tax number " + request.getTaxNumber() + " already exists");
        }
        
        Supplier supplier = mapToEntity(request);
        Supplier savedSupplier = supplierRepository.save(supplier);
        
        log.info("Supplier created: {}", savedSupplier.getName());
        return mapToResponse(savedSupplier);
    }
    
    public SupplierResponse getSupplierById(Long id) {
        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new SupplierNotFoundException("Supplier not found with id: " + id));
        return mapToResponse(supplier);
    }
    
    public SupplierResponse getSupplierByCode(String code) {
        Supplier supplier = supplierRepository.findByCode(code)
                .orElseThrow(() -> new SupplierNotFoundException("Supplier not found with code: " + code));
        return mapToResponse(supplier);
    }
    
    public List<SupplierResponse> getAllSuppliers() {
        return supplierRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    public List<SupplierResponse> getSuppliersByStatus(SupplierStatus status) {
        return supplierRepository.findByStatus(status).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    public List<SupplierResponse> getTopRatedSuppliers(Integer minRating) {
        return supplierRepository.findByRatingGreaterThanEqual(minRating).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    @Transactional
    public SupplierResponse updateSupplier(Long id, SupplierRequest request) {
        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new SupplierNotFoundException("Supplier not found with id: " + id));
        
        if (!supplier.getCode().equals(request.getCode()) && supplierRepository.existsByCode(request.getCode())) {
            throw new SupplierAlreadyExistsException("Supplier with code " + request.getCode() + " already exists");
        }
        
        updateSupplierFields(supplier, request);
        Supplier updatedSupplier = supplierRepository.save(supplier);
        
        log.info("Supplier updated: {}", updatedSupplier.getName());
        return mapToResponse(updatedSupplier);
    }
    
    @Transactional
    public void deleteSupplier(Long id) {
        if (!supplierRepository.existsById(id)) {
            throw new SupplierNotFoundException("Supplier not found with id: " + id);
        }
        supplierRepository.deleteById(id);
        log.info("Supplier deleted with id: {}", id);
    }
    
    private Supplier mapToEntity(SupplierRequest request) {
        Supplier supplier = new Supplier();
        supplier.setName(request.getName());
        supplier.setCode(request.getCode());
        supplier.setTaxNumber(request.getTaxNumber());
        supplier.setContactPerson(request.getContactPerson());
        supplier.setEmail(request.getEmail());
        supplier.setPhone(request.getPhone());
        supplier.setAddress(request.getAddress());
        supplier.setCity(request.getCity());
        supplier.setCountry(request.getCountry());
        supplier.setPostalCode(request.getPostalCode());
        supplier.setPaymentTerms(request.getPaymentTerms());
        supplier.setCreditLimit(request.getCreditLimit());
        supplier.setStatus(request.getStatus());
        supplier.setRating(request.getRating());
        supplier.setNotes(request.getNotes());
        return supplier;
    }
    
    private void updateSupplierFields(Supplier supplier, SupplierRequest request) {
        supplier.setName(request.getName());
        supplier.setCode(request.getCode());
        supplier.setTaxNumber(request.getTaxNumber());
        supplier.setContactPerson(request.getContactPerson());
        supplier.setEmail(request.getEmail());
        supplier.setPhone(request.getPhone());
        supplier.setAddress(request.getAddress());
        supplier.setCity(request.getCity());
        supplier.setCountry(request.getCountry());
        supplier.setPostalCode(request.getPostalCode());
        supplier.setPaymentTerms(request.getPaymentTerms());
        supplier.setCreditLimit(request.getCreditLimit());
        supplier.setStatus(request.getStatus());
        supplier.setRating(request.getRating());
        supplier.setNotes(request.getNotes());
    }
    
    private SupplierResponse mapToResponse(Supplier supplier) {
        return new SupplierResponse(
                supplier.getId(),
                supplier.getName(),
                supplier.getCode(),
                supplier.getTaxNumber(),
                supplier.getContactPerson(),
                supplier.getEmail(),
                supplier.getPhone(),
                supplier.getAddress(),
                supplier.getCity(),
                supplier.getCountry(),
                supplier.getPostalCode(),
                supplier.getPaymentTerms(),
                supplier.getCreditLimit(),
                supplier.getStatus(),
                supplier.getRating(),
                supplier.getNotes(),
                supplier.getCreatedAt(),
                supplier.getUpdatedAt()
        );
    }
}
