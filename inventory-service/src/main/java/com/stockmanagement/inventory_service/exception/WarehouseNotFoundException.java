package com.stockmanagement.inventory_service.exception;

public class WarehouseNotFoundException extends RuntimeException {
    public WarehouseNotFoundException(String message) {
        super(message);
    }

    public WarehouseNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}