CREATE TABLE roles (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(50) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_roles_name UNIQUE (name)
) ENGINE=InnoDB;

CREATE TABLE users (
    id BIGINT NOT NULL AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL,
    email VARCHAR(100) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role_id BIGINT NULL,
    created_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_users_username UNIQUE (username),
    CONSTRAINT uk_users_email UNIQUE (email),
    CONSTRAINT fk_users_role FOREIGN KEY (role_id) REFERENCES roles (id)
) ENGINE=InnoDB;

CREATE TABLE refresh_tokens (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    token_hash VARCHAR(64) NOT NULL,
    expires_at DATETIME(6) NOT NULL,
    revoked_at DATETIME(6) NULL,
    created_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_refresh_tokens_hash UNIQUE (token_hash),
    INDEX ix_refresh_tokens_user_revoked (user_id, revoked_at),
    CONSTRAINT fk_refresh_tokens_user FOREIGN KEY (user_id) REFERENCES users (id)
) ENGINE=InnoDB;

CREATE TABLE categories (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    description TEXT NULL,
    created_at DATETIME(6) NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_categories_name UNIQUE (name)
) ENGINE=InnoDB;

CREATE TABLE brands (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    description TEXT NULL,
    created_at DATETIME(6) NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_brands_name UNIQUE (name)
) ENGINE=InnoDB;

CREATE TABLE suppliers (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(150) NOT NULL,
    contact_person VARCHAR(100) NULL,
    email VARCHAR(100) NULL,
    phone VARCHAR(30) NULL,
    address TEXT NULL,
    created_at DATETIME(6) NULL,
    PRIMARY KEY (id)
) ENGINE=InnoDB;

CREATE TABLE customers (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(150) NOT NULL,
    email VARCHAR(100) NULL,
    phone VARCHAR(30) NULL,
    address TEXT NULL,
    created_at DATETIME(6) NULL,
    PRIMARY KEY (id)
) ENGINE=InnoDB;

CREATE TABLE warehouses (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    location VARCHAR(255) NOT NULL,
    created_at DATETIME(6) NULL,
    PRIMARY KEY (id)
) ENGINE=InnoDB;

CREATE TABLE products (
    id BIGINT NOT NULL AUTO_INCREMENT,
    sku VARCHAR(50) NOT NULL,
    name VARCHAR(100) NOT NULL,
    description TEXT NULL,
    category_id BIGINT NOT NULL,
    brand_id BIGINT NOT NULL,
    base_price DECIMAL(10,2) NULL,
    created_at DATETIME(6) NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_products_sku UNIQUE (sku),
    INDEX ix_products_category (category_id),
    INDEX ix_products_brand (brand_id),
    CONSTRAINT fk_products_category FOREIGN KEY (category_id) REFERENCES categories (id),
    CONSTRAINT fk_products_brand FOREIGN KEY (brand_id) REFERENCES brands (id)
) ENGINE=InnoDB;

CREATE TABLE inventory (
    id BIGINT NOT NULL AUTO_INCREMENT,
    product_id BIGINT NOT NULL,
    warehouse_id BIGINT NOT NULL,
    quantity INT NOT NULL DEFAULT 0,
    min_threshold INT NULL DEFAULT 0,
    max_capacity INT NULL,
    updated_at DATETIME(6) NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_inventory_product_warehouse UNIQUE (product_id, warehouse_id),
    CONSTRAINT fk_inventory_product FOREIGN KEY (product_id) REFERENCES products (id),
    CONSTRAINT fk_inventory_warehouse FOREIGN KEY (warehouse_id) REFERENCES warehouses (id)
) ENGINE=InnoDB;

CREATE TABLE stock_movements (
    id BIGINT NOT NULL AUTO_INCREMENT,
    product_id BIGINT NOT NULL,
    warehouse_id BIGINT NOT NULL,
    destination_warehouse_id BIGINT NULL,
    type VARCHAR(20) NOT NULL,
    quantity INT NOT NULL,
    reference_number VARCHAR(50) NULL,
    performed_by BIGINT NOT NULL,
    notes TEXT NULL,
    created_at DATETIME(6) NULL,
    PRIMARY KEY (id),
    INDEX ix_stock_movements_product_created (product_id, created_at),
    INDEX ix_stock_movements_warehouse_created (warehouse_id, created_at),
    CONSTRAINT fk_stock_movements_product FOREIGN KEY (product_id) REFERENCES products (id),
    CONSTRAINT fk_stock_movements_warehouse FOREIGN KEY (warehouse_id) REFERENCES warehouses (id),
    CONSTRAINT fk_stock_movements_destination FOREIGN KEY (destination_warehouse_id) REFERENCES warehouses (id),
    CONSTRAINT fk_stock_movements_user FOREIGN KEY (performed_by) REFERENCES users (id)
) ENGINE=InnoDB;

CREATE TABLE purchase_orders (
    id BIGINT NOT NULL AUTO_INCREMENT,
    supplier_id BIGINT NOT NULL,
    order_date DATETIME(6) NOT NULL,
    status VARCHAR(20) NOT NULL,
    reference_number VARCHAR(50) NULL,
    notes TEXT NULL,
    created_at DATETIME(6) NULL,
    PRIMARY KEY (id),
    INDEX ix_purchase_orders_supplier (supplier_id),
    CONSTRAINT fk_purchase_orders_supplier FOREIGN KEY (supplier_id) REFERENCES suppliers (id)
) ENGINE=InnoDB;

CREATE TABLE purchase_order_items (
    id BIGINT NOT NULL AUTO_INCREMENT,
    purchase_order_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    quantity INT NOT NULL,
    unit_price DECIMAL(10,2) NULL,
    PRIMARY KEY (id),
    INDEX ix_purchase_order_items_order (purchase_order_id),
    INDEX ix_purchase_order_items_product (product_id),
    CONSTRAINT fk_purchase_order_items_order FOREIGN KEY (purchase_order_id) REFERENCES purchase_orders (id),
    CONSTRAINT fk_purchase_order_items_product FOREIGN KEY (product_id) REFERENCES products (id)
) ENGINE=InnoDB;

CREATE TABLE sale_orders (
    id BIGINT NOT NULL AUTO_INCREMENT,
    customer_id BIGINT NOT NULL,
    order_date DATETIME(6) NOT NULL,
    status VARCHAR(20) NOT NULL,
    reference_number VARCHAR(50) NULL,
    notes TEXT NULL,
    created_at DATETIME(6) NULL,
    PRIMARY KEY (id),
    INDEX ix_sale_orders_customer (customer_id),
    CONSTRAINT fk_sale_orders_customer FOREIGN KEY (customer_id) REFERENCES customers (id)
) ENGINE=InnoDB;

CREATE TABLE sale_order_items (
    id BIGINT NOT NULL AUTO_INCREMENT,
    sale_order_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    quantity INT NOT NULL,
    unit_price DECIMAL(10,2) NULL,
    PRIMARY KEY (id),
    INDEX ix_sale_order_items_order (sale_order_id),
    INDEX ix_sale_order_items_product (product_id),
    CONSTRAINT fk_sale_order_items_order FOREIGN KEY (sale_order_id) REFERENCES sale_orders (id),
    CONSTRAINT fk_sale_order_items_product FOREIGN KEY (product_id) REFERENCES products (id)
) ENGINE=InnoDB;

CREATE TABLE transfer_orders (
    id BIGINT NOT NULL AUTO_INCREMENT,
    source_warehouse_id BIGINT NOT NULL,
    destination_warehouse_id BIGINT NOT NULL,
    transfer_date DATETIME(6) NOT NULL,
    status VARCHAR(20) NOT NULL,
    reference_number VARCHAR(50) NULL,
    created_at DATETIME(6) NULL,
    PRIMARY KEY (id),
    INDEX ix_transfer_orders_source (source_warehouse_id),
    INDEX ix_transfer_orders_destination (destination_warehouse_id),
    CONSTRAINT fk_transfer_orders_source FOREIGN KEY (source_warehouse_id) REFERENCES warehouses (id),
    CONSTRAINT fk_transfer_orders_destination FOREIGN KEY (destination_warehouse_id) REFERENCES warehouses (id)
) ENGINE=InnoDB;

CREATE TABLE transfer_order_items (
    id BIGINT NOT NULL AUTO_INCREMENT,
    transfer_order_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    quantity INT NOT NULL,
    PRIMARY KEY (id),
    INDEX ix_transfer_order_items_order (transfer_order_id),
    INDEX ix_transfer_order_items_product (product_id),
    CONSTRAINT fk_transfer_order_items_order FOREIGN KEY (transfer_order_id) REFERENCES transfer_orders (id),
    CONSTRAINT fk_transfer_order_items_product FOREIGN KEY (product_id) REFERENCES products (id)
) ENGINE=InnoDB;

CREATE TABLE notifications (
    id BIGINT NOT NULL AUTO_INCREMENT,
    title VARCHAR(255) NOT NULL,
    message TEXT NOT NULL,
    type VARCHAR(255) NOT NULL,
    user_id BIGINT NULL,
    is_read BIT(1) NOT NULL DEFAULT b'0',
    created_at DATETIME(6) NULL,
    PRIMARY KEY (id),
    INDEX ix_notifications_user_read_created (user_id, is_read, created_at)
) ENGINE=InnoDB;

CREATE TABLE audit_logs (
    id BIGINT NOT NULL AUTO_INCREMENT,
    action VARCHAR(255) NOT NULL,
    entity_name VARCHAR(255) NOT NULL,
    entity_id VARCHAR(255) NOT NULL,
    user_id BIGINT NULL,
    details TEXT NULL,
    created_at DATETIME(6) NULL,
    PRIMARY KEY (id),
    INDEX ix_audit_logs_user_created (user_id, created_at)
) ENGINE=InnoDB;

INSERT INTO roles (name) VALUES ('ADMIN'), ('MANAGER'), ('STAFF');
