package com.stockmanagement.supplier_service.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.stockmanagement.supplier_service.entity.SupplierStatus;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SupplierRequest {
    
    @NotBlank(message = "Supplier name is required")
    private String name;
    
    @NotBlank(message = "Supplier code is required")
    private String code;
    
    private String taxNumber;
    
    private String contactPerson;
    
    @Email(message = "Email should be valid")
    private String email;
    
    private String phone;
    
    private String address;
    
    private String city;
    
    private String country;
    
    private String postalCode;
    
    private String paymentTerms;
    
    private Double creditLimit;
    
    @NotNull(message = "Status is required")
    private SupplierStatus status;
    
    private Integer rating;
    
    private String notes;
}
