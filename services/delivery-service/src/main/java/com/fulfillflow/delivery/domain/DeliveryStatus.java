package com.fulfillflow.delivery.domain;

/**
 * Delivery lifecycle states.
 * <pre>
 *  SCHEDULED --pick-up--> IN_TRANSIT --deliver--> COMPLETED
 *  SCHEDULED --fail-->    FAILED
 *  SCHEDULED --cancel--> CANCELLED
 *  IN_TRANSIT --fail-->   FAILED
 * </pre>
 */
public enum DeliveryStatus {
    SCHEDULED,
    IN_TRANSIT,
    COMPLETED,
    FAILED,
    CANCELLED;

    public boolean canTransitionTo(DeliveryStatus target) {
        return switch (this) {
            case SCHEDULED -> target == IN_TRANSIT || target == COMPLETED
                    || target == FAILED || target == CANCELLED;
            case IN_TRANSIT -> target == COMPLETED || target == FAILED;
            case COMPLETED, FAILED, CANCELLED -> false;
        };
    }

    public void requireTransitionTo(DeliveryStatus target) {
        if (!canTransitionTo(target)) {
            throw new IllegalStateException(
                    "Cannot transition delivery from " + this + " to " + target);
        }
    }
}
