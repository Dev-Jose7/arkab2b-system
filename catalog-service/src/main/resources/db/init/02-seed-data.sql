INSERT INTO brands (
    brand_id,
    organization_id,
    brand_code,
    brand_name,
    status,
    created_at,
    updated_at
)
VALUES
    ('brand-acme', 'organization-demo', 'ACME', 'ACME', 'ACTIVE', NOW(), NOW()),
    ('brand-globo', 'organization-demo', 'GLOBO', 'Globo', 'ACTIVE', NOW(), NOW()),
    ('brand-old', 'organization-demo', 'OLD', 'Old Brand', 'INACTIVE', NOW(), NOW())
ON CONFLICT (brand_id) DO NOTHING;

INSERT INTO categories (
    category_id,
    organization_id,
    category_code,
    category_name,
    status,
    created_at,
    updated_at
)
VALUES
    ('category-beverages', 'organization-demo', 'BEVERAGES', 'Beverages', 'ACTIVE', NOW(), NOW()),
    ('category-snacks', 'organization-demo', 'SNACKS', 'Snacks', 'ACTIVE', NOW(), NOW()),
    ('category-obsolete', 'organization-demo', 'OBSOLETE', 'Obsolete', 'INACTIVE', NOW(), NOW())
ON CONFLICT (category_id) DO NOTHING;

INSERT INTO brands (
    brand_id,
    organization_id,
    brand_code,
    brand_name,
    status,
    created_at,
    updated_at
)
VALUES
    ('brand-phase6-acme', 'organization-phase6', 'ACME', 'ACME', 'ACTIVE', NOW(), NOW()),
    ('brand-phase6-globo', 'organization-phase6', 'GLOBO', 'Globo', 'ACTIVE', NOW(), NOW())
ON CONFLICT (brand_id) DO NOTHING;

INSERT INTO categories (
    category_id,
    organization_id,
    category_code,
    category_name,
    status,
    created_at,
    updated_at
)
VALUES
    ('category-phase6-beverages', 'organization-phase6', 'BEVERAGES', 'Beverages', 'ACTIVE', NOW(), NOW()),
    ('category-phase6-snacks', 'organization-phase6', 'SNACKS', 'Snacks', 'ACTIVE', NOW(), NOW())
ON CONFLICT (category_id) DO NOTHING;

INSERT INTO products (
    product_id,
    organization_id,
    product_code,
    product_name,
    description,
    brand_id,
    category_id,
    status,
    created_at,
    updated_at
)
VALUES
    (
        'product-demo-coffee-500',
        'organization-demo',
        'COFFEE-500',
        'Cafe Molido 500g',
        'Cafe molido para baseline integrada',
        'brand-acme',
        'category-beverages',
        'ACTIVE',
        NOW(),
        NOW()
    ),
    (
        'product-phase6-coffee-500',
        'organization-phase6',
        'COFFEE-500',
        'Cafe Molido 500g',
        'Cafe molido para flujo de compra phase6',
        'brand-phase6-acme',
        'category-phase6-beverages',
        'ACTIVE',
        NOW(),
        NOW()
    )
ON CONFLICT (product_id) DO NOTHING;

INSERT INTO variants (
    variant_id,
    organization_id,
    product_id,
    sku,
    variant_name,
    description,
    status,
    sellable_from,
    sellable_until,
    weight_grams,
    created_at,
    updated_at
)
VALUES
    (
        'variant-demo-coffee-500',
        'organization-demo',
        'product-demo-coffee-500',
        'SKU-DEMO-COFFEE-500',
        'Cafe Molido 500g',
        'Variante vendible baseline demo',
        'SELLABLE',
        NOW() - INTERVAL '30 day',
        NULL,
        500,
        NOW(),
        NOW()
    ),
    (
        'variant-phase6-coffee-500',
        'organization-phase6',
        'product-phase6-coffee-500',
        'SKU-PHASE6-COFFEE-500',
        'Cafe Molido 500g',
        'Variante vendible baseline phase6',
        'SELLABLE',
        NOW() - INTERVAL '30 day',
        NULL,
        500,
        NOW(),
        NOW()
    )
ON CONFLICT (variant_id) DO NOTHING;

INSERT INTO prices (
    price_id,
    organization_id,
    variant_id,
    price_type,
    currency,
    amount,
    effective_from,
    effective_until,
    status,
    created_at,
    updated_at
)
VALUES
    (
        'price-demo-coffee-base',
        'organization-demo',
        'variant-demo-coffee-500',
        'BASE',
        'COP',
        18500.0000,
        NOW() - INTERVAL '1 day',
        NULL,
        'ACTIVE',
        NOW(),
        NOW()
    ),
    (
        'price-phase6-coffee-base',
        'organization-phase6',
        'variant-phase6-coffee-500',
        'BASE',
        'COP',
        19900.0000,
        NOW() - INTERVAL '1 day',
        NULL,
        'ACTIVE',
        NOW(),
        NOW()
    )
ON CONFLICT (price_id) DO NOTHING;
