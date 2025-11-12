package com.stockmanagement.order_service.service;


import com.stockmanagement.order_service.client.InventoryClient;
import com.stockmanagement.order_service.client.ProductClient;
import com.stockmanagement.order_service.dto.*;
import com.stockmanagement.order_service.entity.Order;
import com.stockmanagement.order_service.entity.OrderItem;
import com.stockmanagement.order_service.entity.OrderStatus;
import com.stockmanagement.order_service.event.OrderCancelledEvent;
import com.stockmanagement.order_service.event.OrderConfirmedEvent;
import com.stockmanagement.order_service.event.OrderCreatedEvent;
import com.stockmanagement.order_service.event.OrderItemEvent;
import com.stockmanagement.order_service.exception.InsufficientStockException;
import com.stockmanagement.order_service.exception.OrderNotFoundException;
import com.stockmanagement.order_service.exception.ProductNotFoundException;
import com.stockmanagement.order_service.metrics.OrderMetrics;
import com.stockmanagement.order_service.publisher.OrderEventPublisher;
import com.stockmanagement.order_service.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {
    
    private final OrderRepository orderRepository;
    private final ProductClient productClient;
    private final InventoryClient inventoryClient;
    private final OrderEventPublisher eventPublisher;
    private final OrderMetrics orderMetrics;
    
    @Transactional
    public OrderResponse createOrder(OrderRequest request) {
        log.info("Creating order for customer: {}", request.getCustomerId());
        
        Order order = new Order();
        order.setOrderNumber(generateOrderNumber());
        order.setCustomerId(request.getCustomerId());
        order.setWarehouseId(request.getWarehouseId());
        order.setStatus(OrderStatus.PENDING);
        order.setNotes(request.getNotes());
        
        BigDecimal totalAmount = BigDecimal.ZERO;
        
        for (OrderItemRequest itemRequest : request.getItems()) {
            ProductDto product = productClient.getProductById(itemRequest.getProductId());
            
            if (product == null || product.getId() == null) {
                throw new ProductNotFoundException("Product not found: " + itemRequest.getProductId());
            }
            
            InventoryDto inventory = inventoryClient.getInventory(
                    itemRequest.getProductId(), 
                    request.getWarehouseId()
            );
            
            if (inventory.getAvailableQuantity() < itemRequest.getQuantity()) {
                throw new InsufficientStockException(
                        "Insufficient stock for product: " + product.getName() + 
                        ". Available: " + inventory.getAvailableQuantity() + 
                        ", Required: " + itemRequest.getQuantity()
                );
            }
            
            OrderItem orderItem = new OrderItem();
            orderItem.setProductId(product.getId());
            orderItem.setProductName(product.getName());
            orderItem.setProductSku(product.getSku());
            orderItem.setQuantity(itemRequest.getQuantity());
            orderItem.setUnitPrice(product.getPrice());
            orderItem.setTotalPrice(product.getPrice().multiply(BigDecimal.valueOf(itemRequest.getQuantity())));
            
            order.addItem(orderItem);
            totalAmount = totalAmount.add(orderItem.getTotalPrice());
        }
        
        order.setTotalAmount(totalAmount);
        Order savedOrder = orderRepository.save(order);
        
        boolean reservationSuccess = reserveInventory(savedOrder);
        
        if (!reservationSuccess) {
            order.setStatus(OrderStatus.FAILED);
            orderRepository.save(order);
            throw new InsufficientStockException("Failed to reserve inventory for order");
        }
        
        order.setStatus(OrderStatus.CONFIRMED);
        Order confirmedOrder = orderRepository.save(order);
        
        publishOrderCreatedEvent(confirmedOrder);
        publishOrderConfirmedEvent(confirmedOrder);
        
        log.info("Order created successfully: {}", confirmedOrder.getOrderNumber());
        return mapToResponse(confirmedOrder);
    }
    
    private boolean reserveInventory(Order order) {
        log.info("Reserving inventory for order: {}", order.getOrderNumber());
        
        for (OrderItem item : order.getItems()) {
            Boolean reserved = inventoryClient.reserveStock(
                    item.getProductId(),
                    order.getWarehouseId(),
                    item.getQuantity()
            );
            
            if (!reserved) {
                log.error("Failed to reserve stock for product: {}", item.getProductId());
                rollbackInventoryReservation(order);
                return false;
            }
        }
        
        return true;
    }
    
    private void rollbackInventoryReservation(Order order) {
        log.info("Rolling back inventory reservation for order: {}", order.getOrderNumber());
        
        for (OrderItem item : order.getItems()) {
            try {
                inventoryClient.releaseStock(
                        item.getProductId(),
                        order.getWarehouseId(),
                        item.getQuantity()
                );
            } catch (Exception e) {
                log.error("Failed to release stock during rollback for product: {}", item.getProductId(), e);
               // orderMetrics.incrementOrderFailed();
            }
        }
    }
    
    public OrderResponse getOrderById(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException("Order not found with id: " + id));
        return mapToResponse(order);
    }
    
    public OrderResponse getOrderByNumber(String orderNumber) {
        Order order = orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new OrderNotFoundException("Order not found with number: " + orderNumber));
        return mapToResponse(order);
    }
    
    public List<OrderResponse> getOrdersByCustomer(Long customerId) {
        return orderRepository.findByCustomerId(customerId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    public List<OrderResponse> getAllOrders() {
        return orderRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    @Transactional
    public OrderResponse updateOrderStatus(Long id, OrderStatus status) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException("Order not found with id: " + id));
        
        order.setStatus(status);
        Order updatedOrder = orderRepository.save(order);
        
        log.info("Order {} status updated to {}", order.getOrderNumber(), status);
        return mapToResponse(updatedOrder);
    }
    
    @Transactional
    public void cancelOrder(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException("Order not found with id: " + id));
        
        if (order.getStatus() == OrderStatus.SHIPPED || order.getStatus() == OrderStatus.DELIVERED) {
            throw new IllegalStateException("Cannot cancel order in status: " + order.getStatus());
        }
        
        rollbackInventoryReservation(order);
        
        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);
        
        publishOrderCancelledEvent(order);
        
        log.info("Order cancelled: {}", order.getOrderNumber());
    }
    
    private void publishOrderCreatedEvent(Order order) {
        List<OrderItemEvent> itemEvents = order.getItems().stream()
                .map(item -> new OrderItemEvent(
                        item.getProductId(),
                        item.getProductName(),
                        item.getQuantity(),
                        item.getUnitPrice(),
                        item.getTotalPrice()
                ))
                .collect(Collectors.toList());
        
        OrderCreatedEvent event = new OrderCreatedEvent(
                order.getId(),
                order.getOrderNumber(),
                order.getCustomerId(),
                order.getWarehouseId(),
                order.getTotalAmount(),
                itemEvents,
                order.getCreatedAt()
        );
        
        eventPublisher.publishOrderCreatedEvent(event);
    }
    
    private void publishOrderConfirmedEvent(Order order) {
        OrderConfirmedEvent event = new OrderConfirmedEvent(
                order.getId(),
                order.getOrderNumber(),
                order.getCustomerId(),
                LocalDateTime.now()
        );
        
        eventPublisher.publishOrderConfirmedEvent(event);
    }
    
    private void publishOrderCancelledEvent(Order order) {
        OrderCancelledEvent event = new OrderCancelledEvent(
                order.getId(),
                order.getOrderNumber(),
                order.getCustomerId(),
                LocalDateTime.now()
        );
        
        eventPublisher.publishOrderCancelledEvent(event);
    }
    
    private String generateOrderNumber() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        return "ORD-" + timestamp;
    }
    
    private OrderResponse mapToResponse(Order order) {
        List<OrderItemResponse> itemResponses = order.getItems().stream()
                .map(item -> new OrderItemResponse(
                        item.getId(),
                        item.getProductId(),
                        item.getProductName(),
                        item.getProductSku(),
                        item.getQuantity(),
                        item.getUnitPrice(),
                        item.getTotalPrice()
                ))
                .collect(Collectors.toList());
        
        return new OrderResponse(
                order.getId(),
                order.getOrderNumber(),
                order.getCustomerId(),
                order.getWarehouseId(),
                order.getStatus(),
                order.getTotalAmount(),
                order.getNotes(),
                itemResponses,
                order.getCreatedAt(),
                order.getUpdatedAt()
        );
    }
}