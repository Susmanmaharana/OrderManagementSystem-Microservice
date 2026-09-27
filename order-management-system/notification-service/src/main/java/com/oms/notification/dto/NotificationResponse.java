package com.oms.notification.dto;

import java.time.LocalDateTime;

public class NotificationResponse {

    private String eventId;
    private String eventType;
    private Long orderId;
    private Long customerId;
    private String message;
    private LocalDateTime processedAt;

    public NotificationResponse() {
    }

    public NotificationResponse(String eventId, String eventType, Long orderId, Long customerId,
                                String message, LocalDateTime processedAt) {
        this.eventId = eventId;
        this.eventType = eventType;
        this.orderId = orderId;
        this.customerId = customerId;
        this.message = message;
        this.processedAt = processedAt;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public LocalDateTime getProcessedAt() {
        return processedAt;
    }

    public void setProcessedAt(LocalDateTime processedAt) {
        this.processedAt = processedAt;
    }
}
