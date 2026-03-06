package com.fulfillflow.order.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "order_lines")
public class OrderLine {

    @Id
    private UUID id;

    private UUID orderId;
    private UUID productId;
    private String sku;
    private String name;

    @Column(name = "unit_price_cents")
    private Long unitPriceCents;
    private Integer quantity;

    @Column(name = "line_total_cents")
    private Long lineTotalCents;
    private String currency;

    @CreationTimestamp
    private Instant createdAt;

    protected OrderLine() {
    }

    public OrderLine(UUID id, UUID orderId, UUID productId, String sku, String name,
                    Long unitPriceCents, Integer quantity, String currency) {
        this.id = id;
        this.orderId = orderId;
        this.productId = productId;
        this.sku = sku;
        this.name = name;
        this.unitPriceCents = unitPriceCents;
        this.quantity = quantity;
        this.currency = currency;
        this.lineTotalCents = unitPriceCents * quantity;
    }

    public UUID getId() { return id; }
    public UUID getOrderId() { return orderId; }
    public UUID getProductId() { return productId; }
    public String getSku() { return sku; }
    public String getName() { return name; }
    public Long getUnitPriceCents() { return unitPriceCents; }
    public Integer getQuantity() { return quantity; }
    public Long getLineTotalCents() { return lineTotalCents; }
    public String getCurrency() { return currency; }
    public Instant getCreatedAt() { return createdAt; }
}
