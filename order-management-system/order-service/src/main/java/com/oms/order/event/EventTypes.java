package com.oms.order.event;

public final class EventTypes {

    public static final String ORDER_CREATED = "ORDER_CREATED";
    public static final String ORDER_CONFIRMED = "ORDER_CONFIRMED";
    public static final String ORDER_CANCELLED = "ORDER_CANCELLED";
    public static final String PAYMENT_COMPLETED = "PAYMENT_COMPLETED";
    public static final String PAYMENT_FAILED = "PAYMENT_FAILED";

    private EventTypes() {
    }
}
