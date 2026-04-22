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
        'b0d0d293-3f63-4a43-b0c1-8d9c2ac9ce0b',
        '8b88f0d3-7f1e-4f5e-b2d0-3dfb2ac6f3a1',
        'MED-CEN',
        'Centro logistico NovaCore Medellin',
        'CO',
        'ACTIVE',
        NOW(),
        NOW()
    ),
    (
        '96c99bf8-7de7-4141-96fc-2d788b771c86',
        '6f6d04b6-4c9d-4b6a-8e13-5b2f9c0a1d77',
        'BOG-NOR',
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
        '8ca61d8f-0fb7-428d-b7a7-9a44d20f978a',
        '8b88f0d3-7f1e-4f5e-b2d0-3dfb2ac6f3a1',
        'b0d0d293-3f63-4a43-b0c1-8d9c2ac9ce0b',
        'ARK-LGT-M185-GRY',
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
        '666041e9-d9d1-46aa-a6b8-e7c0b01e3ae1',
        '8b88f0d3-7f1e-4f5e-b2d0-3dfb2ac6f3a1',
        'b0d0d293-3f63-4a43-b0c1-8d9c2ac9ce0b',
        'ARK-TPL-UE300',
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
        'cb35045a-8f6e-4c3e-a1d5-2e3217a43b9c',
        '6f6d04b6-4c9d-4b6a-8e13-5b2f9c0a1d77',
        '96c99bf8-7de7-4141-96fc-2d788b771c86',
        'ARK-RDG-K552-RGB',
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
        '464ffdb0-70e2-4f8d-898f-a3ba0d78d597',
        '6f6d04b6-4c9d-4b6a-8e13-5b2f9c0a1d77',
        '96c99bf8-7de7-4141-96fc-2d788b771c86',
        'ARK-KNG-A400-480',
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
        '1243c2fa-6f02-4baf-98e5-c98f7cc90c43',
        '8b88f0d3-7f1e-4f5e-b2d0-3dfb2ac6f3a1',
        '8ca61d8f-0fb7-428d-b7a7-9a44d20f978a',
        'b0d0d293-3f63-4a43-b0c1-8d9c2ac9ce0b',
        'ARK-LGT-M185-GRY',
        '43d5ec97-89e3-46a2-a5aa-df69013ef8e0',
        '67fc526d-b87e-42c9-a420-163243833fcf',
        2,
        'CONFIRMED',
        NOW() + INTERVAL '7 day',
        NOW() - INTERVAL '1 minute',
        NULL,
        NOW(),
        NOW()
    ),
    (
        'b14c1f83-a18d-4934-8135-b8530f0240f5',
        '6f6d04b6-4c9d-4b6a-8e13-5b2f9c0a1d77',
        'cb35045a-8f6e-4c3e-a1d5-2e3217a43b9c',
        '96c99bf8-7de7-4141-96fc-2d788b771c86',
        'ARK-RDG-K552-RGB',
        'bc4d3ef1-17d0-4e4b-9f41-5269936f8322',
        'c588f9cf-08a1-4d5c-88db-49c02019a03e',
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
