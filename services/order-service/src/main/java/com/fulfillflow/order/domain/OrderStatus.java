package com.fulfillflow.order.domain;

import com.fulfillflow.common.error.ConflictException;

/**
 * Order state machine. Permitted transitions:
 * <pre>
 *  CREATED --pay-->    PAID --fulfil--> FULFILLED
 *  CREATED --cancel--> CANCELLED
 *  PAID    --cancel--> CANCELLED
 *  PAID    --fail-->   FAILED
 * </pre>
 */
public enum OrderStatus {
    CREATED,
    PAID,
    FULFILLED,
    CANCELLED,
    FAILED;

    public void requireTransitionTo(OrderStatus target, String action) {
        if (!canTransitionTo(target)) {
            throw new ConflictException("INVALID_ORDER_TRANSITION",
                    "Cannot " + action + " order: " + this + " -> " + target + " is not permitted");
        }
    }

    public boolean canTransitionTo(OrderStatus target) {
        return switch (this) {
            case CREATED -> target == PAID || target == CANCELLED;
            case PAID -> target == FULFILLED || target == CANCELLED || target == FAILED;
            case FULFILLED, CANCELLED, FAILED -> false;
        };
    }
}
