package com.fulfillflow.inventory.domain;

import com.fulfillflow.common.error.ConflictException;
import java.util.UUID;

/**
 * Thrown when a reservation request exceeds available stock.
 */
public class InsufficientStockException extends ConflictException {

    private final UUID productId;
    private final int requested;
    private final int available;

    public InsufficientStockException(UUID productId, int requested, int available) {
        super("INSUFFICIENT_STOCK",
                "Insufficient stock for product " + productId
                        + ": requested " + requested + ", available " + available);
        this.productId = productId;
        this.requested = requested;
        this.available = available;
    }

    public UUID getProductId() { return productId; }
    public int getRequested() { return requested; }
    public int getAvailable() { return available; }
}
