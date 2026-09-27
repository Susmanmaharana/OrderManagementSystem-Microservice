package com.oms.inventory.exception;

public class ConcurrentInventoryUpdateException extends RuntimeException {

    public ConcurrentInventoryUpdateException(String message) {
        super(message);
    }
}
