package com.fulfillflow.common.events;

/**
 * Canonical event type names published to Kafka. Kept as constants so that
 * producers and consumers reference the same strings, avoiding typos and
 * making topic routing grep-able.
 */
public final class EventTypes {

    public static final String ORDER_CREATED = "order.created";
    public static final String ORDER_PAID = "order.paid";
    public static final String ORDER_CANCELLED = "order.cancelled";
    public static final String ORDER_FULFILLED = "order.fulfilled";

    public static final String INVENTORY_RESERVED = "inventory.reserved";
    public static final String INVENTORY_RESERVATION_FAILED = "inventory.reservation.failed";
    public static final String INVENTORY_RELEASED = "inventory.released";

    public static final String DELIVERY_SCHEDULED = "delivery.scheduled";
    public static final String DELIVERY_COMPLETED = "delivery.completed";
    public static final String DELIVERY_FAILED = "delivery.failed";

    public static final String NOTIFICATION_REQUESTED = "notification.requested";
    public static final String NOTIFICATION_SENT = "notification.sent";

    private EventTypes() {
    }
}
