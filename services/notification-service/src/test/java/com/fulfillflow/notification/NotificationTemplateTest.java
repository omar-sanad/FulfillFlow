package com.fulfillflow.notification;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import org.junit.jupiter.api.Test;

class NotificationTemplateTest {

    private final NotificationService service = new NotificationService(null, null);

    @Test
    void orderCreatedSubjectIncludesOrderId() {
        String subject = service.renderSubject("order.created", Map.of("orderId", "ABCD1234"));
        assertThat(subject).contains("ABCD1234").contains("confirmed");
    }

    @Test
    void deliveryScheduledSubjectIncludesTrackingNumber() {
        String subject = service.renderSubject("delivery.scheduled",
                Map.of("trackingNumber", "FF-ABC123"));
        assertThat(subject).contains("FF-ABC123");
    }

    @Test
    void orderCancelledBodyIncludesReason() {
        String body = service.renderBody("order.cancelled",
                Map.of("orderId", "ABCD1234", "reason", "Out of stock"));
        assertThat(body).contains("ABCD1234").contains("Out of stock");
    }

    @Test
    void unknownTemplateFallsBackToGeneric() {
        String subject = service.renderSubject("unknown.event", Map.of("orderId", "XYZ"));
        assertThat(subject).contains("FulfillFlow notification");
    }
}
