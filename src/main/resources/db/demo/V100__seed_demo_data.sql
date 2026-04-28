-- Demo seed data for Warehouse Flow Manager.
-- This file is only loaded when the "demo" Spring profile is active.
-- It is intended for portfolio screenshots, reviewer walkthroughs, and local demos.

-- This gives:
-- 8 products
-- 3 active storage locations
-- 1 inactive storage location
-- 2 clearly low-stock products
-- 1 zero-stock critical product
-- active, blocked, and discontinued products
-- inbound, outbound, and adjustment movements
-- realistic replenishment candidates

INSERT INTO storage_locations (code, zone, description, active)
VALUES
    ('A-01-01', 'ZONE-A', 'Fast-pick rack near packing area', true),
    ('A-02-03', 'ZONE-A', 'Standard reserve rack for small parts', true),
    ('B-01-02', 'ZONE-B', 'Bulk storage for packaging material', true),
    ('Q-99-01', 'QUARANTINE', 'Inactive quarantine location for blocked stock', false);

INSERT INTO products (
    sku,
    name,
    description,
    unit,
    quantity,
    minimum_quantity,
    status,
    storage_location_id
)
VALUES
    (
        'SKU-1001',
        'Industrial Storage Bin',
        'Large plastic bin for spare parts and picking operations',
        'piece',
        42,
        20,
        'ACTIVE',
        (SELECT id FROM storage_locations WHERE code = 'A-01-01')
    ),
    (
        'SKU-1002',
        'Pick Cart Wheel',
        'Replacement wheel for warehouse picking carts',
        'piece',
        3,
        12,
        'ACTIVE',
        (SELECT id FROM storage_locations WHERE code = 'A-01-01')
    ),
    (
        'SKU-1003',
        'Conveyor Belt Sensor',
        'Photoelectric sensor used in conveyor belt stations',
        'piece',
        0,
        8,
        'ACTIVE',
        (SELECT id FROM storage_locations WHERE code = 'A-02-03')
    ),
    (
        'SKU-1004',
        'Barcode Scanner Battery',
        'Rechargeable battery pack for handheld scanners',
        'piece',
        6,
        10,
        'ACTIVE',
        (SELECT id FROM storage_locations WHERE code = 'A-02-03')
    ),
    (
        'SKU-1005',
        'Packing Tape Roll',
        'Standard packaging tape for outbound shipments',
        'roll',
        120,
        40,
        'ACTIVE',
        (SELECT id FROM storage_locations WHERE code = 'B-01-02')
    ),
    (
        'SKU-1006',
        'Returned Pallet Label',
        'Blocked label stock waiting for quality inspection',
        'piece',
        0,
        0,
        'BLOCKED',
        (SELECT id FROM storage_locations WHERE code = 'A-02-03')
    ),
    (
        'SKU-1007',
        'Legacy Pick Light Module',
        'Discontinued pick-to-light module from old warehouse line',
        'piece',
        0,
        0,
        'DISCONTINUED',
        (SELECT id FROM storage_locations WHERE code = 'A-02-03')
    ),
    (
        'SKU-1008',
        'Safety Gloves',
        'Warehouse safety gloves for picking and packing staff',
        'pair',
        75,
        50,
        'ACTIVE',
        (SELECT id FROM storage_locations WHERE code = 'B-01-02')
    );

INSERT INTO stock_movements (
    product_id,
    movement_type,
    quantity,
    resulting_quantity,
    note,
    movement_at
)
VALUES
    (
        (SELECT id FROM products WHERE sku = 'SKU-1001'),
        'INBOUND',
        50,
        50,
        'Initial supplier delivery',
        NOW() - INTERVAL '14 days'
    ),
    (
        (SELECT id FROM products WHERE sku = 'SKU-1001'),
        'OUTBOUND',
        8,
        42,
        'Picking order for assembly area',
        NOW() - INTERVAL '3 days'
    ),

    (
        (SELECT id FROM products WHERE sku = 'SKU-1002'),
        'INBOUND',
        20,
        20,
        'Initial replenishment delivery',
        NOW() - INTERVAL '20 days'
    ),
    (
        (SELECT id FROM products WHERE sku = 'SKU-1002'),
        'OUTBOUND',
        10,
        10,
        'Maintenance request for picking carts',
        NOW() - INTERVAL '6 days'
    ),
    (
        (SELECT id FROM products WHERE sku = 'SKU-1002'),
        'OUTBOUND',
        7,
        3,
        'Urgent spare-part issue',
        NOW() - INTERVAL '1 day'
    ),

    (
        (SELECT id FROM products WHERE sku = 'SKU-1003'),
        'INBOUND',
        15,
        15,
        'Sensor delivery for conveyor maintenance',
        NOW() - INTERVAL '18 days'
    ),
    (
        (SELECT id FROM products WHERE sku = 'SKU-1003'),
        'OUTBOUND',
        12,
        3,
        'Conveyor repair work order',
        NOW() - INTERVAL '4 days'
    ),
    (
        (SELECT id FROM products WHERE sku = 'SKU-1003'),
        'ADJUSTMENT',
        0,
        0,
        'Inventory count correction after cycle count',
        NOW() - INTERVAL '2 days'
    ),

    (
        (SELECT id FROM products WHERE sku = 'SKU-1004'),
        'INBOUND',
        20,
        20,
        'Scanner battery restock',
        NOW() - INTERVAL '25 days'
    ),
    (
        (SELECT id FROM products WHERE sku = 'SKU-1004'),
        'OUTBOUND',
        14,
        6,
        'Scanner fleet replacement',
        NOW() - INTERVAL '5 days'
    ),

    (
        (SELECT id FROM products WHERE sku = 'SKU-1005'),
        'INBOUND',
        160,
        160,
        'Bulk packaging material delivery',
        NOW() - INTERVAL '16 days'
    ),
    (
        (SELECT id FROM products WHERE sku = 'SKU-1005'),
        'OUTBOUND',
        40,
        120,
        'Outbound packing consumption',
        NOW() - INTERVAL '7 days'
    ),

    (
        (SELECT id FROM products WHERE sku = 'SKU-1008'),
        'INBOUND',
        90,
        90,
        'Safety stock delivery',
        NOW() - INTERVAL '12 days'
    ),
    (
        (SELECT id FROM products WHERE sku = 'SKU-1008'),
        'OUTBOUND',
        15,
        75,
        'Team equipment issue',
        NOW() - INTERVAL '2 days'
    );