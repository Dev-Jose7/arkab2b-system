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
    ('4d01aa38-baf1-4300-ad88-d1d97c4cb447', '8b88f0d3-7f1e-4f5e-b2d0-3dfb2ac6f3a1', 'LOGITECH', 'Logitech', 'ACTIVE', NOW(), NOW()),
    ('27acb8a9-eef9-4d8f-86b9-93f9d71cf3e5', '8b88f0d3-7f1e-4f5e-b2d0-3dfb2ac6f3a1', 'TPLINK', 'TP-Link', 'ACTIVE', NOW(), NOW()),
    ('e1af4db9-7a9a-4c40-b8a0-d9737d5988d8', '6f6d04b6-4c9d-4b6a-8e13-5b2f9c0a1d77', 'REDRAGON', 'Redragon', 'ACTIVE', NOW(), NOW()),
    ('7d5dd0e5-7fd4-4c80-9592-df9a6b5d2159', '6f6d04b6-4c9d-4b6a-8e13-5b2f9c0a1d77', 'KINGSTON', 'Kingston', 'ACTIVE', NOW(), NOW())
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
    ('2be89df2-2f04-4ba3-8d13-6b0c4dff0c5e', '8b88f0d3-7f1e-4f5e-b2d0-3dfb2ac6f3a1', 'MICE', 'Mice y apuntadores', 'ACTIVE', NOW(), NOW()),
    ('c2f4085b-f11d-486c-a5d1-3c427ca7c859', '8b88f0d3-7f1e-4f5e-b2d0-3dfb2ac6f3a1', 'CONNECTIVITY', 'Conectividad USB y red', 'ACTIVE', NOW(), NOW()),
    ('0a17f6d8-d7a7-4f22-bb9f-8ae735ef3c95', '6f6d04b6-4c9d-4b6a-8e13-5b2f9c0a1d77', 'KEYBOARDS', 'Teclados mecanicos', 'ACTIVE', NOW(), NOW()),
    ('eab40527-9a6c-4e14-93d6-45b2a6ccf08d', '6f6d04b6-4c9d-4b6a-8e13-5b2f9c0a1d77', 'STORAGE', 'Almacenamiento SSD', 'ACTIVE', NOW(), NOW())
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
        '0b5bf628-e499-4ba9-a352-ae77f722c650',
        '8b88f0d3-7f1e-4f5e-b2d0-3dfb2ac6f3a1',
        'M185-WL',
        'Mouse inalambrico Logitech M185 gris',
        'Mouse inalambrico de entrada para oficinas, puntos de venta y estaciones administrativas.',
        '4d01aa38-baf1-4300-ad88-d1d97c4cb447',
        '2be89df2-2f04-4ba3-8d13-6b0c4dff0c5e',
        'ACTIVE',
        NOW(),
        NOW()
    ),
    (
        '659721ef-63a2-48f5-b620-d0cf91b81de2',
        '8b88f0d3-7f1e-4f5e-b2d0-3dfb2ac6f3a1',
        'UE300-USB',
        'Adaptador USB 3.0 a Gigabit TP-Link UE300',
        'Adaptador de red USB para estaciones sin puerto ethernet dedicado.',
        '27acb8a9-eef9-4d8f-86b9-93f9d71cf3e5',
        'c2f4085b-f11d-486c-a5d1-3c427ca7c859',
        'ACTIVE',
        NOW(),
        NOW()
    ),
    (
        '1ecf779c-0baf-4fd4-a2f6-720be51a12f0',
        '6f6d04b6-4c9d-4b6a-8e13-5b2f9c0a1d77',
        'K552-RGB',
        'Teclado mecanico Redragon Kumara K552 RGB',
        'Teclado mecanico TKL para canal gamer, corporativo y armado de puestos de trabajo.',
        'e1af4db9-7a9a-4c40-b8a0-d9737d5988d8',
        '0a17f6d8-d7a7-4f22-bb9f-8ae735ef3c95',
        'ACTIVE',
        NOW(),
        NOW()
    ),
    (
        '24f9bc61-bbf2-4318-8d6f-09f8cd4e2c51',
        '6f6d04b6-4c9d-4b6a-8e13-5b2f9c0a1d77',
        'A400-480',
        'SSD Kingston A400 480GB SATA',
        'Unidad de estado solido para actualizacion de equipos corporativos y ensamble de PC.',
        '7d5dd0e5-7fd4-4c80-9592-df9a6b5d2159',
        'eab40527-9a6c-4e14-93d6-45b2a6ccf08d',
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
        '9ebaa7c4-cd85-4cfa-b916-86a390412534',
        '8b88f0d3-7f1e-4f5e-b2d0-3dfb2ac6f3a1',
        '0b5bf628-e499-4ba9-a352-ae77f722c650',
        'ARK-LGT-M185-GRY',
        'Mouse Logitech M185 gris',
        'Variante vendible orientada a reposicion de estaciones de trabajo.',
        'SELLABLE',
        NOW() - INTERVAL '30 day',
        NULL,
        75,
        NOW(),
        NOW()
    ),
    (
        '12ec4873-724d-44a8-9f85-3f6fdff36c8c',
        '8b88f0d3-7f1e-4f5e-b2d0-3dfb2ac6f3a1',
        '659721ef-63a2-48f5-b620-d0cf91b81de2',
        'ARK-TPL-UE300',
        'TP-Link UE300 USB 3.0 a Gigabit',
        'Variante vendible para estaciones que requieren conectividad cableada.',
        'SELLABLE',
        NOW() - INTERVAL '30 day',
        NULL,
        50,
        NOW(),
        NOW()
    ),
    (
        'f5d14d62-7fbd-4b0b-9c97-872c62c0fd8e',
        '6f6d04b6-4c9d-4b6a-8e13-5b2f9c0a1d77',
        '1ecf779c-0baf-4fd4-a2f6-720be51a12f0',
        'ARK-RDG-K552-RGB',
        'Redragon Kumara K552 RGB',
        'Variante vendible principal para el flujo de compra B2B de Arka.',
        'SELLABLE',
        NOW() - INTERVAL '30 day',
        NULL,
        890,
        NOW(),
        NOW()
    ),
    (
        '3b84ec72-acde-4433-a55f-cd5e47d2a8f5',
        '6f6d04b6-4c9d-4b6a-8e13-5b2f9c0a1d77',
        '24f9bc61-bbf2-4318-8d6f-09f8cd4e2c51',
        'ARK-KNG-A400-480',
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
        'de8e1c86-23a5-49e0-bf56-48e0887a97d3',
        '8b88f0d3-7f1e-4f5e-b2d0-3dfb2ac6f3a1',
        '9ebaa7c4-cd85-4cfa-b916-86a390412534',
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
        '749c84d8-7144-4ca6-8c7e-05ca40fae535',
        '8b88f0d3-7f1e-4f5e-b2d0-3dfb2ac6f3a1',
        '12ec4873-724d-44a8-9f85-3f6fdff36c8c',
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
        '0db608d9-0ab8-44bf-95c2-3880f214d47c',
        '6f6d04b6-4c9d-4b6a-8e13-5b2f9c0a1d77',
        'f5d14d62-7fbd-4b0b-9c97-872c62c0fd8e',
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
        '53d3a9bd-f595-43a8-9c55-f2cb5e5d8a0c',
        '6f6d04b6-4c9d-4b6a-8e13-5b2f9c0a1d77',
        '3b84ec72-acde-4433-a55f-cd5e47d2a8f5',
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
