package com.fulfillflow.common.events;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * Canonical envelope wrapping every inter-service event published to Kafka.
 * <p>
 * Carries standard routing and correlation metadata alongside the
 * service-specific payload. The {@code messageId} is generated and unique per
 * publication, enabling idempotent consumption. {@code occurredAt} is the
 * domain time the event happened (not necessarily the publication time).
 * <p>
 * All fields are non-null by contract; serialised JSON omits nothing so that
 * consumers can rely on the presence of every field.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record EventEnvelope(
        UUID messageId,
        String eventType,
        String eventVersion,
        Instant occurredAt,
        UUID correlationId,
        UUID causationId,
        Map<String, String> metadata,
        Object payload
) {
    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private UUID messageId = UUID.randomUUID();
        private String eventType;
        private String eventVersion = "v1";
        private Instant occurredAt = Instant.now();
        private UUID correlationId;
        private UUID causationId;
        private Map<String, String> metadata = Map.of();
        private Object payload;

        public Builder eventType(String eventType) {
            this.eventType = eventType;
            return this;
        }

        public Builder eventVersion(String eventVersion) {
            this.eventVersion = eventVersion;
            return this;
        }

        public Builder occurredAt(Instant occurredAt) {
            this.occurredAt = occurredAt;
            return this;
        }

        public Builder correlationId(UUID correlationId) {
            this.correlationId = correlationId;
            return this;
        }

        public Builder causationId(UUID causationId) {
            this.causationId = causationId;
            return this;
        }

        public Builder metadata(Map<String, String> metadata) {
            this.metadata = metadata == null ? Map.of() : metadata;
            return this;
        }

        public Builder payload(Object payload) {
            this.payload = payload;
            return this;
        }

        public EventEnvelope build() {
            return new EventEnvelope(
                    messageId, eventType, eventVersion, occurredAt,
                    correlationId, causationId, metadata, payload);
        }
    }
}
