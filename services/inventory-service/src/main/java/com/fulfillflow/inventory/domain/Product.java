package com.fulfillflow.inventory.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.UuidGenerator;

@Entity
@Table(name = "products")
public class Product {

    @Id
    @UuidGenerator
    private UUID id;

    private String sku;
    private String name;
    private String description;

    /** Price in minor units (cents) to avoid floating point. */
    private Long priceCents;
    private String currency;
    private Integer weightGrams;
    private Boolean active = Boolean.TRUE;

    @Version
    private Long version;

    @CreationTimestamp
    private Instant createdAt;

    @UpdateTimestamp
    private Instant updatedAt;

    protected Product() {
    }

    public Product(String sku, String name, String description, Long priceCents,
                   String currency, Integer weightGrams) {
        this.sku = sku;
        this.name = name;
        this.description = description;
        this.priceCents = priceCents;
        this.currency = currency;
        this.weightGrams = weightGrams;
        this.active = Boolean.TRUE;
    }

    public UUID getId() { return id; }
    public String getSku() { return sku; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public Long getPriceCents() { return priceCents; }
    public String getCurrency() { return currency; }
    public Integer getWeightGrams() { return weightGrams; }
    public Boolean getActive() { return active; }
    public Long getVersion() { return version; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }

    void setActive(Boolean active) { this.active = active; }
    void setName(String name) { this.name = name; }
    void setDescription(String description) { this.description = description; }
    void setPriceCents(Long priceCents) { this.priceCents = priceCents; }
    void setCurrency(String currency) { this.currency = currency; }
    void setWeightGrams(Integer weightGrams) { this.weightGrams = weightGrams; }
}
