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
        'kpi-notif-eff-orgdemo-2026w01',
        'organization-demo',
        '2026-W01',
        'notification_effectiveness',
        95.00,
        0,
        NOW(),
        NOW()
    )
ON CONFLICT (organization_id, period, kpi_name) DO NOTHING;
