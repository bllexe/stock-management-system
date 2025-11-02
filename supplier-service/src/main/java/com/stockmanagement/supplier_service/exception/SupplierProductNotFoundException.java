package com.stockmanagement.supplier_service.exception;

public class SupplierProductNotFoundException extends RuntimeException{
    public SupplierProductNotFoundException(String message){
        super(message);
    }

}
