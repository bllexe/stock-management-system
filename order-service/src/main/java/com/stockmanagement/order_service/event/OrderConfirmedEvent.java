package com.stockmanagement.order_service.event;

import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderConfirmedEvent implements Serializable{

    private Long orderId;
    private String orderNumber;
    private Long customerId;
    private LocalDateTime createdAt;

}
