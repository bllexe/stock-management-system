package com.stockmanagement.customer_service.service;

import com.stockmanagement.customer_service.dto.CustomerAddressRequest;
import com.stockmanagement.customer_service.dto.CustomerAddressResponse;
import com.stockmanagement.customer_service.dto.CustomerRequest;
import com.stockmanagement.customer_service.dto.CustomerResponse;
import com.stockmanagement.customer_service.entity.*;
import com.stockmanagement.customer_service.exception.CustomerAlreadyExistsException;
import com.stockmanagement.customer_service.exception.CustomerNotFoundException;
import com.stockmanagement.customer_service.repository.CustomerAddressRepository;
import com.stockmanagement.customer_service.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomerService {
    
    private final CustomerRepository customerRepository;
    private final CustomerAddressRepository customerAddressRepository;
    
    @Transactional
    public CustomerResponse createCustomer(CustomerRequest request) {
        if (customerRepository.existsByCustomerCode(request.getCustomerCode())) {
            throw new CustomerAlreadyExistsException("Customer with code " + request.getCustomerCode() + " already exists");
        }
        
        if (customerRepository.existsByEmail(request.getEmail())) {
            throw new CustomerAlreadyExistsException("Customer with email " + request.getEmail() + " already exists");
        }
        
        Customer customer = mapToEntity(request);
        Customer savedCustomer = customerRepository.save(customer);
        
        log.info("Customer created: {}", savedCustomer.getCustomerCode());
        return mapToResponse(savedCustomer);
    }
    
    public CustomerResponse getCustomerById(Long id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found with id: " + id));
        return mapToResponse(customer);
    }
    
    public CustomerResponse getCustomerByCode(String customerCode) {
        Customer customer = customerRepository.findByCustomerCode(customerCode)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found with code: " + customerCode));
        return mapToResponse(customer);
    }
    
    public CustomerResponse getCustomerByEmail(String email) {
        Customer customer = customerRepository.findByEmail(email)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found with email: " + email));
        return mapToResponse(customer);
    }
    
    public List<CustomerResponse> getAllCustomers() {
        return customerRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    public List<CustomerResponse> getCustomersByStatus(CustomerStatus status) {
        return customerRepository.findByStatus(status).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    public List<CustomerResponse> getCustomersBySegment(CustomerSegment segment) {
        return customerRepository.findBySegment(segment).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    public List<CustomerResponse> searchCustomersByLastName(String lastName) {
        return customerRepository.findByLastNameContainingIgnoreCase(lastName).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    @Transactional
    public CustomerResponse updateCustomer(Long id, CustomerRequest request) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found with id: " + id));
        
        if (!customer.getCustomerCode().equals(request.getCustomerCode()) && 
            customerRepository.existsByCustomerCode(request.getCustomerCode())) {
            throw new CustomerAlreadyExistsException("Customer with code " + request.getCustomerCode() + " already exists");
        }
        
        updateCustomerFields(customer, request);
        Customer updatedCustomer = customerRepository.save(customer);
        
        log.info("Customer updated: {}", updatedCustomer.getCustomerCode());
        return mapToResponse(updatedCustomer);
    }
    
    @Transactional
    public void deleteCustomer(Long id) {
        if (!customerRepository.existsById(id)) {
            throw new CustomerNotFoundException("Customer not found with id: " + id);
        }
        customerRepository.deleteById(id);
        log.info("Customer deleted with id: {}", id);
    }
    
    @Transactional
    public void updateBalance(Long id, BigDecimal amount) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found with id: " + id));
        
        customer.setCurrentBalance(customer.getCurrentBalance().add(amount));
        customerRepository.save(customer);
        log.info("Customer {} balance updated by {}", customer.getCustomerCode(), amount);
    }
    
    @Transactional
    public void addLoyaltyPoints(Long id, Integer points) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found with id: " + id));
        
        customer.setLoyaltyPoints(customer.getLoyaltyPoints() + points);
        customerRepository.save(customer);
        log.info("Customer {} loyalty points updated by {}", customer.getCustomerCode(), points);
    }
    
    @Transactional
    public CustomerAddressResponse addAddress(Long customerId, CustomerAddressRequest request) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found with id: " + customerId));
        
        if (request.getIsDefault() != null && request.getIsDefault()) {
            customer.getAddresses().forEach(addr -> addr.setIsDefault(false));
        }
        
        CustomerAddress address = new CustomerAddress();
        address.setType(request.getType());
        address.setAddressLine1(request.getAddressLine1());
        address.setAddressLine2(request.getAddressLine2());
        address.setCity(request.getCity());
        address.setState(request.getState());
        address.setCountry(request.getCountry());
        address.setPostalCode(request.getPostalCode());
        address.setIsDefault(request.getIsDefault() != null ? request.getIsDefault() : false);
        
        customer.addAddress(address);
        customerRepository.save(customer);
        
        log.info("Address added for customer: {}", customer.getCustomerCode());
        return mapToAddressResponse(address);
    }
    
    public List<CustomerAddressResponse> getCustomerAddresses(Long customerId) {
        return customerAddressRepository.findByCustomerId(customerId).stream()
                .map(this::mapToAddressResponse)
                .collect(Collectors.toList());
    }
    
    @Transactional
    public void deleteAddress(Long addressId) {
        customerAddressRepository.deleteById(addressId);
        log.info("Address deleted with id: {}", addressId);
    }
    
    private Customer mapToEntity(CustomerRequest request) {
        Customer customer = new Customer();
        customer.setCustomerCode(request.getCustomerCode());
        customer.setFirstName(request.getFirstName());
        customer.setLastName(request.getLastName());
        customer.setEmail(request.getEmail());
        customer.setPhone(request.getPhone());
        customer.setTaxNumber(request.getTaxNumber());
        customer.setType(request.getType());
        customer.setStatus(request.getStatus());
        customer.setCreditLimit(request.getCreditLimit());
        customer.setCurrentBalance(BigDecimal.ZERO);
        customer.setLoyaltyPoints(0);
        customer.setSegment(request.getSegment());
        customer.setNotes(request.getNotes());
        return customer;
    }
    
    private void updateCustomerFields(Customer customer, CustomerRequest request) {
        customer.setCustomerCode(request.getCustomerCode());
        customer.setFirstName(request.getFirstName());
        customer.setLastName(request.getLastName());
        customer.setEmail(request.getEmail());
        customer.setPhone(request.getPhone());
        customer.setTaxNumber(request.getTaxNumber());
        customer.setType(request.getType());
        customer.setStatus(request.getStatus());
        customer.setCreditLimit(request.getCreditLimit());
        customer.setSegment(request.getSegment());
        customer.setNotes(request.getNotes());
    }
    
    private CustomerResponse mapToResponse(Customer customer) {
        List<CustomerAddressResponse> addressResponses = customer.getAddresses().stream()
                .map(this::mapToAddressResponse)
                .collect(Collectors.toList());
        
        return new CustomerResponse(
                customer.getId(),
                customer.getCustomerCode(),
                customer.getFirstName(),
                customer.getLastName(),
                customer.getEmail(),
                customer.getPhone(),
                customer.getTaxNumber(),
                customer.getType(),
                customer.getStatus(),
                customer.getCreditLimit(),
                customer.getCurrentBalance(),
                customer.getLoyaltyPoints(),
                customer.getSegment(),
                customer.getNotes(),
                addressResponses,
                customer.getCreatedAt(),
                customer.getUpdatedAt()
        );
    }
    
    private CustomerAddressResponse mapToAddressResponse(CustomerAddress address) {
        return new CustomerAddressResponse(
                address.getId(),
                address.getType(),
                address.getAddressLine1(),
                address.getAddressLine2(),
                address.getCity(),
                address.getState(),
                address.getCountry(),
                address.getPostalCode(),
                address.getIsDefault(),
                address.getCreatedAt(),
                address.getUpdatedAt()
        );
    }
}