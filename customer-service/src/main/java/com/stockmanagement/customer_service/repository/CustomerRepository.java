package com.stockmanagement.customer_service.repository;

import com.stockmanagement.customer_service.entity.Customer;
import com.stockmanagement.customer_service.entity.CustomerSegment;
import com.stockmanagement.customer_service.entity.CustomerStatus;
import com.stockmanagement.customer_service.entity.CustomerType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {
    Optional<Customer> findByCustomerCode(String customerCode);
    Optional<Customer> findByEmail(String email);
    List<Customer> findByStatus(CustomerStatus status);
    List<Customer> findByType(CustomerType type);
    List<Customer> findBySegment(CustomerSegment segment);
    List<Customer> findByLastNameContainingIgnoreCase(String lastName);
    boolean existsByCustomerCode(String customerCode);
    boolean existsByEmail(String email);
}
