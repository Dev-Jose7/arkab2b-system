-- directory-service does not own IAM role seed data.
-- Seed minimal organization/organization used by integrated local stack.
INSERT INTO organization (
    organization_id,
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
    'organization-demo',
    'Organization Demo Organization',
    'Organization Demo',
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
    'addr-organization-demo-hq',
    'organization-demo',
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
WHERE organization_id = 'organization-demo'
  AND country_code = 'CO'
  AND status = 'ACTIVE'
  AND policy_id <> 'policy-organization-demo-co-v1';

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
    'policy-organization-demo-co-v1',
    'organization-demo',
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

INSERT INTO organization_contact (
    contact_id,
    organization_id,
    contact_type,
    label,
    value_normalized,
    value_masked,
    is_primary,
    status,
    created_at,
    updated_at
)
VALUES (
    'contact-organization-demo-email',
    'organization-demo',
    'EMAIL',
    'Operations',
    'ops+organization-demo@arka.test',
    'o***@arka.test',
    TRUE,
    'ACTIVE',
    NOW(),
    NOW()
)
ON CONFLICT (contact_id) DO UPDATE
SET
    value_normalized = EXCLUDED.value_normalized,
    value_masked = EXCLUDED.value_masked,
    is_primary = EXCLUDED.is_primary,
    status = EXCLUDED.status,
    updated_at = NOW();

INSERT INTO organization (
    organization_id,
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
VALUES
    (
        'organization-phase6',
        'Phase 6 Organization',
        'Phase 6',
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
VALUES
    (
        'addr-organization-phase6-hq',
        'organization-phase6',
        'SHIPPING',
        'Phase6 HQ',
        'Cra 11 # 93-52',
        NULL,
        'Bogota',
        'Cundinamarca',
        '110221',
        'CO',
        'Seeded phase6 address',
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
WHERE organization_id = 'organization-phase6'
  AND country_code = 'CO'
  AND status = 'ACTIVE'
  AND policy_id NOT IN ('policy-organization-phase6-co-v1');

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
VALUES
    (
        'policy-organization-phase6-co-v1',
        'organization-phase6',
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

INSERT INTO organization_contact (
    contact_id,
    organization_id,
    contact_type,
    label,
    value_normalized,
    value_masked,
    is_primary,
    status,
    created_at,
    updated_at
)
VALUES
    (
        'contact-organization-phase6-email',
        'organization-phase6',
        'EMAIL',
        'Operations',
        'ops+organization-phase6@arka.test',
        'o***@arka.test',
        TRUE,
        'ACTIVE',
        NOW(),
        NOW()
    ),
    (
        'contact-organization-phase6-phone',
        'organization-phase6',
        'PHONE',
        'Hotline',
        '+573001110006',
        '+57******0006',
        TRUE,
        'ACTIVE',
        NOW(),
        NOW()
    )
ON CONFLICT (contact_id) DO UPDATE
SET
    value_normalized = EXCLUDED.value_normalized,
    value_masked = EXCLUDED.value_masked,
    is_primary = EXCLUDED.is_primary,
    status = EXCLUDED.status,
    updated_at = NOW();
