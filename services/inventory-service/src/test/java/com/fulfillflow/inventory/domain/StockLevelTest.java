package com.fulfillflow.inventory.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.UUID;
import org.junit.jupiter.api.Test;

class StockLevelTest {

    private StockLevel level(int available) {
        Product product = new Product("SKU-1", "Widget", "desc", 1000L, "USD", 100);
        return new StockLevel(product, available);
    }

    @Test
    void reserveReducesAvailableAndIncreasesReserved() {
        StockLevel stock = level(100);
        stock.reserve(30);
        assertThat(stock.getAvailableQuantity()).isEqualTo(70);
        assertThat(stock.getReservedQuantity()).isEqualTo(30);
    }

    @Test
    void reserveThrowsWhenInsufficient() {
        StockLevel stock = level(10);
        assertThatThrownBy(() -> stock.reserve(20))
                .isInstanceOf(InsufficientStockException.class);
        assertThat(stock.getAvailableQuantity()).isEqualTo(10);
        assertThat(stock.getReservedQuantity()).isZero();
    }

    @Test
    void reserveRejectsZeroOrNegative() {
        StockLevel stock = level(100);
        assertThatThrownBy(() -> stock.reserve(0)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> stock.reserve(-1)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void confirmDeductsFromReserved() {
        StockLevel stock = level(100);
        stock.reserve(40);
        stock.confirmReservation(40);
        assertThat(stock.getReservedQuantity()).isZero();
        assertThat(stock.getAvailableQuantity()).isEqualTo(60);
    }

    @Test
    void releaseReturnsToAvailable() {
        StockLevel stock = level(100);
        stock.reserve(40);
        stock.releaseReservation(40);
        assertThat(stock.getAvailableQuantity()).isEqualTo(100);
        assertThat(stock.getReservedQuantity()).isZero();
    }

    @Test
    void restockIncreasesAvailable() {
        StockLevel stock = level(50);
        stock.restock(30);
        assertThat(stock.getAvailableQuantity()).isEqualTo(80);
    }

    @Test
    void fullReserveConfirmReleaseCycle() {
        StockLevel stock = level(100);
        stock.reserve(25);
        stock.confirmReservation(25);
        assertThat(stock.getAvailableQuantity()).isEqualTo(75);
        assertThat(stock.getReservedQuantity()).isZero();
    }
}
