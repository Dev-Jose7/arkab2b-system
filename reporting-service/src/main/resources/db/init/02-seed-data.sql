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
        'kpi-order-fill-rate-orgdemo-2026w01',
        'organization-demo',
        '2026-W01',
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
