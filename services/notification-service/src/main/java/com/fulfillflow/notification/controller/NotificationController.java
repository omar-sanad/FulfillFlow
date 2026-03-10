package com.fulfillflow.notification.controller;

import com.fulfillflow.notification.NotificationService;
import com.fulfillflow.notification.model.NotificationResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notifications")
@Tag(name = "Notifications", description = "Customer and operator notification log")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    @Operation(summary = "List all notifications")
    public List<NotificationResponse> listAll() {
        return notificationService.listAll();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a notification by id")
    public NotificationResponse getNotification(@PathVariable UUID id) {
        return notificationService.getNotification(id);
    }

    @GetMapping("/by-order/{orderId}")
    @Operation(summary = "List notifications for an order")
    public List<NotificationResponse> listForOrder(@PathVariable UUID orderId) {
        return notificationService.listForOrder(orderId);
    }
}
