-- V4__Add_Order_Snapshot_And_Idempotency.sql

-- Add historical snapshot columns to order_items
ALTER TABLE order_items
    ADD COLUMN IF NOT EXISTS product_name VARCHAR(255),
    ADD COLUMN IF NOT EXISTS sku          VARCHAR(100),
    ADD COLUMN IF NOT EXISTS variant_name VARCHAR(255),
    ADD COLUMN IF NOT EXISTS image_url    TEXT;

-- Make product_id and variant_id nullable (product may be deleted but history must be preserved)
ALTER TABLE order_items
    ALTER COLUMN product_id DROP NOT NULL,
    ALTER COLUMN variant_id DROP NOT NULL;

-- Add idempotency_key to orders for duplicate request protection
ALTER TABLE orders
    ADD COLUMN IF NOT EXISTS idempotency_key VARCHAR(100) UNIQUE;
