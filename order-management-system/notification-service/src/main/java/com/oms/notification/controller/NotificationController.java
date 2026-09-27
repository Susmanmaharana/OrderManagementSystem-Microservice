package com.oms.notification.controller;

import com.oms.notification.dto.NotificationResponse;
import com.oms.notification.service.NotificationService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public List<NotificationResponse> listAll() {
        return notificationService.listAll();
    }

    @GetMapping("/order/{orderId}")
    public List<NotificationResponse> listByOrder(@PathVariable Long orderId) {
        return notificationService.listByOrder(orderId);
    }
}
