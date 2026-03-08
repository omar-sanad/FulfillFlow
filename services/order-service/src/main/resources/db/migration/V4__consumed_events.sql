-- Idempotent consumed-events table for order service.
CREATE TABLE consumed_events (
    event_id    UUID PRIMARY KEY,
    event_type  VARCHAR(64) NOT NULL,
    consumed_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
