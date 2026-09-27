package com.oms.notification.service;

import com.oms.notification.dto.NotificationResponse;
import com.oms.notification.entity.ProcessedEvent;
import com.oms.notification.event.DomainEvent;
import com.oms.notification.repository.ProcessedEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final ProcessedEventRepository processedEventRepository;

    public NotificationService(ProcessedEventRepository processedEventRepository) {
        this.processedEventRepository = processedEventRepository;
    }

    @Transactional
    public void handle(DomainEvent event) {
        if (event == null || event.getEventId() == null) {
            log.warn("Ignoring event without eventId");
            return;
        }

        if (processedEventRepository.existsById(event.getEventId())) {
            log.info("Duplicate event ignored eventId={} type={}", event.getEventId(), event.getEventType());
            return;
        }

        String message = buildMessage(event);
        sendNotification(event, message);

        ProcessedEvent processed = new ProcessedEvent();
        processed.setEventId(event.getEventId());
        processed.setEventType(event.getEventType());
        processed.setOrderId(event.getOrderId());
        processed.setCustomerId(event.getCustomerId());
        processed.setMessage(message);
        processedEventRepository.save(processed);
    }

    @Transactional(readOnly = true)
    public List<NotificationResponse> listAll() {
        return toResponses(processedEventRepository.findAllByOrderByProcessedAtDesc());
    }

    @Transactional(readOnly = true)
    public List<NotificationResponse> listByOrder(Long orderId) {
        return toResponses(processedEventRepository.findByOrderIdOrderByProcessedAtDesc(orderId));
    }

    private void sendNotification(DomainEvent event, String message) {
        log.info("Notification sent to customer {} | Order {} | {} | {}",
                event.getCustomerId(),
                event.getOrderId(),
                event.getEventType(),
                message);
    }

    private String buildMessage(DomainEvent event) {
        return "Notification for order " + event.getOrderId()
                + " (" + event.getEventType() + ")"
                + (event.getPayload() != null ? " payload=" + event.getPayload() : "");
    }

    private List<NotificationResponse> toResponses(List<ProcessedEvent> events) {
        List<NotificationResponse> responses = new ArrayList<NotificationResponse>();
        for (ProcessedEvent event : events) {
            responses.add(new NotificationResponse(
                    event.getEventId(),
                    event.getEventType(),
                    event.getOrderId(),
                    event.getCustomerId(),
                    event.getMessage(),
                    event.getProcessedAt()));
        }
        return responses;
    }
}
