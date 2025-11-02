package com.stockmanagement.customer_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import com.stockmanagement.customer_service.entity.CustomerType;
import com.stockmanagement.customer_service.entity.CustomerStatus;
import com.stockmanagement.customer_service.entity.CustomerSegment;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomerResponse {
    private Long id;
    private String customerCode;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String taxNumber;
    private CustomerType type;
    private CustomerStatus status;
    private BigDecimal creditLimit;
    private BigDecimal currentBalance;
    private Integer loyaltyPoints;
    private CustomerSegment segment;
    private String notes;
    private List<CustomerAddressResponse> addresses;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}