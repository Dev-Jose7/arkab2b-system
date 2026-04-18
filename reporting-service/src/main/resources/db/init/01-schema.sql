CREATE TABLE IF NOT EXISTS analytic_facts (
    fact_id              VARCHAR(36) PRIMARY KEY,
    tenant_id            VARCHAR(64) NOT NULL,
    source_event_id      VARCHAR(120) NOT NULL,
    event_type           VARCHAR(120) NOT NULL,
    fact_type            VARCHAR(24) NOT NULL,
    raw_payload          TEXT NOT NULL,
    normalized_payload   TEXT,
    fact_status          VARCHAR(16) NOT NULL,
    rejection_reason     TEXT,
    period               VARCHAR(16) NOT NULL,
    occurred_at          TIMESTAMPTZ,
    created_at           TIMESTAMPTZ NOT NULL,
    updated_at           TIMESTAMPTZ NOT NULL,
    CONSTRAINT ck_analytic_facts_status
        CHECK (fact_status IN ('CAPTURED', 'NORMALIZED', 'APPLIED', 'REJECTED')),
    CONSTRAINT ck_analytic_facts_type
        CHECK (fact_type IN ('SALES', 'REPLENISHMENT', 'OPERATIONS', 'NOTIFICATION', 'GENERIC')),
    CONSTRAINT ux_facts_tenant_source_event UNIQUE (tenant_id, source_event_id)
);

CREATE INDEX IF NOT EXISTS idx_analytic_facts_tenant_period_status
    ON analytic_facts (tenant_id, period, fact_status);
CREATE INDEX IF NOT EXISTS idx_analytic_facts_tenant_event_occurred
    ON analytic_facts (tenant_id, event_type, occurred_at DESC);

CREATE TABLE IF NOT EXISTS sales_projections (
    projection_id        VARCHAR(36) PRIMARY KEY,
    tenant_id            VARCHAR(64) NOT NULL,
    period               VARCHAR(16) NOT NULL,
    total_sales          NUMERIC(18,2) NOT NULL DEFAULT 0,
    paid_amount          NUMERIC(18,2) NOT NULL DEFAULT 0,
    pending_amount       NUMERIC(18,2) NOT NULL DEFAULT 0,
    confirmed_orders     BIGINT NOT NULL DEFAULT 0,
    average_ticket       NUMERIC(18,2) NOT NULL DEFAULT 0,
    version              BIGINT NOT NULL DEFAULT 0,
    created_at           TIMESTAMPTZ NOT NULL,
    updated_at           TIMESTAMPTZ NOT NULL,
    CONSTRAINT ck_sales_projection_values
        CHECK (total_sales >= 0 AND paid_amount >= 0 AND pending_amount >= 0 AND confirmed_orders >= 0 AND version >= 0),
    CONSTRAINT ux_sales_tenant_period UNIQUE (tenant_id, period)
);

CREATE INDEX IF NOT EXISTS idx_sales_projection_period
    ON sales_projections (tenant_id, period DESC);

CREATE TABLE IF NOT EXISTS replenishment_projections (
    projection_id        VARCHAR(36) PRIMARY KEY,
    tenant_id            VARCHAR(64) NOT NULL,
    period               VARCHAR(16) NOT NULL,
    sku                  VARCHAR(100) NOT NULL,
    available_qty        NUMERIC(18,2) NOT NULL DEFAULT 0,
    reorder_point        NUMERIC(18,2) NOT NULL DEFAULT 0,
    coverage_days        NUMERIC(18,2) NOT NULL DEFAULT 0,
    risk_level           VARCHAR(16) NOT NULL,
    version              BIGINT NOT NULL DEFAULT 0,
    created_at           TIMESTAMPTZ NOT NULL,
    updated_at           TIMESTAMPTZ NOT NULL,
    CONSTRAINT ck_replenishment_projection_risk
        CHECK (risk_level IN ('LOW', 'MEDIUM', 'HIGH', 'CRITICAL')),
    CONSTRAINT ck_replenishment_projection_values
        CHECK (version >= 0),
    CONSTRAINT ux_supply_tenant_period_sku UNIQUE (tenant_id, period, sku)
);

CREATE INDEX IF NOT EXISTS idx_replenishment_projection_period_risk
    ON replenishment_projections (tenant_id, period, risk_level);

CREATE TABLE IF NOT EXISTS operations_kpi_projections (
    projection_id        VARCHAR(36) PRIMARY KEY,
    tenant_id            VARCHAR(64) NOT NULL,
    period               VARCHAR(16) NOT NULL,
    kpi_name             VARCHAR(100) NOT NULL,
    kpi_value            NUMERIC(18,2) NOT NULL DEFAULT 0,
    version              BIGINT NOT NULL DEFAULT 0,
    created_at           TIMESTAMPTZ NOT NULL,
    updated_at           TIMESTAMPTZ NOT NULL,
    CONSTRAINT ck_operations_kpi_projection_version CHECK (version >= 0),
    CONSTRAINT ux_kpi_tenant_period_name UNIQUE (tenant_id, period, kpi_name)
);

CREATE INDEX IF NOT EXISTS idx_operations_kpi_period
    ON operations_kpi_projections (tenant_id, period, kpi_name);

CREATE TABLE IF NOT EXISTS weekly_report_executions (
    execution_id             VARCHAR(36) PRIMARY KEY,
    tenant_id                VARCHAR(64) NOT NULL,
    week_id                  VARCHAR(16) NOT NULL,
    report_type              VARCHAR(32) NOT NULL,
    status                   VARCHAR(16) NOT NULL,
    error_code               VARCHAR(120),
    error_message            TEXT,
    completion_artifact_ref  TEXT,
    version                  BIGINT NOT NULL DEFAULT 0,
    created_at               TIMESTAMPTZ NOT NULL,
    started_at               TIMESTAMPTZ,
    completed_at             TIMESTAMPTZ,
    updated_at               TIMESTAMPTZ NOT NULL,
    CONSTRAINT ck_weekly_exec_status
        CHECK (status IN ('PENDING', 'RUNNING', 'COMPLETED', 'FAILED')),
    CONSTRAINT ck_weekly_exec_report_type
        CHECK (report_type IN ('SALES', 'REPLENISHMENT', 'FULL_REBUILD')),
    CONSTRAINT ck_weekly_exec_version CHECK (version >= 0),
    CONSTRAINT ck_weekly_exec_completed_requires_artifact
        CHECK (status <> 'COMPLETED' OR (completion_artifact_ref IS NOT NULL AND length(trim(completion_artifact_ref)) > 0)),
    CONSTRAINT ux_weekly_exec_tenant_week_type UNIQUE (tenant_id, week_id, report_type)
);

CREATE INDEX IF NOT EXISTS idx_weekly_exec_tenant_status
    ON weekly_report_executions (tenant_id, status, updated_at DESC);

CREATE TABLE IF NOT EXISTS report_artifacts (
    artifact_id            VARCHAR(36) PRIMARY KEY,
    execution_id           VARCHAR(36) NOT NULL,
    tenant_id              VARCHAR(64) NOT NULL,
    week_id                VARCHAR(16) NOT NULL,
    report_type            VARCHAR(32) NOT NULL,
    format                 VARCHAR(16) NOT NULL,
    location_ref           TEXT NOT NULL,
    content_hash           VARCHAR(128),
    size_bytes             BIGINT NOT NULL DEFAULT 0,
    created_at             TIMESTAMPTZ NOT NULL,
    updated_at             TIMESTAMPTZ NOT NULL,
    CONSTRAINT ck_report_artifacts_size CHECK (size_bytes >= 0),
    CONSTRAINT ux_artifacts_tenant_week_type_format UNIQUE (tenant_id, week_id, report_type, format),
    CONSTRAINT fk_report_artifacts_execution FOREIGN KEY (execution_id)
        REFERENCES weekly_report_executions (execution_id)
);

CREATE INDEX IF NOT EXISTS idx_report_artifacts_execution
    ON report_artifacts (tenant_id, execution_id, created_at DESC);

CREATE TABLE IF NOT EXISTS consumer_checkpoints (
    checkpoint_id          VARCHAR(36) PRIMARY KEY,
    tenant_id              VARCHAR(64) NOT NULL,
    consumer_name          VARCHAR(100) NOT NULL,
    topic                  VARCHAR(150) NOT NULL,
    partition              INTEGER NOT NULL,
    current_offset         BIGINT NOT NULL,
    latest_offset          BIGINT NOT NULL,
    lag                    BIGINT NOT NULL,
    updated_at             TIMESTAMPTZ NOT NULL,
    CONSTRAINT ck_consumer_checkpoint_offsets
        CHECK (partition >= 0 AND current_offset >= 0 AND latest_offset >= 0 AND lag >= 0),
    CONSTRAINT ux_consumer_checkpoint_scope UNIQUE (tenant_id, consumer_name, topic, partition)
);

CREATE INDEX IF NOT EXISTS idx_consumer_checkpoint_lag
    ON consumer_checkpoints (tenant_id, lag DESC, updated_at DESC);

CREATE TABLE IF NOT EXISTS reporting_audits (
    audit_id               VARCHAR(36) PRIMARY KEY,
    tenant_id              VARCHAR(64) NOT NULL,
    actor_id               VARCHAR(100) NOT NULL,
    action_type            VARCHAR(100) NOT NULL,
    target_type            VARCHAR(80) NOT NULL,
    target_id              VARCHAR(120) NOT NULL,
    outcome                VARCHAR(32) NOT NULL,
    payload                TEXT,
    idempotency_key        VARCHAR(128),
    payload_hash           VARCHAR(128),
    created_at             TIMESTAMPTZ NOT NULL
);

CREATE UNIQUE INDEX IF NOT EXISTS ux_reporting_audits_idempotency
    ON reporting_audits (tenant_id, action_type, idempotency_key)
    WHERE idempotency_key IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_reporting_audits_target
    ON reporting_audits (tenant_id, target_type, target_id, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_reporting_audits_created
    ON reporting_audits (tenant_id, created_at DESC);

CREATE TABLE IF NOT EXISTS outbox_events (
    event_id               VARCHAR(36) PRIMARY KEY,
    aggregate_type         VARCHAR(80) NOT NULL,
    aggregate_id           VARCHAR(120) NOT NULL,
    event_type             VARCHAR(120) NOT NULL,
    payload                TEXT NOT NULL,
    status                 VARCHAR(16) NOT NULL DEFAULT 'PENDING',
    occurred_at            TIMESTAMPTZ NOT NULL,
    published_at           TIMESTAMPTZ,
    retry_count            INTEGER NOT NULL DEFAULT 0,
    last_error             TEXT,
    created_at             TIMESTAMPTZ NOT NULL,
    updated_at             TIMESTAMPTZ NOT NULL,
    CONSTRAINT ck_outbox_events_status CHECK (status IN ('PENDING', 'PUBLISHED', 'FAILED'))
);

CREATE INDEX IF NOT EXISTS idx_outbox_status_occurred
    ON outbox_events (status, occurred_at);

CREATE TABLE IF NOT EXISTS processed_events (
    processed_event_id      VARCHAR(36) PRIMARY KEY,
    event_id                VARCHAR(120) NOT NULL,
    consumer_name           VARCHAR(100) NOT NULL,
    processed_at            TIMESTAMPTZ NOT NULL,
    CONSTRAINT ux_processed_events_consumer UNIQUE (event_id, consumer_name)
);

CREATE INDEX IF NOT EXISTS idx_processed_events_consumer_processed_at
    ON processed_events (consumer_name, processed_at DESC);
