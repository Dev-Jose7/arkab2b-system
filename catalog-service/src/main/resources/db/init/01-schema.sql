CREATE EXTENSION IF NOT EXISTS btree_gist;

CREATE TABLE IF NOT EXISTS brands (
    brand_id         VARCHAR(36) PRIMARY KEY,
    organization_id        VARCHAR(64) NOT NULL,
    brand_code       VARCHAR(64) NOT NULL,
    brand_name       VARCHAR(200) NOT NULL,
    status           VARCHAR(16) NOT NULL,
    created_at       TIMESTAMPTZ NOT NULL,
    updated_at       TIMESTAMPTZ NOT NULL,
    CONSTRAINT ck_brands_status CHECK (status IN ('ACTIVE', 'INACTIVE')),
    CONSTRAINT uq_brands_organization_brand UNIQUE (organization_id, brand_id)
);

CREATE UNIQUE INDEX IF NOT EXISTS ux_brands_organization_code
    ON brands (organization_id, UPPER(brand_code));
CREATE INDEX IF NOT EXISTS idx_brands_organization_status
    ON brands (organization_id, status, updated_at DESC);

CREATE TABLE IF NOT EXISTS categories (
    category_id      VARCHAR(36) PRIMARY KEY,
    organization_id        VARCHAR(64) NOT NULL,
    category_code    VARCHAR(64) NOT NULL,
    category_name    VARCHAR(200) NOT NULL,
    status           VARCHAR(16) NOT NULL,
    created_at       TIMESTAMPTZ NOT NULL,
    updated_at       TIMESTAMPTZ NOT NULL,
    CONSTRAINT ck_categories_status CHECK (status IN ('ACTIVE', 'INACTIVE')),
    CONSTRAINT uq_categories_organization_category UNIQUE (organization_id, category_id)
);

CREATE UNIQUE INDEX IF NOT EXISTS ux_categories_organization_code
    ON categories (organization_id, UPPER(category_code));
CREATE INDEX IF NOT EXISTS idx_categories_organization_status
    ON categories (organization_id, status, updated_at DESC);

CREATE TABLE IF NOT EXISTS products (
    product_id       VARCHAR(36) PRIMARY KEY,
    organization_id        VARCHAR(64) NOT NULL,
    product_code     VARCHAR(64) NOT NULL,
    product_name     VARCHAR(200) NOT NULL,
    description      TEXT,
    brand_id         VARCHAR(36) NOT NULL,
    category_id      VARCHAR(36) NOT NULL,
    status           VARCHAR(16) NOT NULL,
    created_at       TIMESTAMPTZ NOT NULL,
    updated_at       TIMESTAMPTZ NOT NULL,
    CONSTRAINT ck_products_status CHECK (status IN ('DRAFT', 'ACTIVE', 'RETIRED')),
    CONSTRAINT uq_products_organization_product UNIQUE (organization_id, product_id),
    CONSTRAINT fk_products_brand FOREIGN KEY (organization_id, brand_id)
        REFERENCES brands (organization_id, brand_id),
    CONSTRAINT fk_products_category FOREIGN KEY (organization_id, category_id)
        REFERENCES categories (organization_id, category_id)
);

CREATE UNIQUE INDEX IF NOT EXISTS ux_products_organization_product_code
    ON products (organization_id, UPPER(product_code));
CREATE INDEX IF NOT EXISTS idx_products_organization_status_updated
    ON products (organization_id, status, updated_at DESC);
CREATE INDEX IF NOT EXISTS idx_products_search_name
    ON products (organization_id, UPPER(product_name));

CREATE TABLE IF NOT EXISTS product_tags (
    tag_id           VARCHAR(36) PRIMARY KEY,
    organization_id        VARCHAR(64) NOT NULL,
    product_id       VARCHAR(36) NOT NULL,
    tag_code         VARCHAR(100) NOT NULL,
    tag_value        VARCHAR(255) NOT NULL,
    created_at       TIMESTAMPTZ NOT NULL,
    updated_at       TIMESTAMPTZ NOT NULL,
    CONSTRAINT uq_product_tags_organization_product_tag UNIQUE (organization_id, product_id, tag_code, tag_value),
    CONSTRAINT fk_product_tags_product FOREIGN KEY (organization_id, product_id)
        REFERENCES products (organization_id, product_id)
);

CREATE INDEX IF NOT EXISTS idx_product_tags_organization_product
    ON product_tags (organization_id, product_id);

CREATE TABLE IF NOT EXISTS variants (
    variant_id       VARCHAR(36) PRIMARY KEY,
    organization_id        VARCHAR(64) NOT NULL,
    product_id       VARCHAR(36) NOT NULL,
    sku              VARCHAR(80) NOT NULL,
    variant_name     VARCHAR(200) NOT NULL,
    description      TEXT,
    status           VARCHAR(20) NOT NULL,
    sellable_from    TIMESTAMPTZ,
    sellable_until   TIMESTAMPTZ,
    weight_grams     INTEGER,
    created_at       TIMESTAMPTZ NOT NULL,
    updated_at       TIMESTAMPTZ NOT NULL,
    CONSTRAINT ck_variants_status CHECK (status IN ('DRAFT', 'SELLABLE', 'DISCONTINUED')),
    CONSTRAINT ck_variants_sellable_window CHECK (sellable_until IS NULL OR sellable_from IS NULL OR sellable_until > sellable_from),
    CONSTRAINT uq_variants_organization_variant UNIQUE (organization_id, variant_id),
    CONSTRAINT fk_variants_product FOREIGN KEY (organization_id, product_id)
        REFERENCES products (organization_id, product_id)
);

CREATE INDEX IF NOT EXISTS idx_variants_organization_product
    ON variants (organization_id, product_id, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_variants_organization_sku
    ON variants (organization_id, UPPER(sku));
CREATE UNIQUE INDEX IF NOT EXISTS ux_variants_organization_sku_sellable
    ON variants (organization_id, UPPER(sku))
    WHERE status = 'SELLABLE';

CREATE TABLE IF NOT EXISTS variant_attributes (
    attribute_id      VARCHAR(36) PRIMARY KEY,
    organization_id         VARCHAR(64) NOT NULL,
    variant_id        VARCHAR(36) NOT NULL,
    attribute_code    VARCHAR(100) NOT NULL,
    attribute_value   VARCHAR(255) NOT NULL,
    normalized_value  VARCHAR(255),
    created_at        TIMESTAMPTZ NOT NULL,
    updated_at        TIMESTAMPTZ NOT NULL,
    CONSTRAINT uq_variant_attributes_code UNIQUE (organization_id, variant_id, attribute_code),
    CONSTRAINT fk_variant_attributes_variant FOREIGN KEY (organization_id, variant_id)
        REFERENCES variants (organization_id, variant_id)
);

CREATE INDEX IF NOT EXISTS idx_variant_attributes_lookup
    ON variant_attributes (organization_id, attribute_code, normalized_value);
CREATE INDEX IF NOT EXISTS idx_variant_attributes_variant
    ON variant_attributes (organization_id, variant_id);

CREATE TABLE IF NOT EXISTS prices (
    price_id          VARCHAR(36) PRIMARY KEY,
    organization_id         VARCHAR(64) NOT NULL,
    variant_id        VARCHAR(36) NOT NULL,
    price_type        VARCHAR(32) NOT NULL,
    currency          VARCHAR(3) NOT NULL,
    amount            NUMERIC(19,4) NOT NULL,
    effective_from    TIMESTAMPTZ NOT NULL,
    effective_until   TIMESTAMPTZ,
    status            VARCHAR(16) NOT NULL,
    created_at        TIMESTAMPTZ NOT NULL,
    updated_at        TIMESTAMPTZ NOT NULL,
    CONSTRAINT ck_prices_status CHECK (status IN ('ACTIVE', 'SCHEDULED', 'EXPIRED')),
    CONSTRAINT ck_prices_amount_positive CHECK (amount > 0),
    CONSTRAINT ck_prices_window CHECK (effective_until IS NULL OR effective_until > effective_from),
    CONSTRAINT uq_prices_organization_price UNIQUE (organization_id, price_id),
    CONSTRAINT fk_prices_variant FOREIGN KEY (organization_id, variant_id)
        REFERENCES variants (organization_id, variant_id)
);

CREATE INDEX IF NOT EXISTS idx_prices_organization_variant_effective
    ON prices (organization_id, variant_id, effective_from DESC);
CREATE INDEX IF NOT EXISTS idx_prices_resolve_active
    ON prices (organization_id, variant_id, currency, price_type, effective_from DESC, effective_until);

ALTER TABLE prices
    DROP CONSTRAINT IF EXISTS ex_prices_no_overlap;
ALTER TABLE prices
    ADD CONSTRAINT ex_prices_no_overlap EXCLUDE USING gist (
        organization_id WITH =,
        variant_id WITH =,
        currency WITH =,
        price_type WITH =,
        tstzrange(effective_from, COALESCE(effective_until, 'infinity'::timestamptz), '[)') WITH &&
    );

CREATE TABLE IF NOT EXISTS price_schedules (
    schedule_id       VARCHAR(36) PRIMARY KEY,
    organization_id         VARCHAR(64) NOT NULL,
    price_id          VARCHAR(36) NOT NULL,
    execute_after     TIMESTAMPTZ NOT NULL,
    job_status        VARCHAR(16) NOT NULL,
    error_message     TEXT,
    created_at        TIMESTAMPTZ NOT NULL,
    updated_at        TIMESTAMPTZ NOT NULL,
    CONSTRAINT ck_price_schedules_status CHECK (job_status IN ('PENDING', 'EXECUTED', 'FAILED', 'CANCELLED')),
    CONSTRAINT fk_price_schedules_price FOREIGN KEY (organization_id, price_id)
        REFERENCES prices (organization_id, price_id)
);

CREATE INDEX IF NOT EXISTS idx_price_schedules_pending
    ON price_schedules (job_status, execute_after);

CREATE TABLE IF NOT EXISTS catalog_audits (
    audit_id           VARCHAR(36) PRIMARY KEY,
    organization_id          VARCHAR(64) NOT NULL,
    actor_id           VARCHAR(100) NOT NULL,
    action_type        VARCHAR(100) NOT NULL,
    target_type        VARCHAR(80) NOT NULL,
    target_id          VARCHAR(100) NOT NULL,
    outcome            VARCHAR(32) NOT NULL,
    payload            TEXT,
    idempotency_key    VARCHAR(128),
    payload_hash       VARCHAR(128),
    created_at         TIMESTAMPTZ NOT NULL
);

CREATE UNIQUE INDEX IF NOT EXISTS ux_catalog_audits_idempotency
    ON catalog_audits (organization_id, action_type, idempotency_key)
    WHERE idempotency_key IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_catalog_audits_target
    ON catalog_audits (organization_id, target_type, target_id, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_catalog_audits_created
    ON catalog_audits (organization_id, created_at DESC);

CREATE TABLE IF NOT EXISTS outbox_events (
    event_id           VARCHAR(36) PRIMARY KEY,
    aggregate_type     VARCHAR(80) NOT NULL,
    aggregate_id       VARCHAR(100) NOT NULL,
    event_type         VARCHAR(120) NOT NULL,
    payload            TEXT NOT NULL,
    status             VARCHAR(16) NOT NULL DEFAULT 'PENDING',
    occurred_at        TIMESTAMPTZ NOT NULL,
    published_at       TIMESTAMPTZ,
    retry_count        INTEGER NOT NULL DEFAULT 0,
    last_error         TEXT,
    created_at         TIMESTAMPTZ NOT NULL,
    updated_at         TIMESTAMPTZ NOT NULL,
    CONSTRAINT ck_outbox_status CHECK (status IN ('PENDING', 'PUBLISHED', 'FAILED'))
);

CREATE INDEX IF NOT EXISTS idx_outbox_pending_occurred
    ON outbox_events (status, occurred_at);

CREATE TABLE IF NOT EXISTS processed_events (
    processed_event_id  VARCHAR(36) PRIMARY KEY,
    event_id            VARCHAR(120) NOT NULL,
    consumer_name       VARCHAR(100) NOT NULL,
    processed_at        TIMESTAMPTZ NOT NULL,
    CONSTRAINT ux_processed_event_consumer UNIQUE (event_id, consumer_name)
);

CREATE INDEX IF NOT EXISTS idx_processed_events_consumer_time
    ON processed_events (consumer_name, processed_at DESC);
