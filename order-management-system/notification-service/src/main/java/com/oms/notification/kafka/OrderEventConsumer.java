package com.oms.notification.kafka;

import com.oms.notification.event.DomainEvent;
import com.oms.notification.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class OrderEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(OrderEventConsumer.class);

    private final NotificationService notificationService;

    public OrderEventConsumer(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @KafkaListener(topics = "${oms.kafka.topics.order-events:order-events}", groupId = "notification-service")
    public void onOrderEvent(DomainEvent event) {
        log.debug("Received order-events message type={}", event != null ? event.getEventType() : null);
        notificationService.handle(event);
    }

    @KafkaListener(topics = "${oms.kafka.topics.payment-events:payment-events}", groupId = "notification-service")
    public void onPaymentEvent(DomainEvent event) {
        log.debug("Received payment-events message type={}", event != null ? event.getEventType() : null);
        notificationService.handle(event);
    }
}
