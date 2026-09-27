package com.oms.order.event;

import com.oms.order.entity.Order;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "oms.kafka.enabled", havingValue = "false")
public class NoOpOrderEventPublisher implements OrderEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(NoOpOrderEventPublisher.class);

    @Override
    public void publishOrderEvent(String eventType, Order order) {
        log.debug("Kafka disabled - skip order event {} orderId={}", eventType, order.getId());
    }

    @Override
    public void publishPaymentEvent(String eventType, Order order, String paymentId) {
        log.debug("Kafka disabled - skip payment event {} orderId={}", eventType, order.getId());
    }
}
