-- Deliveries table for the delivery service.
CREATE TABLE deliveries (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    order_id        UUID NOT NULL,
    customer_id     UUID NOT NULL,
    courier_id      VARCHAR(64) NOT NULL,
    tracking_number VARCHAR(128) NOT NULL,
    status          VARCHAR(32) NOT NULL DEFAULT 'SCHEDULED',
    scheduled_at    TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    picked_up_at    TIMESTAMPTZ,
    delivered_at    TIMESTAMPTZ,
    failed_at       TIMESTAMPTZ,
    cancelled_at    TIMESTAMPTZ,
    failure_reason  VARCHAR(512),
    version         BIGINT NOT NULL DEFAULT 0,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT deliveries_unique_order UNIQUE (order_id)
);

CREATE INDEX idx_deliveries_customer ON deliveries (customer_id);
CREATE INDEX idx_deliveries_status   ON deliveries (status);
CREATE INDEX idx_deliveries_courier  ON deliveries (courier_id);
