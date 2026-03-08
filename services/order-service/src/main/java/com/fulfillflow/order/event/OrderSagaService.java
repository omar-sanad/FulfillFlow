package com.fulfillflow.order.event;

import com.fulfillflow.common.events.EventTypes;
import com.fulfillflow.common.events.payloads.InventoryReservationFailedPayload;
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
 * Saga coordinator (order side). Reacts to inventory events to keep the order
 * state machine consistent with stock availability:
 * <ul>
 *   <li>inventory.reservation.failed → auto-cancel the order (compensation).</li>
 *   <li>inventory.reserved → order stays CREATED awaiting payment (no transition).</li>
 *   <li>inventory.released → no state change (order already cancelled or being compensated).</li>
 * </ul>
 */
@Service
public class OrderSagaService {

    private static final Logger log = LoggerFactory.getLogger(OrderSagaService.class);

    private final OrderRepository orderRepository;
    private final OrderStatusHistoryRepository historyRepository;

    public OrderSagaService(OrderRepository orderRepository,
                            OrderStatusHistoryRepository historyRepository) {
        this.orderRepository = orderRepository;
        this.historyRepository = historyRepository;
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
}
