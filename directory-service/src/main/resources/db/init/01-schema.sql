CREATE TABLE IF NOT EXISTS organization (
    organization_id        VARCHAR(100) PRIMARY KEY,
    legal_name             VARCHAR(255) NOT NULL,
    trade_name             VARCHAR(255),
    country_code           VARCHAR(2)   NOT NULL,
    currency_code          VARCHAR(3)   NOT NULL,
    timezone               VARCHAR(100) NOT NULL,
    segment_tier           VARCHAR(80),
    status                 VARCHAR(30)  NOT NULL,
    created_at             TIMESTAMP    NOT NULL,
    updated_at             TIMESTAMP    NOT NULL,
    CONSTRAINT chk_organization_status
        CHECK (status IN ('ONBOARDING', 'ACTIVE', 'SUSPENDED', 'INACTIVE'))
);

DROP INDEX IF EXISTS ux_organization_code;
ALTER TABLE organization DROP COLUMN IF EXISTS organization_code;

CREATE INDEX IF NOT EXISTS idx_organization_status
    ON organization (status);
CREATE INDEX IF NOT EXISTS idx_organization_country
    ON organization (country_code);

CREATE TABLE IF NOT EXISTS organization_legal_profile (
    legal_profile_id       VARCHAR(100) PRIMARY KEY,
    organization_id        VARCHAR(100) NOT NULL,
    tax_id_type            VARCHAR(30)  NOT NULL,
    tax_id                 VARCHAR(100) NOT NULL,
    fiscal_regime          VARCHAR(120),
    legal_representative   VARCHAR(255),
    country_code           VARCHAR(2)   NOT NULL,
    verification_status    VARCHAR(30)  NOT NULL,
    verified_at            TIMESTAMP,
    created_at             TIMESTAMP    NOT NULL,
    updated_at             TIMESTAMP    NOT NULL,
    CONSTRAINT fk_organization_legal_profile_org
        FOREIGN KEY (organization_id) REFERENCES organization(organization_id)
        ON DELETE CASCADE,
    CONSTRAINT uk_organization_legal_profile_org
        UNIQUE (organization_id),
    CONSTRAINT chk_organization_legal_profile_status
        CHECK (verification_status IN ('PENDING', 'VERIFIED', 'REJECTED'))
);

CREATE INDEX IF NOT EXISTS idx_organization_legal_profile_tax
    ON organization_legal_profile (UPPER(country_code), UPPER(tax_id));

CREATE TABLE IF NOT EXISTS organization_user_profile (
    user_profile_id        VARCHAR(100) PRIMARY KEY,
    organization_id        VARCHAR(100) NOT NULL,
    iam_user_id            VARCHAR(100) NOT NULL,
    display_name           VARCHAR(255),
    job_title              VARCHAR(120),
    department             VARCHAR(120),
    locale                 VARCHAR(50),
    timezone               VARCHAR(100),
    role_reference         VARCHAR(120),
    ownership_scope        VARCHAR(120),
    status                 VARCHAR(30)  NOT NULL,
    created_at             TIMESTAMP    NOT NULL,
    updated_at             TIMESTAMP    NOT NULL,
    CONSTRAINT fk_organization_user_profile_org
        FOREIGN KEY (organization_id) REFERENCES organization(organization_id)
        ON DELETE CASCADE,
    CONSTRAINT uk_organization_user_profile_identity
        UNIQUE (organization_id, iam_user_id),
    CONSTRAINT chk_organization_user_profile_status
        CHECK (status IN ('ACTIVE', 'INACTIVE'))
);

CREATE INDEX IF NOT EXISTS idx_organization_user_profile_org
    ON organization_user_profile (organization_id);
CREATE INDEX IF NOT EXISTS idx_organization_user_profile_iam
    ON organization_user_profile (iam_user_id);

CREATE TABLE IF NOT EXISTS organization_contact (
    contact_id             VARCHAR(100) PRIMARY KEY,
    organization_id        VARCHAR(100) NOT NULL,
    contact_type           VARCHAR(30)  NOT NULL,
    label                  VARCHAR(120),
    value_normalized       VARCHAR(255) NOT NULL,
    value_masked           VARCHAR(255),
    is_primary             BOOLEAN      NOT NULL DEFAULT FALSE,
    status                 VARCHAR(30)  NOT NULL,
    created_at             TIMESTAMP    NOT NULL,
    updated_at             TIMESTAMP    NOT NULL,
    CONSTRAINT fk_organization_contact_org
        FOREIGN KEY (organization_id) REFERENCES organization(organization_id)
        ON DELETE CASCADE,
    CONSTRAINT chk_organization_contact_status
        CHECK (status IN ('ACTIVE', 'INACTIVE'))
);

CREATE INDEX IF NOT EXISTS idx_organization_contact_org
    ON organization_contact (organization_id);

CREATE UNIQUE INDEX IF NOT EXISTS ux_org_contact_primary_active
    ON organization_contact (organization_id, contact_type)
    WHERE status = 'ACTIVE' AND is_primary = TRUE;

CREATE UNIQUE INDEX IF NOT EXISTS ux_org_contact_value_active
    ON organization_contact (organization_id, contact_type, value_normalized)
    WHERE status = 'ACTIVE';

CREATE TABLE IF NOT EXISTS address (
    address_id             VARCHAR(100) PRIMARY KEY,
    organization_id        VARCHAR(100) NOT NULL,
    address_type           VARCHAR(30)  NOT NULL,
    alias                  VARCHAR(120),
    line1                  VARCHAR(255) NOT NULL,
    line2                  VARCHAR(255),
    city                   VARCHAR(120) NOT NULL,
    state_region           VARCHAR(120),
    postal_code            VARCHAR(30),
    country_code           VARCHAR(2)   NOT NULL,
    reference              VARCHAR(255),
    latitude               DOUBLE PRECISION,
    longitude              DOUBLE PRECISION,
    is_default             BOOLEAN      NOT NULL DEFAULT FALSE,
    status                 VARCHAR(30)  NOT NULL,
    validation_status      VARCHAR(30)  NOT NULL,
    validated_at           TIMESTAMP,
    created_at             TIMESTAMP    NOT NULL,
    updated_at             TIMESTAMP    NOT NULL,
    CONSTRAINT fk_address_org
        FOREIGN KEY (organization_id) REFERENCES organization(organization_id)
        ON DELETE CASCADE,
    CONSTRAINT chk_address_status
        CHECK (status IN ('ACTIVE', 'INACTIVE', 'ARCHIVED')),
    CONSTRAINT chk_address_validation_status
        CHECK (validation_status IN ('PENDING', 'VERIFIED', 'REJECTED'))
);

CREATE INDEX IF NOT EXISTS idx_address_org
    ON address (organization_id);
CREATE INDEX IF NOT EXISTS idx_address_country
    ON address (country_code);

CREATE UNIQUE INDEX IF NOT EXISTS ux_address_default_by_type_active
    ON address (organization_id, address_type)
    WHERE status = 'ACTIVE' AND is_default = TRUE;

CREATE TABLE IF NOT EXISTS organization_country_policy (
    policy_id                  VARCHAR(100) PRIMARY KEY,
    organization_id            VARCHAR(100) NOT NULL,
    country_code               VARCHAR(2)   NOT NULL,
    policy_version             BIGINT       NOT NULL,
    currency_code              VARCHAR(3)   NOT NULL,
    week_starts_on             VARCHAR(20)  NOT NULL,
    weekly_cutoff_local_time   VARCHAR(10)  NOT NULL,
    timezone                   VARCHAR(100) NOT NULL,
    reporting_retention_days   INTEGER      NOT NULL,
    requires_verified_address  BOOLEAN      NOT NULL DEFAULT TRUE,
    effective_from             TIMESTAMP    NOT NULL,
    effective_to               TIMESTAMP,
    status                     VARCHAR(30)  NOT NULL,
    created_at                 TIMESTAMP    NOT NULL,
    updated_at                 TIMESTAMP    NOT NULL,
    CONSTRAINT fk_organization_country_policy_org
        FOREIGN KEY (organization_id) REFERENCES organization(organization_id)
        ON DELETE CASCADE,
    CONSTRAINT uk_organization_country_policy_version
        UNIQUE (organization_id, country_code, policy_version),
    CONSTRAINT chk_organization_country_policy_status
        CHECK (status IN ('ACTIVE', 'INACTIVE', 'SUPERSEDED')),
    CONSTRAINT chk_organization_country_policy_version_positive
        CHECK (policy_version > 0),
    CONSTRAINT chk_organization_country_policy_retention_positive
        CHECK (reporting_retention_days > 0)
);

CREATE UNIQUE INDEX IF NOT EXISTS ux_country_policy_active
    ON organization_country_policy (organization_id, country_code)
    WHERE status = 'ACTIVE';

CREATE INDEX IF NOT EXISTS idx_country_policy_resolution
    ON organization_country_policy (organization_id, country_code, status, effective_from DESC, policy_version DESC);

CREATE TABLE IF NOT EXISTS directory_audit (
    audit_id                VARCHAR(100) PRIMARY KEY,
    organization_id         VARCHAR(100) NOT NULL,
    actor_user_id           VARCHAR(100) NOT NULL,
    action_type             VARCHAR(100) NOT NULL,
    target_type             VARCHAR(100) NOT NULL,
    target_id               VARCHAR(100) NOT NULL,
    outcome                 VARCHAR(30)  NOT NULL,
    payload                 JSONB,
    created_at              TIMESTAMP    NOT NULL,
    CONSTRAINT fk_directory_audit_org
        FOREIGN KEY (organization_id) REFERENCES organization(organization_id)
        ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_directory_audit_org_created
    ON directory_audit (organization_id, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_directory_audit_actor
    ON directory_audit (actor_user_id);

CREATE TABLE IF NOT EXISTS outbox_event (
    event_id              VARCHAR(100) PRIMARY KEY,
    aggregate_type        VARCHAR(100) NOT NULL,
    aggregate_id          VARCHAR(100) NOT NULL,
    event_type            VARCHAR(150) NOT NULL,
    payload               JSONB        NOT NULL,
    status                VARCHAR(30)  NOT NULL,
    occurred_at           TIMESTAMP    NOT NULL,
    published_at          TIMESTAMP,
    retry_count           INTEGER      NOT NULL DEFAULT 0,
    last_error            TEXT,
    created_at            TIMESTAMP    NOT NULL,
    updated_at            TIMESTAMP    NOT NULL,
    CONSTRAINT chk_outbox_status
        CHECK (status IN ('PENDING', 'PUBLISHED', 'FAILED'))
);

CREATE INDEX IF NOT EXISTS idx_outbox_event_status_occurred_at
    ON outbox_event (status, occurred_at);
CREATE INDEX IF NOT EXISTS idx_outbox_event_aggregate
    ON outbox_event (aggregate_type, aggregate_id);

CREATE TABLE IF NOT EXISTS processed_event (
    processed_event_id    VARCHAR(100) PRIMARY KEY,
    event_id              VARCHAR(100) NOT NULL,
    consumer_name         VARCHAR(150) NOT NULL,
    processed_at          TIMESTAMP    NOT NULL,
    CONSTRAINT uk_processed_event_consumer
        UNIQUE (event_id, consumer_name)
);
