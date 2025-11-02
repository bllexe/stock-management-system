package com.stockmanagement.customer_service.repository;

import com.stockmanagement.customer_service.entity.CustomerAddress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CustomerAddressRepository extends JpaRepository<CustomerAddress, Long> {
    List<CustomerAddress> findByCustomerId(Long customerId);
    List<CustomerAddress> findByCustomerIdAndIsDefaultTrue(Long customerId);
}