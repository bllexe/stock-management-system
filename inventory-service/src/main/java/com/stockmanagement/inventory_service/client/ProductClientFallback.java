package com.stockmanagement.inventory_service.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import com.stockmanagement.inventory_service.dto.ProductDto;

@Component
@Slf4j
public class ProductClientFallback implements ProductClient {
    
    @Override
    public ProductDto getProductById(Long id) {
        log.error("Fallback: Unable to fetch product with id: {}", id);
        ProductDto fallbackProduct = new ProductDto();
        fallbackProduct.setId(id);
        fallbackProduct.setName("Product Unavailable");
        return fallbackProduct;
    }
}