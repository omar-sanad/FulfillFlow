package com.fulfillflow.order.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.UUID;
import org.junit.jupiter.api.Test;

class OrderTest {

    private Order newOrder() {
        return new Order(UUID.randomUUID(), new ShippingAddress("Jane", "1 St", null,
                "Cairo", "11511", "EG", null), null, "USD");
    }

    @Test
    void newOrderStartsAsCreated() {
        Order order = newOrder();
        assertThat(order.getStatus()).isEqualTo(OrderStatus.CREATED);
        assertThat(order.getTotalCents()).isZero();
        assertThat(order.getPlacedAt()).isNotNull();
    }

    @Test
    void paySetsPaidAtAndStatus() {
        Order order = newOrder();
        order.transitionTo(OrderStatus.PAID, "Card payment");
        assertThat(order.getStatus()).isEqualTo(OrderStatus.PAID);
        assertThat(order.getPaidAt()).isNotNull();
    }

    @Test
    void cancelFromCreatedSetsReason() {
        Order order = newOrder();
        order.transitionTo(OrderStatus.CANCELLED, "Customer changed mind");
        assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELLED);
        assertThat(order.getCancelReason()).isEqualTo("Customer changed mind");
        assertThat(order.getCancelledAt()).isNotNull();
    }

    @Test
    void fulfillFromPaidSetsFulfilledAt() {
        Order order = newOrder();
        order.transitionTo(OrderStatus.PAID, "pay");
        order.transitionTo(OrderStatus.FULFILLED, "Delivery completed");
        assertThat(order.getStatus()).isEqualTo(OrderStatus.FULFILLED);
        assertThat(order.getFulfilledAt()).isNotNull();
    }

    @Test
    void cannotFulfillDirectlyFromCreated() {
        Order order = newOrder();
        assertThatThrownBy(() -> order.transitionTo(OrderStatus.FULFILLED, "fulfill"))
                .isInstanceOf(com.fulfillflow.common.error.ConflictException.class);
    }

    @Test
    void cannotTransitionFromTerminalState() {
        Order order = newOrder();
        order.transitionTo(OrderStatus.PAID, "pay");
        order.transitionTo(OrderStatus.FULFILLED, "done");
        assertThatThrownBy(() -> order.transitionTo(OrderStatus.CANCELLED, "cancel"))
                .isInstanceOf(com.fulfillflow.common.error.ConflictException.class);
    }

    @Test
    void recomputeTotalSumsLineTotals() {
        Order order = newOrder();
        order.getLines().add(new OrderLine(UUID.randomUUID(), order.getId(), UUID.randomUUID(),
                "SKU-1", "Widget", 2500L, 2, "USD"));
        order.getLines().add(new OrderLine(UUID.randomUUID(), order.getId(), UUID.randomUUID(),
                "SKU-2", "Cable", 1000L, 3, "USD"));
        order.recomputeTotal();
        assertThat(order.getTotalCents()).isEqualTo(8000L);
    }
}
