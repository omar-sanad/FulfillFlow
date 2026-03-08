package com.fulfillflow.inventory.event;

import com.fulfillflow.common.events.EventTypes;
import com.fulfillflow.common.events.payloads.InventoryReleasedPayload;
import com.fulfillflow.common.events.payloads.InventoryReservationFailedPayload;
import com.fulfillflow.common.events.payloads.InventoryReservedPayload;
import com.fulfillflow.common.events.payloads.OrderCreatedPayload;
import com.fulfillflow.common.outbox.OutboxHelper;
import com.fulfillflow.inventory.domain.StockLevel;
import com.fulfillflow.inventory.domain.StockLevelRepository;
import com.fulfillflow.inventory.domain.StockReservation;
import com.fulfillflow.inventory.domain.StockReservationRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Saga participant: handles order lifecycle events by reserving, releasing,
 * or confirming stock. Each action emits a corresponding inventory event via
 * the transactional outbox so the order service can react.
 */
@Service
public class ReservationService {

    private static final Logger log = LoggerFactory.getLogger(ReservationService.class);

    private final StockLevelRepository stockLevelRepository;
    private final StockReservationRepository reservationRepository;
    private final OutboxHelper outboxHelper;

    public ReservationService(StockLevelRepository stockLevelRepository,
                              StockReservationRepository reservationRepository,
                              OutboxHelper outboxHelper) {
        this.stockLevelRepository = stockLevelRepository;
        this.reservationRepository = reservationRepository;
        this.outboxHelper = outboxHelper;
    }

    @Transactional
    public void reserveForOrder(OrderCreatedPayload order) {
        List<StockReservation> created = new ArrayList<>();
        List<InventoryReservedPayload.ReservedItem> reservedItems = new ArrayList<>();
        for (OrderCreatedPayload.OrderLineItem line : order.lines()) {
            StockLevel stock = stockLevelRepository.findById(line.productId())
                    .orElseThrow(() -> new IllegalStateException(
                            "Stock level not found for product " + line.productId()));
            stock.reserve(line.quantity());
            stockLevelRepository.save(stock);

            StockReservation reservation = new StockReservation(
                    order.orderId(), UUID.randomUUID(), line.productId(), line.quantity());
            created.add(reservationRepository.save(reservation));
            reservedItems.add(new InventoryReservedPayload.ReservedItem(
                    line.productId(), line.sku(), line.quantity()));
        }
        emitReserved(order, reservedItems);
        log.info("Reserved stock for order {} ({} lines)", order.orderId(), created.size());
    }

    /**
     * Emits an {@code inventory.reservation.failed} event in a fresh
     * transaction so it persists even when the calling reservation
     * transaction rolls back. This is the saga compensation trigger.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void emitReservationFailure(OrderCreatedPayload order, String reason) {
        outboxHelper.enqueue("Inventory", order.orderId().toString(),
                EventTypes.INVENTORY_RESERVATION_FAILED, "inventory.events.v1", order.orderId(),
                new InventoryReservationFailedPayload(order.orderId(), order.customerId(), reason));
        log.info("Emitted reservation-failed for order {}: {}", order.orderId(), reason);
    }

    @Transactional
    public void releaseForOrder(UUID orderId, String reason) {
        List<StockReservation> reservations = reservationRepository.findByOrderId(orderId);
        for (StockReservation reservation : reservations) {
            if (reservation.getState() == StockReservation.State.RELEASED) {
                continue;
            }
            StockLevel stock = stockLevelRepository.findById(reservation.getProductId())
                    .orElseThrow(() -> new IllegalStateException(
                            "Stock level not found for product " + reservation.getProductId()));
            stock.releaseReservation(reservation.getQuantity());
            stockLevelRepository.save(stock);
            reservation.release();
            reservationRepository.save(reservation);
        }
        outboxHelper.enqueue("Inventory", orderId.toString(), EventTypes.INVENTORY_RELEASED,
                "inventory.events.v1", orderId,
                new InventoryReleasedPayload(orderId, reason));
        log.info("Released {} reservation(s) for order {}", reservations.size(), orderId);
    }

    @Transactional
    public void confirmForOrder(UUID orderId) {
        List<StockReservation> reservations = reservationRepository.findByOrderId(orderId);
        for (StockReservation reservation : reservations) {
            if (reservation.getState() == StockReservation.State.CONFIRMED) {
                continue;
            }
            StockLevel stock = stockLevelRepository.findById(reservation.getProductId())
                    .orElseThrow(() -> new IllegalStateException(
                            "Stock level not found for product " + reservation.getProductId()));
            stock.confirmReservation(reservation.getQuantity());
            stockLevelRepository.save(stock);
            reservation.confirm();
            reservationRepository.save(reservation);
        }
        log.info("Confirmed {} reservation(s) for order {}", reservations.size(), orderId);
    }

    private void emitReserved(OrderCreatedPayload order,
                              List<InventoryReservedPayload.ReservedItem> items) {
        outboxHelper.enqueue("Inventory", order.orderId().toString(), EventTypes.INVENTORY_RESERVED,
                "inventory.events.v1", order.orderId(),
                new InventoryReservedPayload(order.orderId(), order.customerId(), items));
    }

    private void emitReservationFailed(OrderCreatedPayload order, String reason) {
        outboxHelper.enqueue("Inventory", order.orderId().toString(),
                EventTypes.INVENTORY_RESERVATION_FAILED, "inventory.events.v1", order.orderId(),
                new InventoryReservationFailedPayload(order.orderId(), order.customerId(), reason));
    }
}
