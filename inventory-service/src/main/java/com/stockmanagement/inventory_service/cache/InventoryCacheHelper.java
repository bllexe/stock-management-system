package com.stockmanagement.inventory_service.cache;

import com.stockmanagement.inventory_service.dto.InventoryResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
@Slf4j
public class InventoryCacheHelper {
    
    private final RedisTemplate<String, Object> redisTemplate;
    private static final String CACHE_PREFIX = "inventory:";
    private static final String LOCK_PREFIX = "lock:inventory:";
    private static final long TTL_MINUTES = 5;
    private static final long LOCK_TTL_SECONDS = 10;
    
    public void cacheInventory(InventoryResponse inventory) {
        try {
            String key = buildKey(inventory.getProductId(), inventory.getWarehouseId());
            redisTemplate.opsForValue().set(key, inventory, TTL_MINUTES, TimeUnit.MINUTES);
            log.debug("Cached inventory: product={}, warehouse={}", 
                    inventory.getProductId(), inventory.getWarehouseId());
        } catch (Exception e) {
            log.error("Error caching inventory", e);
        }
    }
    
    public InventoryResponse getInventory(Long productId, Long warehouseId) {
        try {
            String key = buildKey(productId, warehouseId);
            return (InventoryResponse) redisTemplate.opsForValue().get(key);
        } catch (Exception e) {
            log.error("Error getting inventory from cache", e);
            return null;
        }
    }
    
    public void evictInventory(Long productId, Long warehouseId) {
        try {
            String key = buildKey(productId, warehouseId);
            redisTemplate.delete(key);
            log.debug("Evicted inventory: product={}, warehouse={}", productId, warehouseId);
        } catch (Exception e) {
            log.error("Error evicting inventory", e);
        }
    }
    
    public boolean acquireLock(Long productId, Long warehouseId) {
        try {
            String lockKey = LOCK_PREFIX + buildKey(productId, warehouseId);
            Boolean acquired = redisTemplate.opsForValue()
                    .setIfAbsent(lockKey, "LOCKED", LOCK_TTL_SECONDS, TimeUnit.SECONDS);
            
            if (Boolean.TRUE.equals(acquired)) {
                log.debug("Lock acquired: product={}, warehouse={}", productId, warehouseId);
                return true;
            }
            log.debug("Lock not acquired: product={}, warehouse={}", productId, warehouseId);
            return false;
        } catch (Exception e) {
            log.error("Error acquiring lock", e);
            return false;
        }
    }
    
    public void releaseLock(Long productId, Long warehouseId) {
        try {
            String lockKey = LOCK_PREFIX + buildKey(productId, warehouseId);
            redisTemplate.delete(lockKey);
            log.debug("Lock released: product={}, warehouse={}", productId, warehouseId);
        } catch (Exception e) {
            log.error("Error releasing lock", e);
        }
    }
    
    public Integer getAvailableQuantity(Long productId, Long warehouseId) {
        try {
            InventoryResponse inventory = getInventory(productId, warehouseId);
            return inventory != null ? inventory.getAvailableQuantity() : null;
        } catch (Exception e) {
            log.error("Error getting available quantity from cache", e);
            return null;
        }
    }
    
    public void updateQuantityInCache(Long productId, Long warehouseId, Integer quantity, Integer reservedQuantity) {
        try {
            InventoryResponse cached = getInventory(productId, warehouseId);
            if (cached != null) {
                cached.setQuantity(quantity);
                cached.setReservedQuantity(reservedQuantity);
                cached.setAvailableQuantity(quantity - reservedQuantity);
                cacheInventory(cached);
                log.debug("Updated quantity in cache: product={}, warehouse={}", productId, warehouseId);
            }
        } catch (Exception e) {
            log.error("Error updating quantity in cache", e);
        }
    }
    
    private String buildKey(Long productId, Long warehouseId) {
        return productId + ":" + warehouseId;
    }
}