package com.fulfillflow.order.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fulfillflow.common.error.ConflictException;
import org.junit.jupiter.api.Test;

class OrderStatusTest {

    @Test
    void createdCanTransitionToPaidAndCancelled() {
        assertThat(OrderStatus.CREATED.canTransitionTo(OrderStatus.PAID)).isTrue();
        assertThat(OrderStatus.CREATED.canTransitionTo(OrderStatus.CANCELLED)).isTrue();
    }

    @Test
    void createdCannotTransitionToFulfilledOrFailed() {
        assertThat(OrderStatus.CREATED.canTransitionTo(OrderStatus.FULFILLED)).isFalse();
        assertThat(OrderStatus.CREATED.canTransitionTo(OrderStatus.FAILED)).isFalse();
    }

    @Test
    void paidCanTransitionToFulfilledCancelledAndFailed() {
        assertThat(OrderStatus.PAID.canTransitionTo(OrderStatus.FULFILLED)).isTrue();
        assertThat(OrderStatus.PAID.canTransitionTo(OrderStatus.CANCELLED)).isTrue();
        assertThat(OrderStatus.PAID.canTransitionTo(OrderStatus.FAILED)).isTrue();
    }

    @Test
    void terminalStatesAllowNoTransitions() {
        for (OrderStatus terminal : new OrderStatus[]{OrderStatus.FULFILLED, OrderStatus.CANCELLED, OrderStatus.FAILED}) {
            for (OrderStatus target : OrderStatus.values()) {
                assertThat(terminal.canTransitionTo(target)).isFalse();
            }
        }
    }

    @Test
    void requireTransitionToThrowsOnInvalidTransition() {
        assertThatThrownBy(() -> OrderStatus.CREATED.requireTransitionTo(OrderStatus.FULFILLED, "fulfill"))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("CREATED")
                .hasMessageContaining("FULFILLED");
    }
}
