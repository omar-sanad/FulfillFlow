package com.fulfillflow.order.controller;

import com.fulfillflow.order.domain.OrderService;
import com.fulfillflow.order.model.CreateOrderRequest;
import com.fulfillflow.order.model.OrderResponse;
import com.fulfillflow.order.model.OrderStatusHistoryResponse;
import com.fulfillflow.order.model.TransitionOrderRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orders")
@Tag(name = "Orders", description = "Customer order placement and lifecycle")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    @Operation(summary = "Place a new order")
    public ResponseEntity<OrderResponse> createOrder(@Valid @RequestBody CreateOrderRequest request) {
        OrderResponse response = orderService.createOrder(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get an order by id (own orders or operator)")
    public OrderResponse getOrder(@PathVariable UUID id) {
        return orderService.getOrder(id);
    }

    @GetMapping
    @Operation(summary = "List the current user's orders")
    public Page<OrderResponse> listMyOrders(Pageable pageable) {
        return orderService.listMyOrders(pageable);
    }

    @GetMapping("/{id}/timeline")
    @Operation(summary = "Get order status history")
    public List<OrderStatusHistoryResponse> getOrderTimeline(@PathVariable UUID id) {
        return orderService.getOrderTimeline(id);
    }

    @PostMapping("/{id}/transitions")
    @Operation(summary = "Transition an order (pay, fulfill, cancel, fail)")
    public OrderResponse transition(@PathVariable UUID id,
                                   @Valid @RequestBody TransitionOrderRequest request) {
        return orderService.transition(id, request);
    }
}
