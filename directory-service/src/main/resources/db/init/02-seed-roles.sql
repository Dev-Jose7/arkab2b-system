-- directory-service does not own IAM role seed data.
-- Seed minimal tenant/organization used by integrated local stack.
INSERT INTO organization (
    organization_id,
    organization_code,
    legal_name,
    trade_name,
    country_code,
    currency_code,
    timezone,
    segment_tier,
    status,
    created_at,
    updated_at
)
VALUES (
    'tenant-demo',
    'TENANT-DEMO',
    'Tenant Demo Organization',
    'Tenant Demo',
    'CO',
    'COP',
    'America/Bogota',
    'FOUNDATION',
    'ACTIVE',
    NOW(),
    NOW()
)
ON CONFLICT (organization_id) DO UPDATE
SET
    status = EXCLUDED.status,
    updated_at = NOW();

INSERT INTO address (
    address_id,
    organization_id,
    address_type,
    alias,
    line1,
    line2,
    city,
    state_region,
    postal_code,
    country_code,
    reference,
    latitude,
    longitude,
    is_default,
    status,
    validation_status,
    validated_at,
    created_at,
    updated_at
)
VALUES (
    'addr-tenant-demo-hq',
    'tenant-demo',
    'SHIPPING',
    'Main HQ',
    'Cra 7 # 32-16',
    NULL,
    'Bogota',
    'Cundinamarca',
    '110311',
    'CO',
    'Seeded default address',
    NULL,
    NULL,
    TRUE,
    'ACTIVE',
    'VERIFIED',
    NOW(),
    NOW(),
    NOW()
)
ON CONFLICT (address_id) DO UPDATE
SET
    status = EXCLUDED.status,
    validation_status = EXCLUDED.validation_status,
    is_default = EXCLUDED.is_default,
    updated_at = NOW();

UPDATE organization_country_policy
SET
    status = 'SUPERSEDED',
    effective_to = COALESCE(effective_to, NOW()),
    updated_at = NOW()
WHERE organization_id = 'tenant-demo'
  AND country_code = 'CO'
  AND status = 'ACTIVE'
  AND policy_id <> 'policy-tenant-demo-co-v1';

INSERT INTO organization_country_policy (
    policy_id,
    organization_id,
    country_code,
    policy_version,
    currency_code,
    week_starts_on,
    weekly_cutoff_local_time,
    timezone,
    reporting_retention_days,
    requires_verified_address,
    effective_from,
    effective_to,
    status,
    created_at,
    updated_at
)
VALUES (
    'policy-tenant-demo-co-v1',
    'tenant-demo',
    'CO',
    1,
    'COP',
    'MONDAY',
    '18:00:00',
    'America/Bogota',
    90,
    TRUE,
    NOW() - INTERVAL '1 day',
    NULL,
    'ACTIVE',
    NOW(),
    NOW()
)
ON CONFLICT (policy_id) DO UPDATE
SET
    status = EXCLUDED.status,
    currency_code = EXCLUDED.currency_code,
    updated_at = NOW();
