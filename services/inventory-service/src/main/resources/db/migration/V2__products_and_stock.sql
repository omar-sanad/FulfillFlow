-- Products catalogue table
CREATE TABLE products (
    id              UUID PRIMARY KEY,
    sku             VARCHAR(64) NOT NULL UNIQUE,
    name            VARCHAR(256) NOT NULL,
    description     TEXT,
    price_cents     BIGINT NOT NULL CHECK (price_cents >= 0),
    currency        VARCHAR(3) NOT NULL DEFAULT 'USD',
    weight_grams    INTEGER CHECK (weight_grams IS NULL OR weight_grams >= 0),
    active          BOOLEAN NOT NULL DEFAULT TRUE,
    version         BIGINT NOT NULL DEFAULT 0,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_products_active ON products (active) WHERE active = TRUE;

-- Stock levels table (one row per product)
CREATE TABLE stock_levels (
    product_id          UUID PRIMARY KEY REFERENCES products (id) ON DELETE CASCADE,
    available_quantity  INTEGER NOT NULL DEFAULT 0 CHECK (available_quantity >= 0),
    reserved_quantity   INTEGER NOT NULL DEFAULT 0 CHECK (reserved_quantity >= 0),
    version             BIGINT NOT NULL DEFAULT 0,
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Stock reservations tracking reservations against orders
CREATE TABLE stock_reservations (
    id              UUID PRIMARY KEY,
    order_id        UUID NOT NULL,
    order_line_id   UUID NOT NULL,
    product_id      UUID NOT NULL REFERENCES products (id),
    quantity        INTEGER NOT NULL CHECK (quantity > 0),
    state           VARCHAR(24) NOT NULL DEFAULT 'PENDING',
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    released_at     TIMESTAMPTZ,
    version         BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT uq_order_line_reservation UNIQUE (order_line_id)
);

CREATE INDEX idx_reservations_order ON stock_reservations (order_id);
CREATE INDEX idx_reservations_state ON stock_reservations (state);
