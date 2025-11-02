package com.stockmanagement.supplier_service.exception;

public class SupplierProductAlreadyExistsException extends RuntimeException{

    public SupplierProductAlreadyExistsException(String message) {
        super(message);
    }
}
