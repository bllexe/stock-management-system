package com.stockmanagement.supplier_service.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SupplierProductRequest {
    
    @NotNull(message = "Product ID is required")
    private Long productId;
    
    private String supplierSku;
    
    private String supplierProductName;
    
    @NotNull(message = "Unit price is required")
    @Positive(message = "Unit price must be positive")
    private BigDecimal unitPrice;
    
    @Min(value = 1, message = "Minimum order quantity must be at least 1")
    private Integer minimumOrderQuantity;
    
    @Min(value = 0, message = "Lead time cannot be negative")
    private Integer leadTimeDays;
    
    private Boolean active;
}
