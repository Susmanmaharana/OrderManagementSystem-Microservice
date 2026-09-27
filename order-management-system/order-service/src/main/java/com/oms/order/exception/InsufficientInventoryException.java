package com.oms.order.exception;

public class InsufficientInventoryException extends RuntimeException {

    public InsufficientInventoryException(String message) {
        super(message);
    }
}
