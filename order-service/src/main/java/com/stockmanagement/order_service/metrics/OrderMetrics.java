package com.stockmanagement.order_service.metrics;



import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;

@Component
public class OrderMetrics {
    
    private final Counter orderCreatedCounter;
    private final Counter orderCancelledCounter;
    private final Counter orderFailedCounter;
    private final Timer orderProcessingTimer;
    
    public OrderMetrics(MeterRegistry registry) {
        this.orderCreatedCounter = Counter.builder("order.created")
                .description("Total orders created")
                .register(registry);
        
        this.orderCancelledCounter = Counter.builder("order.cancelled")
                .description("Total orders cancelled")
                .register(registry);
        
        this.orderFailedCounter = Counter.builder("order.failed")
                .description("Total orders failed")
                .register(registry);
        
        this.orderProcessingTimer = Timer.builder("order.processing.time")
                .description("Order processing duration")
                .register(registry);
    }
    
    public void incrementOrderCreated() {
        orderCreatedCounter.increment();
    }
    
    public void incrementOrderCancelled() {
        orderCancelledCounter.increment();
    }
    
    public void incrementOrderFailed() {
        orderFailedCounter.increment();
    }
    
    public Timer.Sample startTimer() {
        return Timer.start();
    }
    
    public void recordProcessingTime(Timer.Sample sample) {
        sample.stop(orderProcessingTimer);
    }
}