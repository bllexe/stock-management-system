package com.stockmanagement.order_service.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import com.stockmanagement.order_service.dto.ProductDto;

import java.math.BigDecimal;

@Component
@Slf4j
public class ProductClientFallback implements ProductClient {
    
    @Override
    public ProductDto getProductById(Long id) {
        log.error("Fallback: Unable to fetch product with id: {}", id);
        return new ProductDto(id, "Product Unavailable", "N/A", BigDecimal.ZERO);
    }
}