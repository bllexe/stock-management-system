package com.stockmanagement.customer_service.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderCancelledEvent implements Serializable {
  private Long orderId;
  private String orderNumber;
  private Long customerId;
  private LocalDateTime createdAt;
}
