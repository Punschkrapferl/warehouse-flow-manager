ALTER TABLE products
    ALTER COLUMN storage_location_id DROP NOT NULL;

CREATE INDEX IF NOT EXISTS idx_products_storage_location_id
    ON products (storage_location_id);

CREATE INDEX IF NOT EXISTS idx_stock_movements_product_id
    ON stock_movements (product_id);

CREATE INDEX IF NOT EXISTS idx_stock_movements_movement_type
    ON stock_movements (movement_type);

CREATE INDEX IF NOT EXISTS idx_stock_movements_movement_at
    ON stock_movements (movement_at);

CREATE INDEX IF NOT EXISTS idx_stock_movements_product_id_movement_at
    ON stock_movements (product_id, movement_at DESC);