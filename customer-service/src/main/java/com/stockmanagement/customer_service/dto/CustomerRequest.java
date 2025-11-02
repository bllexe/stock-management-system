package com.stockmanagement.customer_service.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import com.stockmanagement.customer_service.entity.CustomerType;
import com.stockmanagement.customer_service.entity.CustomerStatus;
import com.stockmanagement.customer_service.entity.CustomerSegment;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomerRequest {
    
    @NotBlank(message = "Customer code is required")
    private String customerCode;

    @NotBlank(message = "First name is required")
    private String firstName;
    
    @NotBlank(message = "Last name is required")
    private String lastName;
    
    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    private String email;
    
    private String phone;
    
    private String taxNumber;
    
    @NotNull(message = "Customer type is required")
    private CustomerType type;
    
    @NotNull(message = "Customer status is required")
    private CustomerStatus status;
    
    private BigDecimal creditLimit;
    
    @NotNull(message = "Customer segment is required")
    private CustomerSegment segment;
    
    private String notes;
}
