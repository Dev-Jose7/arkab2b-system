CREATE TABLE IF NOT EXISTS notification_requests (
    notification_id       VARCHAR(36) PRIMARY KEY,
    tenant_id             VARCHAR(64) NOT NULL,
    source_event_id       VARCHAR(120) NOT NULL,
    source_event_type     VARCHAR(120) NOT NULL,
    recipient_ref         VARCHAR(120) NOT NULL,
    channel               VARCHAR(32) NOT NULL,
    template_id           VARCHAR(36) NOT NULL,
    channel_policy_id     VARCHAR(36) NOT NULL,
    notification_key      VARCHAR(320) NOT NULL,
    payload_json          TEXT NOT NULL,
    status                VARCHAR(16) NOT NULL,
    retryable             BOOLEAN NOT NULL DEFAULT TRUE,
    next_retry_at         TIMESTAMPTZ,
    max_attempts          INTEGER NOT NULL,
    attempt_count         INTEGER NOT NULL DEFAULT 0,
    trace_id              VARCHAR(120),
    correlation_id        VARCHAR(120),
    version               BIGINT NOT NULL DEFAULT 0,
    created_at            TIMESTAMPTZ NOT NULL,
    updated_at            TIMESTAMPTZ NOT NULL,
    CONSTRAINT ck_notification_requests_channel
        CHECK (channel IN ('EMAIL', 'SMS', 'WHATSAPP', 'IN_APP')),
    CONSTRAINT ck_notification_requests_status
        CHECK (status IN ('PENDING', 'SENT', 'FAILED', 'DISCARDED')),
    CONSTRAINT ck_notification_requests_attempts
        CHECK (max_attempts > 0 AND attempt_count >= 0),
    CONSTRAINT ck_notification_requests_version
        CHECK (version >= 0),
    CONSTRAINT uq_notification_requests_tenant_notification UNIQUE (tenant_id, notification_id)
);

CREATE UNIQUE INDEX IF NOT EXISTS ux_requests_tenant_notification_key
    ON notification_requests (tenant_id, notification_key);
CREATE INDEX IF NOT EXISTS idx_notification_requests_dispatchable
    ON notification_requests (status, retryable, next_retry_at);
CREATE INDEX IF NOT EXISTS idx_notification_requests_tenant_status_updated
    ON notification_requests (tenant_id, status, updated_at DESC);
CREATE INDEX IF NOT EXISTS idx_notification_requests_tenant_event
    ON notification_requests (tenant_id, source_event_type, created_at DESC);

CREATE TABLE IF NOT EXISTS notification_templates (
    template_id            VARCHAR(36) PRIMARY KEY,
    tenant_id              VARCHAR(64) NOT NULL,
    source_event_type      VARCHAR(120) NOT NULL,
    channel                VARCHAR(32) NOT NULL,
    template_version       INTEGER NOT NULL DEFAULT 1,
    subject_template       TEXT NOT NULL,
    body_template          TEXT NOT NULL,
    active                 BOOLEAN NOT NULL DEFAULT TRUE,
    created_at             TIMESTAMPTZ NOT NULL,
    updated_at             TIMESTAMPTZ NOT NULL,
    CONSTRAINT ck_notification_templates_channel
        CHECK (channel IN ('EMAIL', 'SMS', 'WHATSAPP', 'IN_APP')),
    CONSTRAINT ck_notification_templates_version CHECK (template_version > 0)
);

CREATE UNIQUE INDEX IF NOT EXISTS ux_notification_templates_version
    ON notification_templates (tenant_id, source_event_type, channel, template_version);
CREATE INDEX IF NOT EXISTS idx_notification_templates_active
    ON notification_templates (tenant_id, source_event_type, channel, active);

CREATE TABLE IF NOT EXISTS channel_policies (
    policy_id               VARCHAR(36) PRIMARY KEY,
    tenant_id               VARCHAR(64) NOT NULL,
    source_event_type       VARCHAR(120) NOT NULL,
    primary_channel         VARCHAR(32) NOT NULL,
    fallback_channel        VARCHAR(32),
    max_attempts            INTEGER NOT NULL,
    retry_interval_seconds  INTEGER NOT NULL,
    active                  BOOLEAN NOT NULL DEFAULT TRUE,
    created_at              TIMESTAMPTZ NOT NULL,
    updated_at              TIMESTAMPTZ NOT NULL,
    CONSTRAINT ck_channel_policies_primary_channel
        CHECK (primary_channel IN ('EMAIL', 'SMS', 'WHATSAPP', 'IN_APP')),
    CONSTRAINT ck_channel_policies_fallback_channel
        CHECK (fallback_channel IS NULL OR fallback_channel IN ('EMAIL', 'SMS', 'WHATSAPP', 'IN_APP')),
    CONSTRAINT ck_channel_policies_limits
        CHECK (max_attempts > 0 AND retry_interval_seconds >= 0)
);

CREATE UNIQUE INDEX IF NOT EXISTS ux_channel_policies_tenant_source_event
    ON channel_policies (tenant_id, source_event_type);
CREATE INDEX IF NOT EXISTS idx_channel_policies_active
    ON channel_policies (tenant_id, active);

CREATE TABLE IF NOT EXISTS notification_attempts (
    attempt_id              VARCHAR(36) PRIMARY KEY,
    tenant_id               VARCHAR(64) NOT NULL,
    notification_id         VARCHAR(36) NOT NULL,
    attempt_number          INTEGER NOT NULL,
    result_status           VARCHAR(16) NOT NULL,
    provider_code           VARCHAR(64) NOT NULL,
    provider_ref            VARCHAR(160),
    error_code              VARCHAR(120),
    error_message           TEXT,
    retryable               BOOLEAN NOT NULL,
    latency_ms              BIGINT,
    request_snapshot        TEXT,
    response_snapshot       TEXT,
    created_at              TIMESTAMPTZ NOT NULL,
    CONSTRAINT ck_notification_attempts_number CHECK (attempt_number > 0),
    CONSTRAINT ck_notification_attempts_result
        CHECK (result_status IN ('CREATED', 'SENT', 'FAILED')),
    CONSTRAINT uq_notification_attempts_number UNIQUE (notification_id, attempt_number),
    CONSTRAINT fk_notification_attempts_request FOREIGN KEY (tenant_id, notification_id)
        REFERENCES notification_requests (tenant_id, notification_id)
);

CREATE INDEX IF NOT EXISTS idx_notification_attempts_tenant_notification
    ON notification_attempts (tenant_id, notification_id, attempt_number ASC);
CREATE INDEX IF NOT EXISTS idx_notification_attempts_provider_ref
    ON notification_attempts (provider_code, provider_ref);

CREATE TABLE IF NOT EXISTS provider_callbacks (
    callback_id             VARCHAR(36) PRIMARY KEY,
    tenant_id               VARCHAR(64) NOT NULL,
    notification_id         VARCHAR(36) NOT NULL,
    provider_code           VARCHAR(64) NOT NULL,
    provider_ref            VARCHAR(160) NOT NULL,
    callback_event_id       VARCHAR(120) NOT NULL,
    callback_status         VARCHAR(16) NOT NULL,
    payload                 TEXT,
    received_at             TIMESTAMPTZ NOT NULL,
    updated_at              TIMESTAMPTZ NOT NULL,
    CONSTRAINT ck_provider_callbacks_status
        CHECK (callback_status IN ('RECEIVED', 'VALIDATED', 'REJECTED')),
    CONSTRAINT uq_provider_callbacks_dedupe UNIQUE (provider_code, provider_ref, callback_event_id),
    CONSTRAINT fk_provider_callbacks_request FOREIGN KEY (tenant_id, notification_id)
        REFERENCES notification_requests (tenant_id, notification_id)
);

CREATE INDEX IF NOT EXISTS idx_provider_callbacks_notification
    ON provider_callbacks (tenant_id, notification_id, received_at ASC);

CREATE TABLE IF NOT EXISTS notification_audits (
    audit_id                VARCHAR(36) PRIMARY KEY,
    tenant_id               VARCHAR(64) NOT NULL,
    actor_id                VARCHAR(100) NOT NULL,
    action_type             VARCHAR(100) NOT NULL,
    target_type             VARCHAR(80) NOT NULL,
    target_id               VARCHAR(120) NOT NULL,
    outcome                 VARCHAR(32) NOT NULL,
    payload                 TEXT,
    idempotency_key         VARCHAR(128),
    payload_hash            VARCHAR(128),
    created_at              TIMESTAMPTZ NOT NULL
);

CREATE UNIQUE INDEX IF NOT EXISTS ux_notification_audits_idempotency
    ON notification_audits (tenant_id, action_type, idempotency_key)
    WHERE idempotency_key IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_notification_audits_target
    ON notification_audits (tenant_id, target_type, target_id, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_notification_audits_created
    ON notification_audits (tenant_id, created_at DESC);

CREATE TABLE IF NOT EXISTS outbox_events (
    event_id                VARCHAR(36) PRIMARY KEY,
    aggregate_type          VARCHAR(80) NOT NULL,
    aggregate_id            VARCHAR(120) NOT NULL,
    event_type              VARCHAR(120) NOT NULL,
    payload                 TEXT NOT NULL,
    status                  VARCHAR(16) NOT NULL DEFAULT 'PENDING',
    occurred_at             TIMESTAMPTZ NOT NULL,
    published_at            TIMESTAMPTZ,
    retry_count             INTEGER NOT NULL DEFAULT 0,
    last_error              TEXT,
    created_at              TIMESTAMPTZ NOT NULL,
    updated_at              TIMESTAMPTZ NOT NULL,
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
