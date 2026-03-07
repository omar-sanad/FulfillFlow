-- Transactional outbox table shared by the order service.
-- The OutboxMessage entity writes here; the OutboxPublisher polls NEW rows
-- and publishes them to Kafka.
CREATE TABLE outbox_messages (
    id              BIGSERIAL PRIMARY KEY,
    aggregate_type  VARCHAR(48) NOT NULL,
    aggregate_id    VARCHAR(64) NOT NULL,
    event_type      VARCHAR(64) NOT NULL,
    event_version   VARCHAR(8) NOT NULL DEFAULT 'v1',
    payload         JSONB NOT NULL,
    topic           VARCHAR(128) NOT NULL,
    correlation_id  UUID,
    status          VARCHAR(16) NOT NULL DEFAULT 'NEW',
    retry_count     INTEGER NOT NULL DEFAULT 0,
    last_error      TEXT,
    next_attempt_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    published_at    TIMESTAMPTZ,
    version         BIGINT NOT NULL DEFAULT 0,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_outbox_status CHECK (status IN ('NEW','IN_PROGRESS','SENT','FAILED'))
);

CREATE INDEX idx_outbox_due ON outbox_messages (status, next_attempt_at, created_at)
    WHERE status = 'NEW';
CREATE INDEX idx_outbox_aggregate ON outbox_messages (aggregate_type, aggregate_id);
