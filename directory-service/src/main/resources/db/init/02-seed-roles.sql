-- directory-service does not own IAM role seed data.
-- Seed baseline organization context used by integrated local stack.

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
        '8b88f0d3-7f1e-4f5e-b2d0-3dfb2ac6f3a1',
        'NovaCore Integraciones SAS',
        'NovaCore TI',
        'CO',
        'COP',
        'America/Bogota',
        'FOUNDATION',
        'ACTIVE',
        NOW(),
        NOW()
    ),
    (
        '6f6d04b6-4c9d-4b6a-8e13-5b2f9c0a1d77',
        'Arka Distribuciones SAS',
        'Arka B2B',
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
    legal_name = EXCLUDED.legal_name,
    trade_name = EXCLUDED.trade_name,
    country_code = EXCLUDED.country_code,
    currency_code = EXCLUDED.currency_code,
    timezone = EXCLUDED.timezone,
    segment_tier = EXCLUDED.segment_tier,
    status = EXCLUDED.status,
    updated_at = NOW();

INSERT INTO organization_legal_profile (
    legal_profile_id,
    organization_id,
    tax_id_type,
    tax_id,
    fiscal_regime,
    legal_representative,
    country_code,
    verification_status,
    verified_at,
    created_at,
    updated_at
)
VALUES
    (
        'a844ef93-e31f-4f3c-9c4f-1a2ffdb058d1',
        '8b88f0d3-7f1e-4f5e-b2d0-3dfb2ac6f3a1',
        'NIT',
        '901815274-1',
        'RESPONSABLE_IVA',
        'Paula Andrea Vargas',
        'CO',
        'VERIFIED',
        NOW(),
        NOW(),
        NOW()
    ),
    (
        'f1848a9e-7f5e-4939-8bd8-a4cf3dbfd3e3',
        '6f6d04b6-4c9d-4b6a-8e13-5b2f9c0a1d77',
        'NIT',
        '901642318-4',
        'RESPONSABLE_IVA',
        'Santiago Cardenas Mejia',
        'CO',
        'VERIFIED',
        NOW(),
        NOW(),
        NOW()
    )
ON CONFLICT (legal_profile_id) DO UPDATE
SET
    tax_id_type = EXCLUDED.tax_id_type,
    tax_id = EXCLUDED.tax_id,
    fiscal_regime = EXCLUDED.fiscal_regime,
    legal_representative = EXCLUDED.legal_representative,
    country_code = EXCLUDED.country_code,
    verification_status = EXCLUDED.verification_status,
    verified_at = EXCLUDED.verified_at,
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
        '1d3f5c0a-7d1c-4305-96b4-3cde13d8ac8f',
        '8b88f0d3-7f1e-4f5e-b2d0-3dfb2ac6f3a1',
        'SHIPPING',
        'Bodega metropolitana',
        'Calle 10 Sur # 50FF-84',
        'Bodega 7',
        'Medellin',
        'Antioquia',
        '050022',
        'CO',
        'Centro de despacho para integradores y resellers de perifericos.',
        NULL,
        NULL,
        TRUE,
        'ACTIVE',
        'VERIFIED',
        NOW(),
        NOW(),
        NOW()
    ),
    (
        '3c43c7db-bf2c-4f0a-bf63-8e0f7d8a1e9a',
        '6f6d04b6-4c9d-4b6a-8e13-5b2f9c0a1d77',
        'SHIPPING',
        'Centro de distribucion principal',
        'Autopista Norte # 97-50',
        'Bodega 12',
        'Bogota',
        'Cundinamarca',
        '110221',
        'CO',
        'Centro de distribucion Arka para teclados, almacenamiento y accesorios de PC.',
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
    alias = EXCLUDED.alias,
    line1 = EXCLUDED.line1,
    line2 = EXCLUDED.line2,
    city = EXCLUDED.city,
    state_region = EXCLUDED.state_region,
    postal_code = EXCLUDED.postal_code,
    country_code = EXCLUDED.country_code,
    reference = EXCLUDED.reference,
    is_default = EXCLUDED.is_default,
    status = EXCLUDED.status,
    validation_status = EXCLUDED.validation_status,
    validated_at = EXCLUDED.validated_at,
    updated_at = NOW();

UPDATE organization_country_policy
SET
    status = 'SUPERSEDED',
    effective_to = COALESCE(effective_to, NOW()),
    updated_at = NOW()
WHERE organization_id = '8b88f0d3-7f1e-4f5e-b2d0-3dfb2ac6f3a1'
  AND country_code = 'CO'
  AND status = 'ACTIVE'
  AND policy_id <> 'c5ebf957-6df6-432c-8e60-e9ee3c33ae45';

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
        'c5ebf957-6df6-432c-8e60-e9ee3c33ae45',
        '8b88f0d3-7f1e-4f5e-b2d0-3dfb2ac6f3a1',
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
    policy_version = EXCLUDED.policy_version,
    currency_code = EXCLUDED.currency_code,
    week_starts_on = EXCLUDED.week_starts_on,
    weekly_cutoff_local_time = EXCLUDED.weekly_cutoff_local_time,
    timezone = EXCLUDED.timezone,
    reporting_retention_days = EXCLUDED.reporting_retention_days,
    requires_verified_address = EXCLUDED.requires_verified_address,
    effective_from = EXCLUDED.effective_from,
    effective_to = EXCLUDED.effective_to,
    status = EXCLUDED.status,
    updated_at = NOW();

UPDATE organization_country_policy
SET
    status = 'SUPERSEDED',
    effective_to = COALESCE(effective_to, NOW()),
    updated_at = NOW()
WHERE organization_id = '6f6d04b6-4c9d-4b6a-8e13-5b2f9c0a1d77'
  AND country_code = 'CO'
  AND status = 'ACTIVE'
  AND policy_id <> 'f7c6c218-2bb4-4b4c-a7dc-7d4b9e2ef3f1';

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
        'f7c6c218-2bb4-4b4c-a7dc-7d4b9e2ef3f1',
        '6f6d04b6-4c9d-4b6a-8e13-5b2f9c0a1d77',
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
    policy_version = EXCLUDED.policy_version,
    currency_code = EXCLUDED.currency_code,
    week_starts_on = EXCLUDED.week_starts_on,
    weekly_cutoff_local_time = EXCLUDED.weekly_cutoff_local_time,
    timezone = EXCLUDED.timezone,
    reporting_retention_days = EXCLUDED.reporting_retention_days,
    requires_verified_address = EXCLUDED.requires_verified_address,
    effective_from = EXCLUDED.effective_from,
    effective_to = EXCLUDED.effective_to,
    status = EXCLUDED.status,
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
        '7b10db82-291f-49a3-a8fa-d2221872b33e',
        '8b88f0d3-7f1e-4f5e-b2d0-3dfb2ac6f3a1',
        'EMAIL',
        'Operaciones Medellin',
        'operaciones@novacoreti.co',
        'o***@novacoreti.co',
        TRUE,
        'ACTIVE',
        NOW(),
        NOW()
    ),
    (
        '1d7288ce-a640-4907-a3e9-af9d6a3cc17f',
        '8b88f0d3-7f1e-4f5e-b2d0-3dfb2ac6f3a1',
        'PHONE',
        'Linea comercial',
        '+5746041180',
        '+57******1180',
        TRUE,
        'ACTIVE',
        NOW(),
        NOW()
    ),
    (
        'adcf4de2-d8e1-4a70-9cbb-3091ed021fd8',
        '6f6d04b6-4c9d-4b6a-8e13-5b2f9c0a1d77',
        'EMAIL',
        'Operaciones B2B',
        'operaciones@arka.co',
        'o***@arka.co',
        TRUE,
        'ACTIVE',
        NOW(),
        NOW()
    ),
    (
        '5c2b59de-2d87-4afd-9c75-bf2bc1329cff',
        '6f6d04b6-4c9d-4b6a-8e13-5b2f9c0a1d77',
        'PHONE',
        'Linea comercial',
        '+573001110506',
        '+57******0506',
        TRUE,
        'ACTIVE',
        NOW(),
        NOW()
    )
ON CONFLICT (contact_id) DO UPDATE
SET
    label = EXCLUDED.label,
    value_normalized = EXCLUDED.value_normalized,
    value_masked = EXCLUDED.value_masked,
    is_primary = EXCLUDED.is_primary,
    status = EXCLUDED.status,
    updated_at = NOW();
