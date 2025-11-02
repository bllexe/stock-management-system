package com.stockmanagement.customer_service.controller;

import com.stockmanagement.customer_service.dto.CustomerAddressRequest;
import com.stockmanagement.customer_service.dto.CustomerAddressResponse;
import com.stockmanagement.customer_service.dto.CustomerRequest;
import com.stockmanagement.customer_service.dto.CustomerResponse;
import com.stockmanagement.customer_service.entity.CustomerSegment;
import com.stockmanagement.customer_service.entity.CustomerStatus;
import com.stockmanagement.customer_service.service.CustomerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/customers")
@RequiredArgsConstructor
public class CustomerController {
    
    private final CustomerService customerService;
    
    @PostMapping
    public ResponseEntity<CustomerResponse> createCustomer(@Valid @RequestBody CustomerRequest request) {
        CustomerResponse response = customerService.createCustomer(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<CustomerResponse> getCustomerById(@PathVariable Long id) {
        CustomerResponse response = customerService.getCustomerById(id);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/code/{customerCode}")
    public ResponseEntity<CustomerResponse> getCustomerByCode(@PathVariable String customerCode) {
        CustomerResponse response = customerService.getCustomerByCode(customerCode);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/email/{email}")
    public ResponseEntity<CustomerResponse> getCustomerByEmail(@PathVariable String email) {
        CustomerResponse response = customerService.getCustomerByEmail(email);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping
    public ResponseEntity<List<CustomerResponse>> getAllCustomers() {
        List<CustomerResponse> responses = customerService.getAllCustomers();
        return ResponseEntity.ok(responses);
    }
    
    @GetMapping("/status/{status}")
    public ResponseEntity<List<CustomerResponse>> getCustomersByStatus(@PathVariable CustomerStatus status) {
        List<CustomerResponse> responses = customerService.getCustomersByStatus(status);
        return ResponseEntity.ok(responses);
    }
    
    @GetMapping("/segment/{segment}")
    public ResponseEntity<List<CustomerResponse>> getCustomersBySegment(@PathVariable CustomerSegment segment) {
        List<CustomerResponse> responses = customerService.getCustomersBySegment(segment);
        return ResponseEntity.ok(responses);
    }
    
    @GetMapping("/search")
    public ResponseEntity<List<CustomerResponse>> searchCustomers(@RequestParam String lastName) {
        List<CustomerResponse> responses = customerService.searchCustomersByLastName(lastName);
        return ResponseEntity.ok(responses);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<CustomerResponse> updateCustomer(
            @PathVariable Long id,
            @Valid @RequestBody CustomerRequest request) {
        CustomerResponse response = customerService.updateCustomer(id, request);
        return ResponseEntity.ok(response);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCustomer(@PathVariable Long id) {
        customerService.deleteCustomer(id);
        return ResponseEntity.noContent().build();
    }
    
    @PutMapping("/{id}/balance")
    public ResponseEntity<Void> updateBalance(
            @PathVariable Long id,
            @RequestParam BigDecimal amount) {
        customerService.updateBalance(id, amount);
        return ResponseEntity.ok().build();
    }
    
    @PutMapping("/{id}/loyalty-points")
    public ResponseEntity<Void> addLoyaltyPoints(
            @PathVariable Long id,
            @RequestParam Integer points) {
        customerService.addLoyaltyPoints(id, points);
        return ResponseEntity.ok().build();
    }
    
    @PostMapping("/{customerId}/addresses")
    public ResponseEntity<CustomerAddressResponse> addAddress(
            @PathVariable Long customerId,
            @Valid @RequestBody CustomerAddressRequest request) {
        CustomerAddressResponse response = customerService.addAddress(customerId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @GetMapping("/{customerId}/addresses")
    public ResponseEntity<List<CustomerAddressResponse>> getCustomerAddresses(@PathVariable Long customerId) {
        List<CustomerAddressResponse> responses = customerService.getCustomerAddresses(customerId);
        return ResponseEntity.ok(responses);
    }
    
    @DeleteMapping("/addresses/{addressId}")
    public ResponseEntity<Void> deleteAddress(@PathVariable Long addressId) {
        customerService.deleteAddress(addressId);
        return ResponseEntity.noContent().build();
    }
}