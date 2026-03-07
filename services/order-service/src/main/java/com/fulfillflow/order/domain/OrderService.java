package com.fulfillflow.order.domain;

import com.fulfillflow.common.error.NotFoundException;
import com.fulfillflow.common.events.EventTypes;
import com.fulfillflow.common.events.payloads.OrderCancelledPayload;
import com.fulfillflow.common.events.payloads.OrderCreatedPayload;
import com.fulfillflow.common.events.payloads.OrderFulfilledPayload;
import com.fulfillflow.common.events.payloads.OrderPaidPayload;
import com.fulfillflow.common.outbox.OutboxHelper;
import com.fulfillflow.common.security.AuthenticatedUser;
import com.fulfillflow.common.security.SecurityContextHelper;
import com.fulfillflow.order.model.CreateOrderRequest;
import com.fulfillflow.order.model.OrderLineRequest;
import com.fulfillflow.order.model.OrderLineResponse;
import com.fulfillflow.order.model.OrderResponse;
import com.fulfillflow.order.model.OrderStatusHistoryResponse;
import com.fulfillflow.order.model.ShippingAddressRequest;
import com.fulfillflow.order.model.TransitionOrderRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Application service for orders. Owns order creation, state transitions, and
 * customer-scoped queries. A customer may only access their own orders;
 * operators (administrator / warehouse) may access any order.
 */
@Service
@Transactional
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderLineRepository lineRepository;
    private final OrderStatusHistoryRepository historyRepository;
    private final SecurityContextHelper securityContextHelper;
    private final OutboxHelper outboxHelper;

    public OrderService(OrderRepository orderRepository,
                        OrderLineRepository lineRepository,
                        OrderStatusHistoryRepository historyRepository,
                        SecurityContextHelper securityContextHelper,
                        OutboxHelper outboxHelper) {
        this.orderRepository = orderRepository;
        this.lineRepository = lineRepository;
        this.historyRepository = historyRepository;
        this.securityContextHelper = securityContextHelper;
        this.outboxHelper = outboxHelper;
    }

    public OrderResponse createOrder(CreateOrderRequest request) {
        AuthenticatedUser user = securityContextHelper.requireUser();
        UUID customerId = UUID.fromString(user.subject());

        ShippingAddress address = new ShippingAddress(
                request.shippingAddress().fullName(),
                request.shippingAddress().line1(),
                request.shippingAddress().line2(),
                request.shippingAddress().city(),
                request.shippingAddress().postalCode(),
                request.shippingAddress().country(),
                request.shippingAddress().phone());
        String currency = request.currency() != null ? request.currency() : "USD";

        Order order = new Order(customerId, address, request.notes(), currency);
        order = orderRepository.save(order);

        List<OrderLine> lines = new ArrayList<>();
        for (OrderLineRequest lr : request.lines()) {
            OrderLine line = new OrderLine(
                    UUID.randomUUID(), order.getId(), lr.productId(), lr.sku(), lr.name(),
                    lr.unitPriceCents(), lr.quantity(), currency);
            lines.add(line);
        }
        lineRepository.saveAll(lines);
        order.getLines().addAll(lines);
        order.recomputeTotal();
        order = orderRepository.save(order);

        historyRepository.save(new OrderStatusHistory(
                order.getId(), null, OrderStatus.CREATED, "Order placed", user.username()));

        emitOrderCreated(order, lines);
        return toResponse(order, lines);
    }

    private void emitOrderCreated(Order order, List<OrderLine> lines) {
        List<OrderCreatedPayload.OrderLineItem> items = lines.stream()
                .map(l -> new OrderCreatedPayload.OrderLineItem(
                        l.getProductId(), l.getSku(), l.getName(), l.getQuantity(), l.getUnitPriceCents()))
                .toList();
        OrderCreatedPayload payload = new OrderCreatedPayload(
                order.getId(), order.getCustomerId(), items,
                order.getTotalCents(), order.getCurrency(), order.getShippingAddress().fullName());
        outboxHelper.enqueue("Order", order.getId().toString(), EventTypes.ORDER_CREATED,
                "orders.events.v1", order.getId(), payload);
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrder(UUID orderId) {
        Order order = loadOrder(orderId);
        assertAccess(order);
        List<OrderLine> lines = lineRepository.findByOrderId(orderId);
        return toResponse(order, lines);
    }

    @Transactional(readOnly = true)
    public Page<OrderResponse> listMyOrders(Pageable pageable) {
        AuthenticatedUser user = securityContextHelper.requireUser();
        UUID customerId = UUID.fromString(user.subject());
        return orderRepository.findByCustomerId(customerId, pageable).map(o ->
                toResponse(o, lineRepository.findByOrderId(o.getId())));
    }

    @Transactional(readOnly = true)
    public List<OrderStatusHistoryResponse> getOrderTimeline(UUID orderId) {
        Order order = loadOrder(orderId);
        assertAccess(order);
        return historyRepository.findByOrderIdOrderByOccurredAtAsc(orderId).stream()
                .map(h -> new OrderStatusHistoryResponse(
                        h.getFromStatus(), h.getToStatus(), h.getReason(), h.getActor(), h.getOccurredAt()))
                .toList();
    }

    public OrderResponse transition(UUID orderId, TransitionOrderRequest request) {
        Order order = loadOrder(orderId);
        assertAccess(order);
        AuthenticatedUser user = securityContextHelper.requireUser();

        OrderStatus from = order.getStatus();
        OrderStatus target = parseAction(request.action());
        order.transitionTo(target, request.reason());
        order = orderRepository.save(order);
        historyRepository.save(new OrderStatusHistory(
                order.getId(), from, target, request.reason(), user.username()));
        emitTransitionEvent(order, target, request.reason());
        List<OrderLine> lines = lineRepository.findByOrderId(orderId);
        return toResponse(order, lines);
    }

    private void emitTransitionEvent(Order order, OrderStatus target, String reason) {
        String orderId = order.getId().toString();
        switch (target) {
            case PAID -> outboxHelper.enqueue("Order", orderId, EventTypes.ORDER_PAID,
                    "orders.events.v1", order.getId(),
                    new OrderPaidPayload(order.getId(), order.getCustomerId(),
                            order.getTotalCents(), order.getCurrency()));
            case FULFILLED -> outboxHelper.enqueue("Order", orderId, EventTypes.ORDER_FULFILLED,
                    "orders.events.v1", order.getId(),
                    new OrderFulfilledPayload(order.getId(), order.getCustomerId()));
            case CANCELLED -> outboxHelper.enqueue("Order", orderId, EventTypes.ORDER_CANCELLED,
                    "orders.events.v1", order.getId(),
                    new OrderCancelledPayload(order.getId(), order.getCustomerId(), reason));
            default -> { }
        }
    }

    private Order loadOrder(UUID orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("ORDER_NOT_FOUND", "Order " + orderId + " not found"));
    }

    private void assertAccess(Order order) {
        AuthenticatedUser user = securityContextHelper.requireUser();
        boolean isOperator = user.roles().stream()
                .anyMatch(r -> r.equals("administrator") || r.equals("warehouse")
                        || r.equals("ROLE_administrator") || r.equals("ROLE_warehouse"));
        if (isOperator) {
            return;
        }
        UUID customerId = UUID.fromString(user.subject());
        if (!order.getCustomerId().equals(customerId)) {
            throw new NotFoundException("ORDER_NOT_FOUND", "Order " + order.getId() + " not found");
        }
    }

    private OrderStatus parseAction(String action) {
        return switch (action.toLowerCase()) {
            case "pay" -> OrderStatus.PAID;
            case "fulfill" -> OrderStatus.FULFILLED;
            case "cancel" -> OrderStatus.CANCELLED;
            case "fail" -> OrderStatus.FAILED;
            default -> throw new IllegalArgumentException("Unknown action: " + action);
        };
    }

    private OrderResponse toResponse(Order order, List<OrderLine> lines) {
        List<OrderLineResponse> lineResponses = lines.stream()
                .map(l -> new OrderLineResponse(
                        l.getId(), l.getProductId(), l.getSku(), l.getName(),
                        l.getUnitPriceCents(), l.getQuantity(), l.getLineTotalCents(),
                        l.getCurrency(), l.getCreatedAt()))
                .toList();
        ShippingAddressRequest addr = new ShippingAddressRequest(
                order.getShippingAddress().fullName(),
                order.getShippingAddress().line1(),
                order.getShippingAddress().line2(),
                order.getShippingAddress().city(),
                order.getShippingAddress().postalCode(),
                order.getShippingAddress().country(),
                order.getShippingAddress().phone());
        return new OrderResponse(
                order.getId(), order.getCustomerId(), order.getStatus().name(),
                order.getTotalCents(), order.getCurrency(), addr, order.getNotes(),
                order.getPlacedAt(), order.getPaidAt(), order.getFulfilledAt(),
                order.getCancelledAt(), order.getCancelReason(), order.getVersion(),
                order.getCreatedAt(), order.getUpdatedAt(), lineResponses);
    }
}
