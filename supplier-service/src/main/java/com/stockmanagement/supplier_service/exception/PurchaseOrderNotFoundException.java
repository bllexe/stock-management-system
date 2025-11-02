package com.stockmanagement.supplier_service.exception;

public class PurchaseOrderNotFoundException extends RuntimeException{

    public PurchaseOrderNotFoundException(String message) {
        super(message);
    }
}
