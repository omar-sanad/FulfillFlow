package com.fulfillflow.delivery.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.UUID;
import org.junit.jupiter.api.Test;

class DeliveryTest {

    private Delivery newDelivery() {
        return new Delivery(UUID.randomUUID(), UUID.randomUUID(), "COURIER-01", "FF-ABC123");
    }

    @Test
    void newDeliveryStartsAsScheduled() {
        Delivery delivery = newDelivery();
        assertThat(delivery.getStatus()).isEqualTo(DeliveryStatus.SCHEDULED);
        assertThat(delivery.getScheduledAt()).isNotNull();
    }

    @Test
    void markInTransitSetsPickedUpAt() {
        Delivery delivery = newDelivery();
        delivery.markInTransit();
        assertThat(delivery.getStatus()).isEqualTo(DeliveryStatus.IN_TRANSIT);
        assertThat(delivery.getPickedUpAt()).isNotNull();
    }

    @Test
    void markCompletedFromInTransitSetsDeliveredAt() {
        Delivery delivery = newDelivery();
        delivery.markInTransit();
        delivery.markCompleted();
        assertThat(delivery.getStatus()).isEqualTo(DeliveryStatus.COMPLETED);
        assertThat(delivery.getDeliveredAt()).isNotNull();
    }

    @Test
    void markCompletedFromScheduledIsAllowed() {
        Delivery delivery = newDelivery();
        delivery.markCompleted();
        assertThat(delivery.getStatus()).isEqualTo(DeliveryStatus.COMPLETED);
    }

    @Test
    void markFailedSetsReasonAndTimestamp() {
        Delivery delivery = newDelivery();
        delivery.markFailed("Courier unavailable");
        assertThat(delivery.getStatus()).isEqualTo(DeliveryStatus.FAILED);
        assertThat(delivery.getFailureReason()).isEqualTo("Courier unavailable");
        assertThat(delivery.getFailedAt()).isNotNull();
    }

    @Test
    void cancelSetsCancelledAt() {
        Delivery delivery = newDelivery();
        delivery.cancel();
        assertThat(delivery.getStatus()).isEqualTo(DeliveryStatus.CANCELLED);
        assertThat(delivery.getCancelledAt()).isNotNull();
    }

    @Test
    void cannotCancelCompletedDelivery() {
        Delivery delivery = newDelivery();
        delivery.markCompleted();
        assertThatThrownBy(delivery::cancel).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotCompleteFailedDelivery() {
        Delivery delivery = newDelivery();
        delivery.markFailed("broken");
        assertThatThrownBy(delivery::markCompleted).isInstanceOf(IllegalStateException.class);
    }
}
