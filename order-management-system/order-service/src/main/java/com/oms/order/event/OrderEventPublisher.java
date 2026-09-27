package com.oms.order.event;

import com.oms.order.entity.Order;

public interface OrderEventPublisher {

    void publishOrderEvent(String eventType, Order order);

    void publishPaymentEvent(String eventType, Order order, String paymentId);
}
