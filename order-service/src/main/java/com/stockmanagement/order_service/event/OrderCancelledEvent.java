package com.stockmanagement.order_service.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderCancelledEvent implements Serializable {
    private Long orderId;
    private String orderNumber;
    private Long customerId;
    private LocalDateTime cancelledAt;
}
