package com.fulfillflow.delivery.controller;

import com.fulfillflow.delivery.DeliveryService;
import com.fulfillflow.delivery.model.DeliveryResponse;
import com.fulfillflow.delivery.model.FailDeliveryRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/deliveries")
@Tag(name = "Deliveries", description = "Courier dispatch and delivery lifecycle")
public class DeliveryController {

    private final DeliveryService deliveryService;

    public DeliveryController(DeliveryService deliveryService) {
        this.deliveryService = deliveryService;
    }

    @GetMapping
    @Operation(summary = "List all deliveries")
    public List<DeliveryResponse> listDeliveries() {
        return deliveryService.listAll();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a delivery by id")
    public DeliveryResponse getDelivery(@PathVariable UUID id) {
        return deliveryService.getDelivery(id);
    }

    @GetMapping("/by-order/{orderId}")
    @Operation(summary = "Get the delivery for an order")
    public DeliveryResponse getByOrder(@PathVariable UUID orderId) {
        return deliveryService.getByOrder(orderId);
    }

    @PostMapping("/{id}/pickup")
    @Operation(summary = "Mark a delivery as picked up / in transit")
    public DeliveryResponse pickup(@PathVariable UUID id) {
        return deliveryService.markInTransit(id);
    }

    @PostMapping("/{id}/complete")
    @Operation(summary = "Mark a delivery as completed")
    public ResponseEntity<DeliveryResponse> complete(@PathVariable UUID id) {
        return ResponseEntity.ok(deliveryService.markCompleted(id));
    }

    @PostMapping("/{id}/fail")
    @Operation(summary = "Mark a delivery as failed")
    public DeliveryResponse fail(@PathVariable UUID id, @RequestBody FailDeliveryRequest request) {
        return deliveryService.markFailed(id, request.reason());
    }
}
