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
        'MAIN',
        'Warehouse Demo Main',
        'CO',
        'ACTIVE',
        NOW(),
        NOW()
    ),
    (
        'warehouse-phase6-main',
        'organization-phase6',
        'MAIN',
        'Warehouse Phase6 Main',
        'CO',
        'ACTIVE',
        NOW(),
        NOW()
    )
ON CONFLICT (warehouse_id) DO UPDATE
SET
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
        'stock-item-demo-coffee-500',
        'organization-demo',
        'warehouse-demo-main',
        'SKU-DEMO-COFFEE-500',
        100,
        2,
        20,
        10,
        'ACTIVE',
        0,
        NOW(),
        NOW()
    ),
    (
        'stock-item-phase6-coffee-500',
        'organization-phase6',
        'warehouse-phase6-main',
        'SKU-PHASE6-COFFEE-500',
        80,
        2,
        20,
        10,
        'ACTIVE',
        0,
        NOW(),
        NOW()
    )
ON CONFLICT (stock_item_id) DO UPDATE
SET
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
        'reservation-demo-checkout-1',
        'organization-demo',
        'stock-item-demo-coffee-500',
        'warehouse-demo-main',
        'SKU-DEMO-COFFEE-500',
        'cart-demo-seeded',
        'order-demo-seeded',
        2,
        'CONFIRMED',
        NOW() + INTERVAL '7 day',
        NOW() - INTERVAL '1 minute',
        NULL,
        NOW(),
        NOW()
    ),
    (
        'reservation-phase6-checkout-1',
        'organization-phase6',
        'stock-item-phase6-coffee-500',
        'warehouse-phase6-main',
        'SKU-PHASE6-COFFEE-500',
        'cart-phase6-seeded',
        'order-phase6-seeded',
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
    qty = EXCLUDED.qty,
    status = EXCLUDED.status,
    expires_at = EXCLUDED.expires_at,
    confirmed_at = EXCLUDED.confirmed_at,
    released_at = EXCLUDED.released_at,
    updated_at = NOW();
