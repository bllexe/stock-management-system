package com.stockmanagement.product_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponse {
    private Long id;
    private String name;
    private String description;
    private String sku;
    private String barcode;
    private BigDecimal price;
    private String category;
    private String brand;
    private String unit;
    private Boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}