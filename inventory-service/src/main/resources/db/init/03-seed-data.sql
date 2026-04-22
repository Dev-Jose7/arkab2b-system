INSERT INTO warehouses (
    warehouse_id,
    organization_id,
    warehouse_code,
    warehouse_name,
    country_code,
    status,
    created_at,
    updated_at
)
VALUES
    (
        'warehouse-demo-main',
        'organization-demo',
        'BOG-MAIN',
        'Centro logistico demo Bogota',
        'CO',
        'ACTIVE',
        NOW(),
        NOW()
    ),
    (
        'warehouse-phase6-main',
        'organization-phase6',
        'BOG-NORTE',
        'Centro de distribucion Arka Bogota',
        'CO',
        'ACTIVE',
        NOW(),
        NOW()
    )
ON CONFLICT (warehouse_id) DO UPDATE
SET
    organization_id = EXCLUDED.organization_id,
    warehouse_code = EXCLUDED.warehouse_code,
    warehouse_name = EXCLUDED.warehouse_name,
    country_code = EXCLUDED.country_code,
    status = EXCLUDED.status,
    updated_at = NOW();

INSERT INTO stock_items (
    stock_item_id,
    organization_id,
    warehouse_id,
    sku,
    physical_qty,
    reserved_qty,
    reorder_point,
    safety_stock,
    status,
    version,
    created_at,
    updated_at
)
VALUES
    (
        'stock-item-demo-m185',
        'organization-demo',
        'warehouse-demo-main',
        'SKU-DEMO-M185-GRY',
        120,
        2,
        25,
        12,
        'ACTIVE',
        0,
        NOW(),
        NOW()
    ),
    (
        'stock-item-demo-ue300',
        'organization-demo',
        'warehouse-demo-main',
        'SKU-DEMO-UE300',
        60,
        0,
        10,
        5,
        'ACTIVE',
        0,
        NOW(),
        NOW()
    ),
    (
        'stock-item-phase6-k552',
        'organization-phase6',
        'warehouse-phase6-main',
        'SKU-PHASE6-K552-RGB',
        48,
        2,
        8,
        4,
        'ACTIVE',
        0,
        NOW(),
        NOW()
    ),
    (
        'stock-item-phase6-a400-480',
        'organization-phase6',
        'warehouse-phase6-main',
        'SKU-PHASE6-A400-480',
        70,
        0,
        12,
        6,
        'ACTIVE',
        0,
        NOW(),
        NOW()
    )
ON CONFLICT (stock_item_id) DO UPDATE
SET
    organization_id = EXCLUDED.organization_id,
    warehouse_id = EXCLUDED.warehouse_id,
    sku = EXCLUDED.sku,
    physical_qty = EXCLUDED.physical_qty,
    reserved_qty = EXCLUDED.reserved_qty,
    reorder_point = EXCLUDED.reorder_point,
    safety_stock = EXCLUDED.safety_stock,
    status = EXCLUDED.status,
    updated_at = NOW();

INSERT INTO stock_reservations (
    reservation_id,
    organization_id,
    stock_item_id,
    warehouse_id,
    sku,
    cart_id,
    order_id,
    qty,
    status,
    expires_at,
    confirmed_at,
    released_at,
    created_at,
    updated_at
)
VALUES
    (
        'reservation-demo-m185-1',
        'organization-demo',
        'stock-item-demo-m185',
        'warehouse-demo-main',
        'SKU-DEMO-M185-GRY',
        'cart-demo-m185-seeded',
        'order-demo-m185-seeded',
        2,
        'CONFIRMED',
        NOW() + INTERVAL '7 day',
        NOW() - INTERVAL '1 minute',
        NULL,
        NOW(),
        NOW()
    ),
    (
        'reservation-phase6-k552-1',
        'organization-phase6',
        'stock-item-phase6-k552',
        'warehouse-phase6-main',
        'SKU-PHASE6-K552-RGB',
        'cart-phase6-k552-seeded',
        'order-phase6-k552-seeded',
        2,
        'CONFIRMED',
        NOW() + INTERVAL '7 day',
        NOW() - INTERVAL '1 minute',
        NULL,
        NOW(),
        NOW()
    )
ON CONFLICT (reservation_id) DO UPDATE
SET
    organization_id = EXCLUDED.organization_id,
    stock_item_id = EXCLUDED.stock_item_id,
    warehouse_id = EXCLUDED.warehouse_id,
    sku = EXCLUDED.sku,
    cart_id = EXCLUDED.cart_id,
    order_id = EXCLUDED.order_id,
    qty = EXCLUDED.qty,
    status = EXCLUDED.status,
    expires_at = EXCLUDED.expires_at,
    confirmed_at = EXCLUDED.confirmed_at,
    released_at = EXCLUDED.released_at,
    updated_at = NOW();
