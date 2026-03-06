-- Orders table
CREATE TABLE orders (
    id              UUID PRIMARY KEY,
    customer_id     UUID NOT NULL,
    status          VARCHAR(24) NOT NULL DEFAULT 'CREATED',
    total_cents     BIGINT NOT NULL DEFAULT 0 CHECK (total_cents >= 0),
    currency        VARCHAR(3) NOT NULL DEFAULT 'USD',
    shipping_address    JSONB NOT NULL,
    notes           TEXT,
    placed_at       TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    paid_at         TIMESTAMPTZ,
    fulfilled_at    TIMESTAMPTZ,
    cancelled_at    TIMESTAMPTZ,
    cancel_reason   TEXT,
    version         BIGINT NOT NULL DEFAULT 0,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_order_status CHECK (status IN ('CREATED','PAID','FULFILLED','CANCELLED','FAILED'))
);

CREATE INDEX idx_orders_customer ON orders (customer_id);
CREATE INDEX idx_orders_status ON orders (status);

-- Order lines (one row per product in an order)
CREATE TABLE order_lines (
    id              UUID PRIMARY KEY,
    order_id        UUID NOT NULL REFERENCES orders (id) ON DELETE CASCADE,
    product_id      UUID NOT NULL,
    sku             VARCHAR(64) NOT NULL,
    name            VARCHAR(256) NOT NULL,
    unit_price_cents    BIGINT NOT NULL CHECK (unit_price_cents >= 0),
    quantity        INTEGER NOT NULL CHECK (quantity > 0),
    line_total_cents   BIGINT NOT NULL CHECK (line_total_cents >= 0),
    currency        VARCHAR(3) NOT NULL DEFAULT 'USD',
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_order_line_id UNIQUE (id)
);

CREATE INDEX idx_order_lines_order ON order_lines (order_id);
CREATE INDEX idx_order_lines_product ON order_lines (product_id);

-- Order status history (audit trail of state transitions)
CREATE TABLE order_status_history (
    id              BIGSERIAL PRIMARY KEY,
    order_id        UUID NOT NULL REFERENCES orders (id) ON DELETE CASCADE,
    from_status     VARCHAR(24),
    to_status       VARCHAR(24) NOT NULL,
    reason          TEXT,
    actor           VARCHAR(128),
    occurred_at     TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_order_history_order ON order_status_history (order_id, occurred_at);
