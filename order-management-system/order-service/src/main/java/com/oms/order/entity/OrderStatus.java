package com.oms.order.entity;

public enum OrderStatus {
    CREATED,
    INVENTORY_RESERVED,
    PAYMENT_PENDING,
    CONFIRMED,
    PAYMENT_FAILED,
    CANCELLED
}
