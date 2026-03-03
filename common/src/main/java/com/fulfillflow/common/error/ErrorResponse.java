package com.fulfillflow.common.error;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Standard error body returned by every FulfillFlow service REST endpoint on
 * failure. The {@code traceId} lets an operator correlate an error response
 * with the originating request and downstream event flow.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(
        UUID traceId,
        Instant timestamp,
        int status,
        String error,
        String code,
        String message,
        String path,
        List<FieldError> fieldErrors
) {
    public static Builder builder() {
        return new Builder();
    }

    public record FieldError(String field, String rejectedValue, String message) {
    }

    public static final class Builder {
        private UUID traceId = UUID.randomUUID();
        private Instant timestamp = Instant.now();
        private int status;
        private String error;
        private String code;
        private String message;
        private String path;
        private List<FieldError> fieldErrors;

        public Builder status(int status) {
            this.status = status;
            return this;
        }

        public Builder error(String error) {
            this.error = error;
            return this;
        }

        public Builder code(String code) {
            this.code = code;
            return this;
        }

        public Builder message(String message) {
            this.message = message;
            return this;
        }

        public Builder path(String path) {
            this.path = path;
            return this;
        }

        public Builder timestamp(Instant timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public Builder fieldErrors(List<FieldError> fieldErrors) {
            this.fieldErrors = fieldErrors;
            return this;
        }

        public ErrorResponse build() {
            return new ErrorResponse(traceId, timestamp, status, error, code, message, path, fieldErrors);
        }
    }
}
