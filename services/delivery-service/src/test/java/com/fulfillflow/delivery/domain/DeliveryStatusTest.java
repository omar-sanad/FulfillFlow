package com.fulfillflow.delivery.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class DeliveryStatusTest {

    @Test
    void scheduledCanTransitionToAllNonTerminal() {
        assertThat(DeliveryStatus.SCHEDULED.canTransitionTo(DeliveryStatus.IN_TRANSIT)).isTrue();
        assertThat(DeliveryStatus.SCHEDULED.canTransitionTo(DeliveryStatus.COMPLETED)).isTrue();
        assertThat(DeliveryStatus.SCHEDULED.canTransitionTo(DeliveryStatus.FAILED)).isTrue();
        assertThat(DeliveryStatus.SCHEDULED.canTransitionTo(DeliveryStatus.CANCELLED)).isTrue();
    }

    @Test
    void inTransitCanCompleteOrFail() {
        assertThat(DeliveryStatus.IN_TRANSIT.canTransitionTo(DeliveryStatus.COMPLETED)).isTrue();
        assertThat(DeliveryStatus.IN_TRANSIT.canTransitionTo(DeliveryStatus.FAILED)).isTrue();
        assertThat(DeliveryStatus.IN_TRANSIT.canTransitionTo(DeliveryStatus.IN_TRANSIT)).isFalse();
    }

    @Test
    void terminalStatesAllowNoTransitions() {
        for (DeliveryStatus terminal : new DeliveryStatus[]{DeliveryStatus.COMPLETED, DeliveryStatus.FAILED, DeliveryStatus.CANCELLED}) {
            for (DeliveryStatus target : DeliveryStatus.values()) {
                assertThat(terminal.canTransitionTo(target)).isFalse();
            }
        }
    }
}
