INSERT INTO brands (
    brand_id,
    tenant_id,
    brand_code,
    brand_name,
    status,
    created_at,
    updated_at
)
VALUES
    ('brand-acme', 'tenant-demo', 'ACME', 'ACME', 'ACTIVE', NOW(), NOW()),
    ('brand-globo', 'tenant-demo', 'GLOBO', 'Globo', 'ACTIVE', NOW(), NOW()),
    ('brand-old', 'tenant-demo', 'OLD', 'Old Brand', 'INACTIVE', NOW(), NOW())
ON CONFLICT (brand_id) DO NOTHING;

INSERT INTO categories (
    category_id,
    tenant_id,
    category_code,
    category_name,
    status,
    created_at,
    updated_at
)
VALUES
    ('category-beverages', 'tenant-demo', 'BEVERAGES', 'Beverages', 'ACTIVE', NOW(), NOW()),
    ('category-snacks', 'tenant-demo', 'SNACKS', 'Snacks', 'ACTIVE', NOW(), NOW()),
    ('category-obsolete', 'tenant-demo', 'OBSOLETE', 'Obsolete', 'INACTIVE', NOW(), NOW())
ON CONFLICT (category_id) DO NOTHING;
