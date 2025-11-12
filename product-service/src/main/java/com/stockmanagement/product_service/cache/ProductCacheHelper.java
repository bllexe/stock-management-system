package com.stockmanagement.product_service.cache;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import com.stockmanagement.product_service.dto.ProductResponse;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
@Slf4j
public class ProductCacheHelper {
    
    private final RedisTemplate<String, Object> redisTemplate;
    private static final String CACHE_PREFIX = "products:";
    private static final String SKU_PREFIX = "products:sku:";
    private static final String CATEGORY_PREFIX = "products:category:";
    private static final String ALL_PRODUCTS = "products:all";
    private static final String ACTIVE_PRODUCTS = "products:active";
    private static final long TTL_HOURS = 1;
    
    public void cacheProduct(ProductResponse product) {
        try {
            String key = CACHE_PREFIX + product.getId();
            redisTemplate.opsForValue().set(key, product, TTL_HOURS, TimeUnit.HOURS);
            
            String skuKey = SKU_PREFIX + product.getSku();
            redisTemplate.opsForValue().set(skuKey, product, TTL_HOURS, TimeUnit.HOURS);
            
            log.debug("Cached product: {}", product.getId());
        } catch (Exception e) {
            log.error("Error caching product: {}", product.getId(), e);
        }
    }
    
    public ProductResponse getProduct(Long id) {
        try {
            String key = CACHE_PREFIX + id;
            return (ProductResponse) redisTemplate.opsForValue().get(key);
        } catch (Exception e) {
            log.error("Error getting product from cache: {}", id, e);
            return null;
        }
    }
    
    public ProductResponse getProductBySku(String sku) {
        try {
            String key = SKU_PREFIX + sku;
            return (ProductResponse) redisTemplate.opsForValue().get(key);
        } catch (Exception e) {
            log.error("Error getting product by SKU from cache: {}", sku, e);
            return null;
        }
    }
    
    public void evictProduct(Long id, String sku) {
        try {
            redisTemplate.delete(CACHE_PREFIX + id);
            if (sku != null) {
                redisTemplate.delete(SKU_PREFIX + sku);
            }
            evictListCaches();
            log.debug("Evicted product: {}", id);
        } catch (Exception e) {
            log.error("Error evicting product: {}", id, e);
        }
    }
    
    public void evictListCaches() {
        try {
            redisTemplate.delete(ALL_PRODUCTS);
            redisTemplate.delete(ACTIVE_PRODUCTS);
            
            var keys = redisTemplate.keys(CATEGORY_PREFIX + "*");
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
            }
            log.debug("Evicted list caches");
        } catch (Exception e) {
            log.error("Error evicting list caches", e);
        }
    }
    
    public void cacheProductList(String key, List<ProductResponse> products) {
        try {
            redisTemplate.opsForValue().set(key, products, TTL_HOURS, TimeUnit.HOURS);
            log.debug("Cached product list: {}", key);
        } catch (Exception e) {
            log.error("Error caching product list: {}", key, e);
        }
    }
    
    @SuppressWarnings("unchecked")
    public List<ProductResponse> getProductList(String key) {
        try {
            return (List<ProductResponse>) redisTemplate.opsForValue().get(key);
        } catch (Exception e) {
            log.error("Error getting product list from cache: {}", key, e);
            return null;
        }
    }
}