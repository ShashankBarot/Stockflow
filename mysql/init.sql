-- Stock Management System - Database Initialization
-- This script runs automatically when MySQL container starts for the first time.

-- Create roles
INSERT INTO roles (name) VALUES ('ADMIN'), ('MANAGER'), ('STAFF'), ('VIEWER');

-- Create default admin user
-- Password: admin123 (BCrypt encoded)
INSERT INTO users (username, email, password_hash, role_id, created_at)
VALUES (
    'admin',
    'admin@stockmanagement.com',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
    1,
    NOW()
);

-- Create sample warehouses
INSERT INTO warehouses (name, location, created_at) VALUES
    ('Main Warehouse', '123 Industrial Blvd, City A', NOW()),
    ('Distribution Center', '456 Logistics Ave, City B', NOW());

-- Create sample products
INSERT INTO products (sku, name, description, category, base_price, created_at) VALUES
    ('SKU-001', 'Widget A', 'Standard widget type A', 'Widgets', 29.99, NOW()),
    ('SKU-002', 'Widget B', 'Premium widget type B', 'Widgets', 49.99, NOW()),
    ('SKU-003', 'Gadget X', 'Multi-purpose gadget', 'Gadgets', 99.99, NOW());

-- Create sample inventory
INSERT INTO inventory (product_id, warehouse_id, quantity, min_threshold, max_capacity, updated_at) VALUES
    (1, 1, 500, 50, 1000, NOW()),
    (2, 1, 250, 30, 500, NOW()),
    (3, 2, 100, 20, 300, NOW());
