package com.fulfillflow.inventory.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.UpdateTimestamp;

/**
 * Represents the stock level for a single product. Tracks available and
 * reserved quantities separately so that an order reservation reduces available
 * stock without yet deducting it permanently.
 */
@Entity
@Table(name = "stock_levels")
public class StockLevel {

    @Id
    private UUID productId;

    @OneToOne
    @MapsId
    private Product product;

    private Integer availableQuantity = 0;
    private Integer reservedQuantity = 0;

    @Version
    private Long version;

    @UpdateTimestamp
    private Instant updatedAt;

    protected StockLevel() {
    }

    public StockLevel(Product product, int availableQuantity) {
        this.product = product;
        this.productId = product.getId();
        this.availableQuantity = availableQuantity;
        this.reservedQuantity = 0;
    }

    /**
     * Reserves {@code quantity} units against this stock level. Throws if
     * insufficient stock is available.
     */
    public void reserve(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("quantity must be positive");
        }
        if (quantity > availableQuantity) {
            throw new InsufficientStockException(product.getId(), quantity, availableQuantity);
        }
        availableQuantity -= quantity;
        reservedQuantity += quantity;
    }

    /**
     * Confirms a reservation by permanently deducting the reserved quantity.
     */
    public void confirmReservation(int quantity) {
        if (quantity <= 0 || quantity > reservedQuantity) {
            throw new IllegalArgumentException("invalid confirm quantity");
        }
        reservedQuantity -= quantity;
    }

    /**
     * Releases a reservation back to available stock (compensation).
     */
    public void releaseReservation(int quantity) {
        if (quantity <= 0 || quantity > reservedQuantity) {
            throw new IllegalArgumentException("invalid release quantity");
        }
        reservedQuantity -= quantity;
        availableQuantity += quantity;
    }

    public void restock(int quantity) {
        if (quantity < 0) {
            throw new IllegalArgumentException("restock quantity must not be negative");
        }
        availableQuantity += quantity;
    }

    public UUID getProductId() { return productId; }
    public Product getProduct() { return product; }
    public Integer getAvailableQuantity() { return availableQuantity; }
    public Integer getReservedQuantity() { return reservedQuantity; }
    public Long getVersion() { return version; }
    public Instant getUpdatedAt() { return updatedAt; }
}
