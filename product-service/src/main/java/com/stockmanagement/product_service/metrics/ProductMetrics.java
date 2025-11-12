package com.stockmanagement.product_service.metrics;


import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ProductMetrics {
    
    private final Counter productCreatedCounter;
    private final Counter productDeletedCounter;
    private final Counter cacheHitCounter;
    private final Counter cacheMissCounter;
    private final Timer productQueryTimer;
    
    public ProductMetrics(MeterRegistry registry) {
        this.productCreatedCounter = Counter.builder("product.created")
                .description("Total products created")
                .register(registry);
        
        this.productDeletedCounter = Counter.builder("product.deleted")
                .description("Total products deleted")
                .register(registry);
        
        this.cacheHitCounter = Counter.builder("product.cache.hit")
                .description("Cache hit count")
                .register(registry);
        
        this.cacheMissCounter = Counter.builder("product.cache.miss")
                .description("Cache miss count")
                .register(registry);
        
        this.productQueryTimer = Timer.builder("product.query.time")
                .description("Product query duration")
                .register(registry);
    }
    
    public void incrementProductCreated() {
        productCreatedCounter.increment();
    }
    
    public void incrementProductDeleted() {
        productDeletedCounter.increment();
    }
    
    public void incrementCacheHit() {
        cacheHitCounter.increment();
    }
    
    public void incrementCacheMiss() {
        cacheMissCounter.increment();
    }
    
    public Timer.Sample startTimer() {
        return Timer.start();
    }
    
    public void recordQueryTime(Timer.Sample sample) {
        sample.stop(productQueryTimer);
    }
}