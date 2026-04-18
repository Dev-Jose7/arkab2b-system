INSERT INTO user_account (
    user_id,
    email,
    status,
    failed_login_count,
    created_at,
    updated_at
)
VALUES (
    'svc-arka-system',
    'svc-arka-system@service.local',
    'ACTIVE',
    0,
    NOW(),
    NOW()
)
ON CONFLICT (user_id) DO UPDATE SET
    status = EXCLUDED.status,
    updated_at = NOW();

INSERT INTO user_role_assignment (
    assignment_id,
    user_id,
    role_id,
    status,
    assigned_by,
    assigned_at,
    created_at,
    updated_at
)
SELECT
    'svc-arka-system-arka-admin',
    'svc-arka-system',
    r.role_id,
    'ACTIVE',
    'svc-arka-system',
    NOW(),
    NOW(),
    NOW()
FROM role r
WHERE r.role_code = 'ARKA_ADMIN'
ON CONFLICT (user_id, role_id) DO UPDATE SET
    status = EXCLUDED.status,
    updated_at = EXCLUDED.updated_at;
