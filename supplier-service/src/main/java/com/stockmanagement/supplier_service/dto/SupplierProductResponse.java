package com.stockmanagement.supplier_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SupplierProductResponse {
    private Long id;
    private Long supplierId;
    private String supplierName;
    private Long productId;
    private String productName;
    private String productSku;
    private String supplierSku;
    private String supplierProductName;
    private BigDecimal unitPrice;
    private Integer minimumOrderQuantity;
    private Integer leadTimeDays;
    private Boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
