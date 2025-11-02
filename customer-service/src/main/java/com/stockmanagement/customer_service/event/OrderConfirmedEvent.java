package com.stockmanagement.customer_service.event;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderConfirmedEvent implements Serializable {

  private Long orderId;
  private String orderNumber;
  private Long customerId;
  private LocalDateTime confirmedAt;

}
