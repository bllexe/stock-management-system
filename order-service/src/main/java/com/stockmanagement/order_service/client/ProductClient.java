package com.stockmanagement.order_service.client;


import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.stockmanagement.order_service.dto.ProductDto;

@FeignClient(name = "product-service", fallback = ProductClientFallback.class)
public interface ProductClient {
    
    @GetMapping("/products/{id}")
    ProductDto getProductById(@PathVariable Long id);
}
 