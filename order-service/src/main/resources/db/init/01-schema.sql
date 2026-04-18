CREATE TABLE IF NOT EXISTS carts (
    cart_id                  VARCHAR(100) PRIMARY KEY,
    tenant_id                VARCHAR(100) NOT NULL,
    organization_id          VARCHAR(100) NOT NULL,
    user_id                  VARCHAR(100) NOT NULL,
    status                   VARCHAR(40)  NOT NULL,
    version                  BIGINT       NOT NULL DEFAULT 0,
    created_at               TIMESTAMP    NOT NULL,
    updated_at               TIMESTAMP    NOT NULL,
    CONSTRAINT chk_carts_status
        CHECK (status IN ('ACTIVE', 'CHECKOUT_IN_PROGRESS', 'CONVERTED', 'ABANDONED', 'CANCELLED')),
    CONSTRAINT chk_carts_version
        CHECK (version >= 0)
);

CREATE INDEX IF NOT EXISTS idx_carts_tenant_org_user
    ON carts (tenant_id, organization_id, user_id, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_carts_status
    ON carts (tenant_id, status, updated_at DESC);
CREATE UNIQUE INDEX IF NOT EXISTS ux_carts_active_tenant_org_user
    ON carts (tenant_id, organization_id, user_id)
    WHERE status IN ('ACTIVE', 'CHECKOUT_IN_PROGRESS');

CREATE TABLE IF NOT EXISTS cart_items (
    cart_item_id              VARCHAR(100) PRIMARY KEY,
    cart_id                   VARCHAR(100) NOT NULL,
    tenant_id                 VARCHAR(100) NOT NULL,
    organization_id           VARCHAR(100) NOT NULL,
    variant_id                VARCHAR(120) NOT NULL,
    sku                       VARCHAR(120) NOT NULL,
    qty                       INTEGER      NOT NULL,
    unit_price                NUMERIC(19,4) NOT NULL,
    currency                  VARCHAR(3)   NOT NULL,
    reservation_id            VARCHAR(120),
    reservation_confirmed     BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at                TIMESTAMP    NOT NULL,
    updated_at                TIMESTAMP    NOT NULL,
    CONSTRAINT fk_cart_items_cart
        FOREIGN KEY (cart_id) REFERENCES carts(cart_id)
        ON DELETE CASCADE,
    CONSTRAINT chk_cart_items_qty
        CHECK (qty > 0),
    CONSTRAINT chk_cart_items_price
        CHECK (unit_price >= 0),
    CONSTRAINT chk_cart_items_reservation_confirmation
        CHECK ((reservation_confirmed = FALSE) OR (reservation_id IS NOT NULL))
);

CREATE INDEX IF NOT EXISTS idx_cart_items_cart
    ON cart_items (cart_id, created_at ASC);
CREATE INDEX IF NOT EXISTS idx_cart_items_tenant_org
    ON cart_items (tenant_id, organization_id, sku);

CREATE TABLE IF NOT EXISTS checkout_attempts (
    checkout_attempt_id       VARCHAR(100) PRIMARY KEY,
    tenant_id                 VARCHAR(100) NOT NULL,
    organization_id           VARCHAR(100) NOT NULL,
    user_id                   VARCHAR(100) NOT NULL,
    cart_id                   VARCHAR(100) NOT NULL,
    checkout_correlation_id   VARCHAR(120) NOT NULL,
    validation_status         VARCHAR(20)  NOT NULL,
    address_id                VARCHAR(120) NOT NULL,
    country_code              VARCHAR(2)   NOT NULL,
    regional_policy_version   BIGINT       NOT NULL,
    policy_currency           VARCHAR(3),
    rejection_reasons         TEXT,
    created_at                TIMESTAMP    NOT NULL,
    updated_at                TIMESTAMP    NOT NULL,
    CONSTRAINT fk_checkout_attempt_cart
        FOREIGN KEY (cart_id) REFERENCES carts(cart_id)
        ON DELETE RESTRICT,
    CONSTRAINT chk_checkout_validation_status
        CHECK (validation_status IN ('VALID', 'INVALID')),
    CONSTRAINT chk_checkout_policy_version
        CHECK (regional_policy_version > 0)
);

CREATE UNIQUE INDEX IF NOT EXISTS ux_checkout_attempts_tenant_correlation
    ON checkout_attempts (tenant_id, checkout_correlation_id);
CREATE INDEX IF NOT EXISTS idx_checkout_attempts_cart
    ON checkout_attempts (cart_id, created_at DESC);

CREATE TABLE IF NOT EXISTS purchase_orders (
    order_id                  VARCHAR(100) PRIMARY KEY,
    order_number              VARCHAR(120) NOT NULL,
    tenant_id                 VARCHAR(100) NOT NULL,
    organization_id           VARCHAR(100) NOT NULL,
    user_id                   VARCHAR(100) NOT NULL,
    cart_id                   VARCHAR(100) NOT NULL,
    checkout_correlation_id   VARCHAR(120) NOT NULL,
    address_id                VARCHAR(120) NOT NULL,
    country_code              VARCHAR(2)   NOT NULL,
    regional_policy_version   BIGINT       NOT NULL,
    policy_currency           VARCHAR(3)   NOT NULL,
    status                    VARCHAR(40)  NOT NULL,
    payment_status            VARCHAR(40)  NOT NULL,
    subtotal                  NUMERIC(19,4) NOT NULL,
    total_amount              NUMERIC(19,4) NOT NULL,
    version                   BIGINT       NOT NULL DEFAULT 0,
    created_at                TIMESTAMP    NOT NULL,
    updated_at                TIMESTAMP    NOT NULL,
    CONSTRAINT fk_purchase_orders_cart
        FOREIGN KEY (cart_id) REFERENCES carts(cart_id)
        ON DELETE RESTRICT,
    CONSTRAINT chk_purchase_orders_status
        CHECK (status IN ('CREATED', 'PENDING_APPROVAL', 'CONFIRMED', 'CANCELLED', 'READY_TO_DISPATCH', 'DISPATCHED', 'DELIVERED')),
    CONSTRAINT chk_purchase_orders_payment_status
        CHECK (payment_status IN ('PENDING', 'PARTIALLY_PAID', 'PAID', 'OVERPAID_REVIEW')),
    CONSTRAINT chk_purchase_orders_regional_policy
        CHECK (regional_policy_version > 0),
    CONSTRAINT chk_purchase_orders_subtotal
        CHECK (subtotal >= 0),
    CONSTRAINT chk_purchase_orders_total_amount
        CHECK (total_amount >= 0),
    CONSTRAINT chk_purchase_orders_version
        CHECK (version >= 0)
);

CREATE UNIQUE INDEX IF NOT EXISTS ux_purchase_orders_tenant_order_number
    ON purchase_orders (tenant_id, order_number);
CREATE UNIQUE INDEX IF NOT EXISTS ux_purchase_orders_tenant_checkout_correlation
    ON purchase_orders (tenant_id, checkout_correlation_id);
CREATE UNIQUE INDEX IF NOT EXISTS ux_purchase_orders_cart
    ON purchase_orders (cart_id);
CREATE INDEX IF NOT EXISTS idx_purchase_orders_tenant_org_status_created
    ON purchase_orders (tenant_id, organization_id, status, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_purchase_orders_tenant_created
    ON purchase_orders (tenant_id, created_at DESC);

CREATE TABLE IF NOT EXISTS order_lines (
    order_line_id              VARCHAR(100) PRIMARY KEY,
    order_id                   VARCHAR(100) NOT NULL,
    tenant_id                  VARCHAR(100) NOT NULL,
    organization_id            VARCHAR(100) NOT NULL,
    variant_id                 VARCHAR(120) NOT NULL,
    sku                        VARCHAR(120) NOT NULL,
    qty                        INTEGER      NOT NULL,
    unit_price                 NUMERIC(19,4) NOT NULL,
    currency                   VARCHAR(3)   NOT NULL,
    reservation_id             VARCHAR(120) NOT NULL,
    reservation_confirmed      BOOLEAN      NOT NULL,
    line_total                 NUMERIC(19,4) NOT NULL,
    created_at                 TIMESTAMP    NOT NULL,
    updated_at                 TIMESTAMP    NOT NULL,
    CONSTRAINT fk_order_lines_order
        FOREIGN KEY (order_id) REFERENCES purchase_orders(order_id)
        ON DELETE CASCADE,
    CONSTRAINT chk_order_lines_qty
        CHECK (qty > 0),
    CONSTRAINT chk_order_lines_unit_price
        CHECK (unit_price >= 0),
    CONSTRAINT chk_order_lines_line_total
        CHECK (line_total >= 0),
    CONSTRAINT chk_order_lines_reservation_confirmed
        CHECK (reservation_confirmed = TRUE)
);

CREATE INDEX IF NOT EXISTS idx_order_lines_order
    ON order_lines (order_id, created_at ASC);
CREATE INDEX IF NOT EXISTS idx_order_lines_tenant_sku
    ON order_lines (tenant_id, sku);

CREATE TABLE IF NOT EXISTS payment_records (
    payment_record_id          VARCHAR(100) PRIMARY KEY,
    order_id                   VARCHAR(100) NOT NULL,
    tenant_id                  VARCHAR(100) NOT NULL,
    organization_id            VARCHAR(100) NOT NULL,
    payment_reference          VARCHAR(150) NOT NULL,
    amount                     NUMERIC(19,4) NOT NULL,
    method                     VARCHAR(60)  NOT NULL,
    support_reference          VARCHAR(200) NOT NULL,
    status                     VARCHAR(30)  NOT NULL,
    received_at                TIMESTAMP    NOT NULL,
    created_at                 TIMESTAMP    NOT NULL,
    updated_at                 TIMESTAMP    NOT NULL,
    CONSTRAINT fk_payment_records_order
        FOREIGN KEY (order_id) REFERENCES purchase_orders(order_id)
        ON DELETE CASCADE,
    CONSTRAINT chk_payment_records_status
        CHECK (status IN ('REGISTERED', 'VALIDATED', 'REJECTED')),
    CONSTRAINT chk_payment_records_amount
        CHECK (amount > 0)
);

CREATE UNIQUE INDEX IF NOT EXISTS ux_payment_records_order_reference
    ON payment_records (order_id, UPPER(payment_reference));
CREATE INDEX IF NOT EXISTS idx_payment_records_order_created
    ON payment_records (order_id, created_at ASC);

CREATE TABLE IF NOT EXISTS order_status_histories (
    status_history_id          VARCHAR(100) PRIMARY KEY,
    order_id                   VARCHAR(100) NOT NULL,
    tenant_id                  VARCHAR(100) NOT NULL,
    actor_user_id              VARCHAR(100) NOT NULL,
    from_status                VARCHAR(40),
    to_status                  VARCHAR(40)  NOT NULL,
    reason                     VARCHAR(255),
    occurred_at                TIMESTAMP    NOT NULL,
    CONSTRAINT fk_order_status_histories_order
        FOREIGN KEY (order_id) REFERENCES purchase_orders(order_id)
        ON DELETE CASCADE,
    CONSTRAINT chk_order_status_histories_to_status
        CHECK (to_status IN ('CREATED', 'PENDING_APPROVAL', 'CONFIRMED', 'CANCELLED', 'READY_TO_DISPATCH', 'DISPATCHED', 'DELIVERED'))
);

CREATE INDEX IF NOT EXISTS idx_order_status_histories_order_occurred
    ON order_status_histories (order_id, occurred_at ASC);

CREATE TABLE IF NOT EXISTS order_audits (
    audit_id                   VARCHAR(100) PRIMARY KEY,
    tenant_id                  VARCHAR(100) NOT NULL,
    organization_id            VARCHAR(100) NOT NULL,
    actor_user_id              VARCHAR(100) NOT NULL,
    action_type                VARCHAR(120) NOT NULL,
    target_type                VARCHAR(80)  NOT NULL,
    target_id                  VARCHAR(100) NOT NULL,
    outcome                    VARCHAR(30)  NOT NULL,
    payload                    TEXT,
    created_at                 TIMESTAMP    NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_order_audits_order_created
    ON order_audits (tenant_id, organization_id, target_id, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_order_audits_actor_created
    ON order_audits (actor_user_id, created_at DESC);

CREATE TABLE IF NOT EXISTS idempotency_records (
    idempotency_id             VARCHAR(100) PRIMARY KEY,
    tenant_id                  VARCHAR(100) NOT NULL,
    operation_name             VARCHAR(120) NOT NULL,
    idempotency_key            VARCHAR(120) NOT NULL,
    request_hash               VARCHAR(128) NOT NULL,
    resource_type              VARCHAR(80),
    resource_id                VARCHAR(100),
    response_status            INTEGER      NOT NULL,
    created_at                 TIMESTAMP    NOT NULL,
    updated_at                 TIMESTAMP    NOT NULL,
    CONSTRAINT uk_idempotency_tenant_operation_key
        UNIQUE (tenant_id, operation_name, idempotency_key)
);

CREATE INDEX IF NOT EXISTS idx_idempotency_tenant_created
    ON idempotency_records (tenant_id, created_at DESC);

CREATE TABLE IF NOT EXISTS outbox_events (
    event_id                   VARCHAR(100) PRIMARY KEY,
    aggregate_type             VARCHAR(100) NOT NULL,
    aggregate_id               VARCHAR(100) NOT NULL,
    event_type                 VARCHAR(150) NOT NULL,
    payload                    TEXT         NOT NULL,
    status                     VARCHAR(30)  NOT NULL,
    occurred_at                TIMESTAMP    NOT NULL,
    published_at               TIMESTAMP,
    retry_count                INTEGER      NOT NULL DEFAULT 0,
    last_error                 TEXT,
    created_at                 TIMESTAMP    NOT NULL,
    updated_at                 TIMESTAMP    NOT NULL,
    CONSTRAINT chk_outbox_events_status
        CHECK (status IN ('PENDING', 'PUBLISHED', 'FAILED'))
);

CREATE INDEX IF NOT EXISTS idx_outbox_events_status_occurred
    ON outbox_events (status, occurred_at);
CREATE INDEX IF NOT EXISTS idx_outbox_events_aggregate
    ON outbox_events (aggregate_type, aggregate_id);

CREATE TABLE IF NOT EXISTS processed_events (
    processed_event_id         VARCHAR(100) PRIMARY KEY,
    event_id                   VARCHAR(100) NOT NULL,
    consumer_name              VARCHAR(150) NOT NULL,
    processed_at               TIMESTAMP    NOT NULL,
    CONSTRAINT uk_processed_event_consumer
        UNIQUE (event_id, consumer_name)
);
