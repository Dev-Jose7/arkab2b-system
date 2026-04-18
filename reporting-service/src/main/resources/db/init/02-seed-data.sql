INSERT INTO operations_kpi_projections (
    projection_id,
    tenant_id,
    period,
    kpi_name,
    kpi_value,
    version,
    created_at,
    updated_at
)
VALUES
    (
        'kpi-notif-eff-tenant-demo-2026w01',
        'tenant-demo',
        '2026-W01',
        'notification_effectiveness',
        95.00,
        0,
        NOW(),
        NOW()
    )
ON CONFLICT (tenant_id, period, kpi_name) DO NOTHING;
