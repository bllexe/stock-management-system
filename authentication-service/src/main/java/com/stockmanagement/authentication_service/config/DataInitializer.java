package com.stockmanagement.authentication_service.config;

import com.stockmanagement.authentication_service.entity.Role;
import com.stockmanagement.authentication_service.entity.RoleName;
import com.stockmanagement.authentication_service.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {
    
    private final RoleRepository roleRepository;
    
    @Override
    public void run(String... args) throws Exception {
        if (roleRepository.count() == 0) {
            Role userRole = new Role(null, RoleName.ROLE_USER, "Standard user role");
            Role adminRole = new Role(null, RoleName.ROLE_ADMIN, "Administrator role");
            Role managerRole = new Role(null, RoleName.ROLE_MANAGER, "Manager role");
            Role warehouseRole = new Role(null, RoleName.ROLE_WAREHOUSE_STAFF, "Warehouse staff role");
            
            roleRepository.save(userRole);
            roleRepository.save(adminRole);
            roleRepository.save(managerRole);
            roleRepository.save(warehouseRole);
            
            log.info("Roles initialized successfully");
        }
    }
}