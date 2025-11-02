package com.stockmanagement.supplier_service.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "suppliers")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Supplier {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String name;
    
    @Column(unique = true, nullable = false)
    private String code;
    
    @Column(unique = true)
    private String taxNumber;
    
    @Column(name = "contact_person")
    private String contactPerson;
    
    private String email;
    
    private String phone;
    
    private String address;
    
    private String city;
    
    private String country;
    
    @Column(name = "postal_code")
    private String postalCode;
    
    @Column(name = "payment_terms")
    private String paymentTerms;
    
    @Column(name = "credit_limit")
    private Double creditLimit;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SupplierStatus status;
    
    @Column(name = "rating")
    private Integer rating;
    
    @Column(length = 1000)
    private String notes;
    
    @OneToMany(mappedBy = "supplier", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SupplierProduct> products = new ArrayList<>();
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}