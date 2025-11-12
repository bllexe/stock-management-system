package com.stockmanagement.product_service.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;
import com.stockmanagement.product_service.repository.ProductRepository;

import io.micrometer.core.instrument.Timer;

import com.stockmanagement.product_service.cache.ProductCacheHelper;
import com.stockmanagement.product_service.dto.ProductRequest;
import com.stockmanagement.product_service.dto.ProductResponse;
import com.stockmanagement.product_service.entity.Product;
import com.stockmanagement.product_service.exception.ProductAlreadyExistsException;
import com.stockmanagement.product_service.exception.ProductNotFoundException;
import com.stockmanagement.product_service.metrics.ProductMetrics;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {
    
    private final ProductRepository productRepository;
    private final ProductCacheHelper cacheHelper;
    private final ProductMetrics metrics; //custom metric for service
    //@Transactional @Cacheput don't use it same method causes conflict
    
    @Transactional
    public ProductResponse createProduct(ProductRequest request) {
        if (productRepository.existsBySku(request.getSku())) {
            throw new ProductAlreadyExistsException("Product with SKU " + request.getSku() + " already exists");
        }
        
        if (request.getBarcode() != null && productRepository.existsByBarcode(request.getBarcode())) {
            throw new ProductAlreadyExistsException("Product with barcode " + request.getBarcode() + " already exists");
        }
        
        Product product = mapToEntity(request);
        Product savedProduct = productRepository.save(product);
        ProductResponse response = mapToResponse(savedProduct);
        
        cacheHelper.cacheProduct(response);
        cacheHelper.evictListCaches();
        
        metrics.incrementProductCreated();
        
        return response;
    }
    
   public ProductResponse getProductById(Long id) {
        Timer.Sample sample = metrics.startTimer();
        
        ProductResponse cached = cacheHelper.getProduct(id);
        if (cached != null) {
            log.debug("Product found in cache: {}", id);
            metrics.incrementCacheHit();  // Cache hit
            metrics.recordQueryTime(sample);
            return cached;
        }
        
        metrics.incrementCacheMiss();  // Cache miss
        
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with id: " + id));
        
        ProductResponse response = mapToResponse(product);
        cacheHelper.cacheProduct(response);
        
        metrics.recordQueryTime(sample);
        
        return response;
    }
    
    public ProductResponse getProductBySku(String sku) {
        ProductResponse cached = cacheHelper.getProductBySku(sku);
        if (cached != null) {
            log.debug("Product found in cache by SKU: {}", sku);
            return cached;
        }
        
        Product product = productRepository.findBySku(sku)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with SKU: " + sku));
        
        ProductResponse response = mapToResponse(product);
        cacheHelper.cacheProduct(response);
        
        return response;
    }
    
    public ProductResponse getProductByBarcode(String barcode) {
        Product product = productRepository.findByBarcode(barcode)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with barcode: " + barcode));
        return mapToResponse(product);
    }
    
    public List<ProductResponse> getAllProducts() {
        List<ProductResponse> cached = cacheHelper.getProductList("products:all");
        if (cached != null) {
            log.debug("All products found in cache");
            return cached;
        }
        
        List<ProductResponse> products = productRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        
        cacheHelper.cacheProductList("products:all", products);
        
        return products;
    }
    
    public List<ProductResponse> getActiveProducts() {
        List<ProductResponse> cached = cacheHelper.getProductList("products:active");
        if (cached != null) {
            log.debug("Active products found in cache");
            return cached;
        }
        
        List<ProductResponse> products = productRepository.findByActiveTrue().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        
        cacheHelper.cacheProductList("products:active", products);
        
        return products;
    }
    
    public List<ProductResponse> getProductsByCategory(String category) {
        String cacheKey = "products:category:" + category;
        List<ProductResponse> cached = cacheHelper.getProductList(cacheKey);
        if (cached != null) {
            log.debug("Products by category found in cache: {}", category);
            return cached;
        }
        
        List<ProductResponse> products = productRepository.findByCategory(category).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        
        cacheHelper.cacheProductList(cacheKey, products);
        
        return products;
    }
    
    @Transactional
    public ProductResponse updateProduct(Long id, ProductRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with id: " + id));
        
        if (!product.getSku().equals(request.getSku()) && productRepository.existsBySku(request.getSku())) {
            throw new ProductAlreadyExistsException("Product with SKU " + request.getSku() + " already exists");
        }
        
        String oldSku = product.getSku();
        updateProductFields(product, request);
        Product updatedProduct = productRepository.save(product);
        ProductResponse response = mapToResponse(updatedProduct);
        
        cacheHelper.evictProduct(id, oldSku);
        cacheHelper.cacheProduct(response);
        cacheHelper.evictListCaches();
        
        return response;
    }
    
    @Transactional
    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with id: " + id));
        
        productRepository.deleteById(id);
        
        cacheHelper.evictProduct(id, product.getSku());
        cacheHelper.evictListCaches();

        metrics.incrementProductDeleted();
    }



    private Product mapToEntity(ProductRequest request) {
        Product product = new Product();
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setSku(request.getSku());
        product.setBarcode(request.getBarcode());
        product.setPrice(request.getPrice());
        product.setCategory(request.getCategory());
        product.setBrand(request.getBrand());
        product.setUnit(request.getUnit());
        product.setActive(request.getActive() != null ? request.getActive() : true);
        return product;
    }
    
    private void updateProductFields(Product product, ProductRequest request) {
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setSku(request.getSku());
        product.setBarcode(request.getBarcode());
        product.setPrice(request.getPrice());
        product.setCategory(request.getCategory());
        product.setBrand(request.getBrand());
        product.setUnit(request.getUnit());
        if (request.getActive() != null) {
            product.setActive(request.getActive());
        }
    }
    
    private ProductResponse mapToResponse(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getSku(),
                product.getBarcode(),
                product.getPrice(),
                product.getCategory(),
                product.getBrand(),
                product.getUnit(),
                product.getActive(),
                product.getCreatedAt(),
                product.getUpdatedAt()
        );
    }
}