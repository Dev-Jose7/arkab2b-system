INSERT INTO operations_kpi_projections (
    projection_id,
    organization_id,
    period,
    kpi_name,
    kpi_value,
    version,
    created_at,
    updated_at
)
VALUES
    (
        '0395f80c-9ea0-4ddd-a588-badd3d661601',
        '6f6d04b6-4c9d-4b6a-8e13-5b2f9c0a1d77',
        '2026-W17',
        'order_fill_rate',
        97.50,
        0,
        NOW(),
        NOW()
    )
ON CONFLICT (organization_id, period, kpi_name) DO UPDATE
SET
    projection_id = EXCLUDED.projection_id,
    kpi_value = EXCLUDED.kpi_value,
    version = EXCLUDED.version,
    updated_at = NOW();
