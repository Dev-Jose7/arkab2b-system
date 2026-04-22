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
    ('brand-logitech', 'organization-demo', 'LOGITECH', 'Logitech', 'ACTIVE', NOW(), NOW()),
    ('brand-tplink', 'organization-demo', 'TPLINK', 'TP-Link', 'ACTIVE', NOW(), NOW()),
    ('brand-legacy', 'organization-demo', 'LEGACY', 'Legacy Generic', 'INACTIVE', NOW(), NOW()),
    ('brand-phase6-redragon', 'organization-phase6', 'REDRAGON', 'Redragon', 'ACTIVE', NOW(), NOW()),
    ('brand-phase6-kingston', 'organization-phase6', 'KINGSTON', 'Kingston', 'ACTIVE', NOW(), NOW())
ON CONFLICT (brand_id) DO UPDATE
SET
    organization_id = EXCLUDED.organization_id,
    brand_code = EXCLUDED.brand_code,
    brand_name = EXCLUDED.brand_name,
    status = EXCLUDED.status,
    updated_at = NOW();

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
    ('category-mice', 'organization-demo', 'MICE', 'Mice y apuntadores', 'ACTIVE', NOW(), NOW()),
    ('category-connectivity', 'organization-demo', 'CONNECTIVITY', 'Conectividad USB y red', 'ACTIVE', NOW(), NOW()),
    ('category-legacy', 'organization-demo', 'LEGACY', 'Catalogo legado', 'INACTIVE', NOW(), NOW()),
    ('category-phase6-keyboards', 'organization-phase6', 'KEYBOARDS', 'Teclados mecanicos', 'ACTIVE', NOW(), NOW()),
    ('category-phase6-storage', 'organization-phase6', 'STORAGE', 'Almacenamiento SSD', 'ACTIVE', NOW(), NOW())
ON CONFLICT (category_id) DO UPDATE
SET
    organization_id = EXCLUDED.organization_id,
    category_code = EXCLUDED.category_code,
    category_name = EXCLUDED.category_name,
    status = EXCLUDED.status,
    updated_at = NOW();

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
        'product-demo-m185',
        'organization-demo',
        'M185-WL',
        'Mouse inalambrico Logitech M185 gris',
        'Mouse inalambrico de entrada para oficinas, puntos de venta y estaciones administrativas.',
        'brand-logitech',
        'category-mice',
        'ACTIVE',
        NOW(),
        NOW()
    ),
    (
        'product-demo-ue300',
        'organization-demo',
        'UE300-USB',
        'Adaptador USB 3.0 a Gigabit TP-Link UE300',
        'Adaptador de red USB para estaciones sin puerto ethernet dedicado.',
        'brand-tplink',
        'category-connectivity',
        'ACTIVE',
        NOW(),
        NOW()
    ),
    (
        'product-phase6-k552',
        'organization-phase6',
        'K552-RGB',
        'Teclado mecanico Redragon Kumara K552 RGB',
        'Teclado mecanico TKL para canal gamer, corporativo y armado de puestos de trabajo.',
        'brand-phase6-redragon',
        'category-phase6-keyboards',
        'ACTIVE',
        NOW(),
        NOW()
    ),
    (
        'product-phase6-a400-480',
        'organization-phase6',
        'A400-480',
        'SSD Kingston A400 480GB SATA',
        'Unidad de estado solido para actualizacion de equipos corporativos y ensamble de PC.',
        'brand-phase6-kingston',
        'category-phase6-storage',
        'ACTIVE',
        NOW(),
        NOW()
    )
ON CONFLICT (product_id) DO UPDATE
SET
    organization_id = EXCLUDED.organization_id,
    product_code = EXCLUDED.product_code,
    product_name = EXCLUDED.product_name,
    description = EXCLUDED.description,
    brand_id = EXCLUDED.brand_id,
    category_id = EXCLUDED.category_id,
    status = EXCLUDED.status,
    updated_at = NOW();

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
        'variant-demo-m185',
        'organization-demo',
        'product-demo-m185',
        'SKU-DEMO-M185-GRY',
        'Mouse Logitech M185 gris',
        'Variante vendible de mouse inalambrico para baseline demo.',
        'SELLABLE',
        NOW() - INTERVAL '30 day',
        NULL,
        75,
        NOW(),
        NOW()
    ),
    (
        'variant-demo-ue300',
        'organization-demo',
        'product-demo-ue300',
        'SKU-DEMO-UE300',
        'TP-Link UE300 USB 3.0 a Gigabit',
        'Variante vendible de conectividad para baseline demo.',
        'SELLABLE',
        NOW() - INTERVAL '30 day',
        NULL,
        50,
        NOW(),
        NOW()
    ),
    (
        'variant-phase6-k552',
        'organization-phase6',
        'product-phase6-k552',
        'SKU-PHASE6-K552-RGB',
        'Redragon Kumara K552 RGB',
        'Variante vendible principal para flujo de compra Arka B2B.',
        'SELLABLE',
        NOW() - INTERVAL '30 day',
        NULL,
        890,
        NOW(),
        NOW()
    ),
    (
        'variant-phase6-a400-480',
        'organization-phase6',
        'product-phase6-a400-480',
        'SKU-PHASE6-A400-480',
        'Kingston A400 480GB SATA',
        'Variante vendible de almacenamiento para renovacion de equipos.',
        'SELLABLE',
        NOW() - INTERVAL '30 day',
        NULL,
        41,
        NOW(),
        NOW()
    )
ON CONFLICT (variant_id) DO UPDATE
SET
    organization_id = EXCLUDED.organization_id,
    product_id = EXCLUDED.product_id,
    sku = EXCLUDED.sku,
    variant_name = EXCLUDED.variant_name,
    description = EXCLUDED.description,
    status = EXCLUDED.status,
    sellable_from = EXCLUDED.sellable_from,
    sellable_until = EXCLUDED.sellable_until,
    weight_grams = EXCLUDED.weight_grams,
    updated_at = NOW();

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
        'price-demo-m185-base',
        'organization-demo',
        'variant-demo-m185',
        'BASE',
        'COP',
        64900.0000,
        NOW() - INTERVAL '1 day',
        NULL,
        'ACTIVE',
        NOW(),
        NOW()
    ),
    (
        'price-demo-ue300-base',
        'organization-demo',
        'variant-demo-ue300',
        'BASE',
        'COP',
        89900.0000,
        NOW() - INTERVAL '1 day',
        NULL,
        'ACTIVE',
        NOW(),
        NOW()
    ),
    (
        'price-phase6-k552-base',
        'organization-phase6',
        'variant-phase6-k552',
        'BASE',
        'COP',
        189900.0000,
        NOW() - INTERVAL '1 day',
        NULL,
        'ACTIVE',
        NOW(),
        NOW()
    ),
    (
        'price-phase6-a400-480-base',
        'organization-phase6',
        'variant-phase6-a400-480',
        'BASE',
        'COP',
        159900.0000,
        NOW() - INTERVAL '1 day',
        NULL,
        'ACTIVE',
        NOW(),
        NOW()
    )
ON CONFLICT (price_id) DO UPDATE
SET
    organization_id = EXCLUDED.organization_id,
    variant_id = EXCLUDED.variant_id,
    price_type = EXCLUDED.price_type,
    currency = EXCLUDED.currency,
    amount = EXCLUDED.amount,
    effective_from = EXCLUDED.effective_from,
    effective_until = EXCLUDED.effective_until,
    status = EXCLUDED.status,
    updated_at = NOW();
