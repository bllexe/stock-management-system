package com.stockmanagement.api_gateway.filters;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class LoggingFilter implements GlobalFilter, Ordered {
    
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String traceId = exchange.getRequest().getHeaders().getFirst("X-B3-TraceId");
        String spanId = exchange.getRequest().getHeaders().getFirst("X-B3-SpanId");
        
        log.info("Request: {} {} [TraceId: {}, SpanId: {}]", 
                exchange.getRequest().getMethod(), 
                exchange.getRequest().getURI(),
                traceId,
                spanId);
        
        return chain.filter(exchange).then(Mono.fromRunnable(() -> {
            log.info("Response Status: {} [TraceId: {}]", 
                    exchange.getResponse().getStatusCode(),
                    traceId);
        }));
    }
    
    @Override
    public int getOrder() {
        return -1;
    }
}