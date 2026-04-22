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
        'organization-demo',
        'Arka Demo SAS',
        'Arka Demo',
        'CO',
        'COP',
        'America/Bogota',
        'FOUNDATION',
        'ACTIVE',
        NOW(),
        NOW()
    ),
    (
        'organization-phase6',
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
        'legal-profile-organization-demo',
        'organization-demo',
        'NIT',
        '901654321-7',
        'RESPONSABLE_IVA',
        'Laura Mendoza Rios',
        'CO',
        'VERIFIED',
        NOW(),
        NOW(),
        NOW()
    ),
    (
        'legal-profile-organization-phase6',
        'organization-phase6',
        'NIT',
        '900812345-6',
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
        'addr-organization-demo-hq',
        'organization-demo',
        'SHIPPING',
        'Centro logistico demo',
        'Calle 72 # 20-37',
        'Bodega 4',
        'Bogota',
        'Cundinamarca',
        '110231',
        'CO',
        'Bodega local de pruebas para integracion de accesorios PC',
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
        'addr-organization-phase6-hq',
        'organization-phase6',
        'SHIPPING',
        'Centro de distribucion norte',
        'Autopista Norte # 97-50',
        'Bodega 12',
        'Bogota',
        'Cundinamarca',
        '110221',
        'CO',
        'Centro de distribucion Arka para perifericos y accesorios de PC',
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
VALUES
    (
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
WHERE organization_id = 'organization-phase6'
  AND country_code = 'CO'
  AND status = 'ACTIVE'
  AND policy_id <> 'policy-organization-phase6-co-v1';

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
        'contact-organization-demo-email',
        'organization-demo',
        'EMAIL',
        'Operaciones demo',
        'operaciones.demo@arka-b2b.test',
        'o***@arka-b2b.test',
        TRUE,
        'ACTIVE',
        NOW(),
        NOW()
    ),
    (
        'contact-organization-phase6-email',
        'organization-phase6',
        'EMAIL',
        'Operaciones B2B',
        'operaciones.b2b@arka-b2b.test',
        'o***@arka-b2b.test',
        TRUE,
        'ACTIVE',
        NOW(),
        NOW()
    ),
    (
        'contact-organization-phase6-phone',
        'organization-phase6',
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
