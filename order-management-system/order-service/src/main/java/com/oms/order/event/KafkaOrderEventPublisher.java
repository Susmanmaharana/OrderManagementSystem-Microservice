package com.oms.order.event;

import com.oms.order.entity.Order;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Component
@ConditionalOnProperty(name = "oms.kafka.enabled", havingValue = "true", matchIfMissing = true)
public class KafkaOrderEventPublisher implements OrderEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(KafkaOrderEventPublisher.class);

    private final KafkaTemplate<String, DomainEvent> kafkaTemplate;
    private final String orderEventsTopic;
    private final String paymentEventsTopic;

    public KafkaOrderEventPublisher(KafkaTemplate<String, DomainEvent> kafkaTemplate,
                                    @Value("${oms.kafka.topics.order-events:order-events}") String orderEventsTopic,
                                    @Value("${oms.kafka.topics.payment-events:payment-events}") String paymentEventsTopic) {
        this.kafkaTemplate = kafkaTemplate;
        this.orderEventsTopic = orderEventsTopic;
        this.paymentEventsTopic = paymentEventsTopic;
    }

    @Override
    public void publishOrderEvent(String eventType, Order order) {
        DomainEvent event = buildEvent(eventType, order, null);
        send(orderEventsTopic, order.getId(), event);
    }

    @Override
    public void publishPaymentEvent(String eventType, Order order, String paymentId) {
        DomainEvent event = buildEvent(eventType, order, paymentId);
        send(paymentEventsTopic, order.getId(), event);
    }

    private DomainEvent buildEvent(String eventType, Order order, String paymentId) {
        DomainEvent event = new DomainEvent();
        event.setEventId(UUID.randomUUID().toString());
        event.setEventType(eventType);
        event.setTimestamp(Instant.now().toString());
        event.setOrderId(order.getId());
        event.setCustomerId(order.getCustomerId());
        Map<String, Object> payload = new HashMap<String, Object>();
        payload.put("status", order.getStatus() != null ? order.getStatus().name() : null);
        payload.put("totalAmount", order.getTotalAmount());
        if (paymentId != null) {
            payload.put("paymentId", paymentId);
        }
        event.setPayload(payload);
        return event;
    }

    private void send(String topic, Long orderId, DomainEvent event) {
        try {
            String key = String.valueOf(orderId);
            kafkaTemplate.send(topic, key, event);
            log.info("Published {} eventId={} orderId={} topic={}",
                    event.getEventType(), event.getEventId(), orderId, topic);
        } catch (Exception ex) {
            // Do not fail the business transaction if Kafka is unavailable
            log.error("Failed to publish {} for orderId={} to topic={}",
                    event.getEventType(), orderId, topic, ex);
        }
    }
}
