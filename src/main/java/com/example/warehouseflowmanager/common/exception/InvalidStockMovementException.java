package com.example.warehouseflowmanager.common.exception;

public class InvalidStockMovementException extends RuntimeException {

    public InvalidStockMovementException(String message) {
        super(message);
    }
}