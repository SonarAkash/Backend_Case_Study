-- 1. COMPANIES: Top level entity
CREATE TABLE companies (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 2. WAREHOUSES: A company can have multiple warehouses
CREATE TABLE warehouses (
    id SERIAL PRIMARY KEY,
    company_id INT REFERENCES companies(id),
    name VARCHAR(255) NOT NULL,
    location VARCHAR(255),
    CONSTRAINT fk_company FOREIGN KEY (company_id) REFERENCES companies(id)
);

-- 3. SUPPLIERS: External entities providing products
CREATE TABLE suppliers (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    contact_email VARCHAR(255),
    phone VARCHAR(50)
);

-- 4. PRODUCTS: The items being sold
CREATE TABLE products (
    id SERIAL PRIMARY KEY,
    company_id INT REFERENCES companies(id),
    sku VARCHAR(50) NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    price DECIMAL(10, 2) NOT NULL, -- DECIMAL is better for currency than FLOAT
    reorder_level INT DEFAULT 10, -- Threshold for low stock alerts
    supplier_id INT REFERENCES suppliers(id), -- Main supplier for reordering
    is_bundle BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(company_id, sku) -- SKUs must be unique within a company
);

-- 5. PRODUCT_BUNDLES: Handling the "Bundle" requirement
-- Associates a 'parent' bundle product with 'child' component products
CREATE TABLE product_bundles (
    bundle_id INT REFERENCES products(id),
    component_id INT REFERENCES products(id),
    quantity_needed INT NOT NULL, -- How many components needed for 1 bundle
    PRIMARY KEY (bundle_id, component_id)
);

-- 6. INVENTORY: Tracks stock of a product in a specific warehouse
CREATE TABLE inventory (
    id SERIAL PRIMARY KEY,
    product_id INT REFERENCES products(id),
    warehouse_id INT REFERENCES warehouses(id),
    quantity INT NOT NULL DEFAULT 0,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT quantity_non_negative CHECK (quantity >= 0),
    UNIQUE(product_id, warehouse_id) -- One record per product-warehouse pair
);

-- 7. INVENTORY_HISTORY: Tracks *when* and *why* inventory changed
CREATE TABLE inventory_history (
    id SERIAL PRIMARY KEY,
    inventory_id INT REFERENCES inventory(id),
    change_amount INT NOT NULL, -- Can be positive (restock) or negative (sale)
    reason VARCHAR(50), -- e.g., 'SALE', 'RESTOCK', 'DAMAGE'
    changed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
