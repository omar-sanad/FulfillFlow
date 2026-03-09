package com.fulfillflow.order.event;

import com.fulfillflow.common.events.EventTypes;
import com.fulfillflow.common.events.payloads.DeliveryCompletedPayload;
import com.fulfillflow.common.events.payloads.DeliveryFailedPayload;
import com.fulfillflow.common.events.payloads.InventoryReservationFailedPayload;
import com.fulfillflow.common.events.payloads.OrderCancelledPayload;
import com.fulfillflow.common.events.payloads.OrderFulfilledPayload;
import com.fulfillflow.common.outbox.OutboxHelper;
import com.fulfillflow.order.domain.Order;
import com.fulfillflow.order.domain.OrderRepository;
import com.fulfillflow.order.domain.OrderStatus;
import com.fulfillflow.order.domain.OrderStatusHistory;
import com.fulfillflow.order.domain.OrderStatusHistoryRepository;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Saga coordinator (order side). Reacts to inventory and delivery events to
 * keep the order state machine consistent with downstream services:
 * <ul>
 *   <li>inventory.reservation.failed → auto-cancel the order (compensation).</li>
 *   <li>inventory.reserved → order stays CREATED awaiting payment (no transition).</li>
 *   <li>delivery.completed → auto-fulfil a paid order (PAID → FULFILLED).</li>
 *   <li>delivery.failed → mark a paid order as FAILED.</li>
 * </ul>
 */
@Service
public class OrderSagaService {

    private static final Logger log = LoggerFactory.getLogger(OrderSagaService.class);

    private final OrderRepository orderRepository;
    private final OrderStatusHistoryRepository historyRepository;
    private final OutboxHelper outboxHelper;

    public OrderSagaService(OrderRepository orderRepository,
                            OrderStatusHistoryRepository historyRepository,
                            OutboxHelper outboxHelper) {
        this.orderRepository = orderRepository;
        this.historyRepository = historyRepository;
        this.outboxHelper = outboxHelper;
    }

    @Transactional
    public void onReservationFailed(InventoryReservationFailedPayload payload) {
        UUID orderId = payload.orderId();
        Order order = orderRepository.findById(orderId).orElse(null);
        if (order == null) {
            log.warn("Reservation failed for unknown order {}", orderId);
            return;
        }
        if (order.getStatus() == OrderStatus.CANCELLED
                || order.getStatus() == OrderStatus.FAILED) {
            log.info("Order {} already {}; skipping reservation-failed handling",
                    orderId, order.getStatus());
            return;
        }
        OrderStatus from = order.getStatus();
        if (!from.canTransitionTo(OrderStatus.CANCELLED)) {
            log.warn("Cannot cancel order {} from state {} on reservation failure", orderId, from);
            return;
        }
        String reason = "Inventory reservation failed: " + payload.reason();
        order.transitionTo(OrderStatus.CANCELLED, reason);
        orderRepository.save(order);
        historyRepository.save(new OrderStatusHistory(
                orderId, from, OrderStatus.CANCELLED, reason, "saga"));
        log.info("Auto-cancelled order {} due to reservation failure", orderId);
    }

    public void onReserved(UUID orderId) {
        log.info("Stock reserved for order {}; awaiting payment", orderId);
    }

    public void onReleased(UUID orderId) {
        log.info("Stock released for order {}", orderId);
    }

    @Transactional
    public void onDeliveryCompleted(DeliveryCompletedPayload payload) {
        UUID orderId = payload.orderId();
        Order order = orderRepository.findById(orderId).orElse(null);
        if (order == null) {
            log.warn("Delivery completed for unknown order {}", orderId);
            return;
        }
        if (order.getStatus() == OrderStatus.FULFILLED) {
            log.info("Order {} already fulfilled; skipping delivery.completed", orderId);
            return;
        }
        OrderStatus from = order.getStatus();
        if (!from.canTransitionTo(OrderStatus.FULFILLED)) {
            log.warn("Cannot fulfil order {} from state {} on delivery completion", orderId, from);
            return;
        }
        order.transitionTo(OrderStatus.FULFILLED, "Delivery completed");
        orderRepository.save(order);
        historyRepository.save(new OrderStatusHistory(
                orderId, from, OrderStatus.FULFILLED, "Delivery completed", "saga"));
        outboxHelper.enqueue("Order", orderId.toString(), EventTypes.ORDER_FULFILLED,
                "orders.events.v1", orderId,
                new OrderFulfilledPayload(order.getId(), order.getCustomerId()));
        log.info("Auto-fulfilled order {} on delivery completion", orderId);
    }

    @Transactional
    public void onDeliveryFailed(DeliveryFailedPayload payload) {
        UUID orderId = payload.orderId();
        Order order = orderRepository.findById(orderId).orElse(null);
        if (order == null) {
            log.warn("Delivery failed for unknown order {}", orderId);
            return;
        }
        if (order.getStatus() == OrderStatus.FAILED
                || order.getStatus() == OrderStatus.CANCELLED) {
            log.info("Order {} already {}; skipping delivery.failed", orderId, order.getStatus());
            return;
        }
        OrderStatus from = order.getStatus();
        if (!from.canTransitionTo(OrderStatus.FAILED)) {
            log.warn("Cannot fail order {} from state {} on delivery failure", orderId, from);
            return;
        }
        String reason = "Delivery failed: " + payload.reason();
        order.transitionTo(OrderStatus.FAILED, reason);
        orderRepository.save(order);
        historyRepository.save(new OrderStatusHistory(
                orderId, from, OrderStatus.FAILED, reason, "saga"));
        log.info("Marked order {} as failed due to delivery failure", orderId);
    }
}
