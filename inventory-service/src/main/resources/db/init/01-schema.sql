CREATE TABLE IF NOT EXISTS warehouses (
    warehouse_id        VARCHAR(100) PRIMARY KEY,
    organization_id           VARCHAR(100) NOT NULL,
    warehouse_code      VARCHAR(60)  NOT NULL,
    warehouse_name      VARCHAR(255) NOT NULL,
    country_code        VARCHAR(2)   NOT NULL,
    status              VARCHAR(30)  NOT NULL,
    created_at          TIMESTAMP    NOT NULL,
    updated_at          TIMESTAMP    NOT NULL,
    CONSTRAINT chk_warehouse_status
        CHECK (status IN ('ACTIVE', 'INACTIVE'))
);

CREATE UNIQUE INDEX IF NOT EXISTS ux_warehouses_organization_code
    ON warehouses (organization_id, UPPER(warehouse_code));
CREATE INDEX IF NOT EXISTS idx_warehouses_organization
    ON warehouses (organization_id);

CREATE TABLE IF NOT EXISTS stock_items (
    stock_item_id       VARCHAR(100) PRIMARY KEY,
    organization_id           VARCHAR(100) NOT NULL,
    warehouse_id        VARCHAR(100) NOT NULL,
    sku                 VARCHAR(120) NOT NULL,
    physical_qty        INTEGER      NOT NULL,
    reserved_qty        INTEGER      NOT NULL DEFAULT 0,
    reorder_point       INTEGER      NOT NULL DEFAULT 0,
    safety_stock        INTEGER      NOT NULL DEFAULT 0,
    status              VARCHAR(30)  NOT NULL,
    version             BIGINT       NOT NULL DEFAULT 0,
    created_at          TIMESTAMP    NOT NULL,
    updated_at          TIMESTAMP    NOT NULL,
    CONSTRAINT fk_stock_items_warehouse
        FOREIGN KEY (warehouse_id) REFERENCES warehouses(warehouse_id)
        ON DELETE RESTRICT,
    CONSTRAINT chk_stock_item_qty_non_negative
        CHECK (physical_qty >= 0),
    CONSTRAINT chk_stock_item_reserved_non_negative
        CHECK (reserved_qty >= 0),
    CONSTRAINT chk_stock_item_reserved_le_physical
        CHECK (reserved_qty <= physical_qty),
    CONSTRAINT chk_stock_item_reorder_non_negative
        CHECK (reorder_point >= 0),
    CONSTRAINT chk_stock_item_safety_non_negative
        CHECK (safety_stock >= 0),
    CONSTRAINT chk_stock_item_status
        CHECK (status IN ('ACTIVE', 'BLOCKED', 'RECONCILING')),
    CONSTRAINT chk_stock_item_version_non_negative
        CHECK (version >= 0)
);

CREATE UNIQUE INDEX IF NOT EXISTS ux_stock_items_organization_warehouse_sku
    ON stock_items (organization_id, warehouse_id, UPPER(sku));
CREATE INDEX IF NOT EXISTS idx_stock_items_organization_warehouse
    ON stock_items (organization_id, warehouse_id);
CREATE INDEX IF NOT EXISTS idx_stock_items_low_stock
    ON stock_items (organization_id, warehouse_id, status, reorder_point, physical_qty, reserved_qty);

CREATE TABLE IF NOT EXISTS stock_reservations (
    reservation_id      VARCHAR(100) PRIMARY KEY,
    organization_id           VARCHAR(100) NOT NULL,
    stock_item_id       VARCHAR(100) NOT NULL,
    warehouse_id        VARCHAR(100) NOT NULL,
    sku                 VARCHAR(120) NOT NULL,
    cart_id             VARCHAR(100),
    order_id            VARCHAR(100),
    qty                 INTEGER      NOT NULL,
    status              VARCHAR(30)  NOT NULL,
    expires_at          TIMESTAMP,
    confirmed_at        TIMESTAMP,
    released_at         TIMESTAMP,
    created_at          TIMESTAMP    NOT NULL,
    updated_at          TIMESTAMP    NOT NULL,
    CONSTRAINT fk_stock_reservations_item
        FOREIGN KEY (stock_item_id) REFERENCES stock_items(stock_item_id)
        ON DELETE RESTRICT,
    CONSTRAINT chk_stock_reservation_qty_positive
        CHECK (qty > 0),
    CONSTRAINT chk_stock_reservation_status
        CHECK (status IN ('ACTIVE', 'CONFIRMED', 'RELEASED', 'EXPIRED')),
    CONSTRAINT chk_stock_reservation_active_expiration
        CHECK ((status <> 'ACTIVE') OR expires_at IS NOT NULL)
);

CREATE INDEX IF NOT EXISTS idx_stock_reservations_organization_cart
    ON stock_reservations (organization_id, cart_id, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_stock_reservations_organization_item
    ON stock_reservations (organization_id, stock_item_id, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_stock_reservations_active_expiration
    ON stock_reservations (organization_id, expires_at)
    WHERE status = 'ACTIVE';

CREATE TABLE IF NOT EXISTS stock_movements (
    movement_id         VARCHAR(100) PRIMARY KEY,
    organization_id           VARCHAR(100) NOT NULL,
    stock_item_id       VARCHAR(100) NOT NULL,
    warehouse_id        VARCHAR(100) NOT NULL,
    sku                 VARCHAR(120) NOT NULL,
    movement_type       VARCHAR(40)  NOT NULL,
    delta_qty           INTEGER      NOT NULL,
    reason              VARCHAR(255) NOT NULL,
    reservation_id      VARCHAR(100),
    order_id            VARCHAR(100),
    correlation_id      VARCHAR(120),
    created_at          TIMESTAMP    NOT NULL,
    CONSTRAINT fk_stock_movements_item
        FOREIGN KEY (stock_item_id) REFERENCES stock_items(stock_item_id)
        ON DELETE RESTRICT,
    CONSTRAINT chk_stock_movement_type
        CHECK (movement_type IN (
            'INITIALIZED',
            'OPERATIONAL_ADJUSTMENT',
            'RESERVATION_CREATED',
            'RESERVATION_CONFIRMED',
            'RESERVATION_RELEASED',
            'RESERVATION_EXPIRED',
            'RECONCILIATION'
        ))
);

CREATE INDEX IF NOT EXISTS idx_stock_movements_organization_item_created
    ON stock_movements (organization_id, stock_item_id, created_at DESC);

CREATE TABLE IF NOT EXISTS reservation_ledgers (
    ledger_id           VARCHAR(100) PRIMARY KEY,
    organization_id           VARCHAR(100) NOT NULL,
    reservation_id      VARCHAR(100) NOT NULL,
    entry_type          VARCHAR(40)  NOT NULL,
    qty                 INTEGER      NOT NULL,
    note                VARCHAR(255),
    created_at          TIMESTAMP    NOT NULL,
    CONSTRAINT fk_reservation_ledgers_reservation
        FOREIGN KEY (reservation_id) REFERENCES stock_reservations(reservation_id)
        ON DELETE RESTRICT,
    CONSTRAINT chk_reservation_ledger_qty_positive
        CHECK (qty > 0)
);

CREATE INDEX IF NOT EXISTS idx_reservation_ledgers_organization_reservation_created
    ON reservation_ledgers (organization_id, reservation_id, created_at DESC);

CREATE TABLE IF NOT EXISTS idempotency_records (
    idempotency_id      VARCHAR(100) PRIMARY KEY,
    organization_id           VARCHAR(100) NOT NULL,
    operation_name      VARCHAR(120) NOT NULL,
    idempotency_key     VARCHAR(120) NOT NULL,
    request_hash        VARCHAR(128) NOT NULL,
    resource_type       VARCHAR(80),
    resource_id         VARCHAR(100),
    response_status     INTEGER      NOT NULL,
    created_at          TIMESTAMP    NOT NULL,
    updated_at          TIMESTAMP    NOT NULL,
    CONSTRAINT uk_idempotency_organization_operation_key
        UNIQUE (organization_id, operation_name, idempotency_key)
);

CREATE INDEX IF NOT EXISTS idx_idempotency_created
    ON idempotency_records (organization_id, created_at DESC);

CREATE TABLE IF NOT EXISTS inventory_audits (
    audit_id            VARCHAR(100) PRIMARY KEY,
    organization_id           VARCHAR(100) NOT NULL,
    actor_user_id       VARCHAR(100) NOT NULL,
    action_type         VARCHAR(120) NOT NULL,
    target_type         VARCHAR(80)  NOT NULL,
    target_id           VARCHAR(100) NOT NULL,
    outcome             VARCHAR(30)  NOT NULL,
    payload             JSONB,
    created_at          TIMESTAMP    NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_inventory_audits_organization_created
    ON inventory_audits (organization_id, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_inventory_audits_actor
    ON inventory_audits (actor_user_id, created_at DESC);

CREATE TABLE IF NOT EXISTS outbox_events (
    event_id            VARCHAR(100) PRIMARY KEY,
    aggregate_type      VARCHAR(100) NOT NULL,
    aggregate_id        VARCHAR(100) NOT NULL,
    event_type          VARCHAR(150) NOT NULL,
    payload             JSONB        NOT NULL,
    status              VARCHAR(30)  NOT NULL,
    occurred_at         TIMESTAMP    NOT NULL,
    published_at        TIMESTAMP,
    retry_count         INTEGER      NOT NULL DEFAULT 0,
    last_error          TEXT,
    created_at          TIMESTAMP    NOT NULL,
    updated_at          TIMESTAMP    NOT NULL,
    CONSTRAINT chk_outbox_events_status
        CHECK (status IN ('PENDING', 'PUBLISHED', 'FAILED'))
);

CREATE INDEX IF NOT EXISTS idx_outbox_events_status_occurred
    ON outbox_events (status, occurred_at);
CREATE INDEX IF NOT EXISTS idx_outbox_events_aggregate
    ON outbox_events (aggregate_type, aggregate_id);

CREATE TABLE IF NOT EXISTS processed_events (
    processed_event_id  VARCHAR(100) PRIMARY KEY,
    event_id            VARCHAR(100) NOT NULL,
    consumer_name       VARCHAR(150) NOT NULL,
    processed_at        TIMESTAMP    NOT NULL,
    CONSTRAINT uk_processed_event_consumer
        UNIQUE (event_id, consumer_name)
);
