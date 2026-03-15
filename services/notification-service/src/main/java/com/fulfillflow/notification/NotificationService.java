package com.fulfillflow.notification;

import com.fulfillflow.common.events.EventTypes;
import com.fulfillflow.common.events.payloads.NotificationSentPayload;
import com.fulfillflow.common.outbox.OutboxHelper;
import com.fulfillflow.notification.domain.Notification;
import com.fulfillflow.notification.domain.NotificationChannel;
import com.fulfillflow.notification.domain.NotificationRepository;
import com.fulfillflow.notification.domain.NotificationStatus;
import com.fulfillflow.notification.model.NotificationResponse;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Application service for notifications. Renders a notification from a template
 * + variables, persists it, simulates dispatch to the configured channel, and
 * emits {@code notification.sent} via the transactional outbox. The simulated
 * provider is deterministic: EMAIL always succeeds; SMS succeeds unless the
 * recipient starts with {@code +999} (a synthetic failure probe).
 */
@Service
@Transactional
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);
    private static final String CHANNEL_EMAIL = "EMAIL";

    private final NotificationRepository notificationRepository;
    private final OutboxHelper outboxHelper;

    public NotificationService(NotificationRepository notificationRepository, OutboxHelper outboxHelper) {
        this.notificationRepository = notificationRepository;
        this.outboxHelper = outboxHelper;
    }

    public Notification sendForOrder(UUID orderId, UUID customerId, String channel,
                                     String template, String recipient,
                                     Map<String, String> variables) {
        NotificationChannel ch = NotificationChannel.valueOf(channel.toUpperCase());
        String subject = renderSubject(template, variables);
        String body = renderBody(template, variables);
        Notification notification = new Notification(
                UUID.randomUUID(), orderId, customerId, ch, template, recipient, subject, body);
        notification = notificationRepository.save(notification);

        boolean delivered = dispatch(ch, recipient, body);
        if (delivered) {
            notification.markSent();
        } else {
            notification.markFailed("Simulated provider failure");
        }
        notification = notificationRepository.save(notification);
        emitSent(notification);
        log.info("Notification {} for order {} via {} -> {}",
                notification.getId(), orderId, ch, notification.getStatus());
        return notification;
    }

    @Transactional(readOnly = true)
    public NotificationResponse getNotification(UUID id) {
        return toResponse(load(id));
    }

    @Transactional(readOnly = true)
    public List<NotificationResponse> listForCustomer(UUID customerId) {
        return notificationRepository.findAll().stream()
                .filter(n -> n.getCustomerId().equals(customerId))
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<NotificationResponse> listAll() {
        return notificationRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<NotificationResponse> listForOrder(UUID orderId) {
        return notificationRepository.findAll().stream()
                .filter(n -> orderId.equals(n.getOrderId()))
                .map(this::toResponse)
                .toList();
    }

    private boolean dispatch(NotificationChannel channel, String recipient, String body) {
        if (channel == NotificationChannel.SMS && recipient != null && recipient.startsWith("+999")) {
            return false;
        }
        return CHANNEL_EMAIL.equals(channel.name()) || channel == NotificationChannel.SMS
                || channel == NotificationChannel.PUSH;
    }

    String renderSubject(String template, Map<String, String> variables) {
        return switch (template) {
            case "order.created" -> "Order confirmed — #" + variables.getOrDefault("orderId", "—");
            case "order.paid" -> "Payment received — #" + variables.getOrDefault("orderId", "—");
            case "order.fulfilled" -> "Your order is fulfilled — #" + variables.getOrDefault("orderId", "—");
            case "order.cancelled" -> "Order cancelled — #" + variables.getOrDefault("orderId", "—");
            case "delivery.scheduled" -> "Your delivery is on the way — " + variables.getOrDefault("trackingNumber", "—");
            case "delivery.completed" -> "Delivery completed — #" + variables.getOrDefault("orderId", "—");
            case "delivery.failed" -> "Delivery issue — #" + variables.getOrDefault("orderId", "—");
            default -> "FulfillFlow notification";
        };
    }

    String renderBody(String template, Map<String, String> variables) {
        String orderId = variables.getOrDefault("orderId", "—");
        return switch (template) {
            case "order.created" -> "Thanks for your order " + orderId + ". We're reserving your items now.";
            case "order.paid" -> "We received your payment for order " + orderId + ". Preparing for dispatch.";
            case "order.fulfilled" -> "Great news — order " + orderId + " is fulfilled. Enjoy!";
            case "order.cancelled" -> "Order " + orderId + " was cancelled: "
                    + variables.getOrDefault("reason", "see your account") + ".";
            case "delivery.scheduled" -> "Your order " + orderId + " is dispatched with courier "
                    + variables.getOrDefault("courierId", "—") + ". Tracking: "
                    + variables.getOrDefault("trackingNumber", "—") + ".";
            case "delivery.completed" -> "Your order " + orderId + " has been delivered. Thank you for shopping with us!";
            case "delivery.failed" -> "We couldn't complete delivery for order " + orderId + ": "
                    + variables.getOrDefault("reason", "please contact support") + ".";
            default -> "FulfillFlow notification for order " + orderId + ".";
        };
    }

    private void emitSent(Notification n) {
        NotificationStatus status = n.getStatus();
        outboxHelper.enqueue("Notification", n.getId().toString(),
                EventTypes.NOTIFICATION_SENT, "notifications.events.v1", n.getOrderId(),
                new NotificationSentPayload(n.getId(), n.getOrderId(), n.getCustomerId(),
                        n.getChannel().name(), n.getTemplate(), status.name(),
                        n.getSentAt() != null ? n.getSentAt() : Instant.now()));
    }

    private Notification load(UUID id) {
        return notificationRepository.findById(id).orElseThrow(() ->
                new com.fulfillflow.common.error.NotFoundException(
                        "NOTIFICATION_NOT_FOUND", "Notification " + id + " not found"));
    }

    private NotificationResponse toResponse(Notification n) {
        return new NotificationResponse(n.getId(), n.getOrderId(), n.getCustomerId(),
                n.getChannel().name(), n.getTemplate(), n.getRecipient(), n.getSubject(),
                n.getStatus().name(), n.getSentAt(), n.getFailureReason(),
                n.getVersion() == null ? 0 : n.getVersion(), n.getCreatedAt(), n.getUpdatedAt());
    }
}
