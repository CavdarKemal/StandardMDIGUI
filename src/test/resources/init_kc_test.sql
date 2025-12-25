-- ============================================================
-- PostgreSQL Database Setup Script
-- Database: KC_TEST
-- Author: StandardMDIGUI
-- Created: 2024-12-24
-- ============================================================

-- Hinweis: Zuerst als postgres-User verbinden und Datenbank erstellen:
-- CREATE DATABASE kc_test;

-- Dann mit der kc_test Datenbank verbinden und dieses Script ausführen

-- ============================================================
-- Drop existing tables (in correct order due to foreign keys)
-- ============================================================
DROP TABLE IF EXISTS orders CASCADE;
DROP TABLE IF EXISTS address CASCADE;
DROP TABLE IF EXISTS customer CASCADE;

-- ============================================================
-- Table: customer
-- ============================================================
CREATE TABLE customer (
    id              SERIAL PRIMARY KEY,
    customer_number VARCHAR(20) NOT NULL UNIQUE,
    first_name      VARCHAR(100) NOT NULL,
    last_name       VARCHAR(100) NOT NULL,
    email           VARCHAR(255) UNIQUE,
    phone           VARCHAR(50),
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    active          BOOLEAN DEFAULT TRUE
);

COMMENT ON TABLE customer IS 'Kundenstammdaten';
COMMENT ON COLUMN customer.customer_number IS 'Eindeutige Kundennummer';

-- ============================================================
-- Table: address
-- ============================================================
CREATE TABLE address (
    id              SERIAL PRIMARY KEY,
    customer_id     INTEGER NOT NULL REFERENCES customer(id) ON DELETE CASCADE,
    address_type    VARCHAR(20) DEFAULT 'BILLING' CHECK (address_type IN ('BILLING', 'SHIPPING', 'BOTH')),
    street          VARCHAR(255) NOT NULL,
    house_number    VARCHAR(20),
    postal_code     VARCHAR(20) NOT NULL,
    city            VARCHAR(100) NOT NULL,
    country         VARCHAR(100) DEFAULT 'Deutschland',
    is_default      BOOLEAN DEFAULT FALSE,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE address IS 'Kundenadressen (Rechnung/Lieferung)';
COMMENT ON COLUMN address.address_type IS 'BILLING=Rechnungsadresse, SHIPPING=Lieferadresse, BOTH=Beides';

CREATE INDEX idx_address_customer ON address(customer_id);

-- ============================================================
-- Table: orders
-- ============================================================
CREATE TABLE orders (
    id              SERIAL PRIMARY KEY,
    order_number    VARCHAR(30) NOT NULL UNIQUE,
    customer_id     INTEGER NOT NULL REFERENCES customer(id) ON DELETE RESTRICT,
    shipping_address_id INTEGER REFERENCES address(id),
    billing_address_id  INTEGER REFERENCES address(id),
    order_date      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    status          VARCHAR(20) DEFAULT 'NEW' CHECK (status IN ('NEW', 'CONFIRMED', 'SHIPPED', 'DELIVERED', 'CANCELLED')),
    total_amount    DECIMAL(12, 2) DEFAULT 0.00,
    currency        VARCHAR(3) DEFAULT 'EUR',
    notes           TEXT,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE orders IS 'Kundenbestellungen';
COMMENT ON COLUMN orders.status IS 'NEW=Neu, CONFIRMED=Bestätigt, SHIPPED=Versendet, DELIVERED=Zugestellt, CANCELLED=Storniert';

CREATE INDEX idx_orders_customer ON orders(customer_id);
CREATE INDEX idx_orders_status ON orders(status);
CREATE INDEX idx_orders_date ON orders(order_date);

-- ============================================================
-- Sample Data
-- ============================================================

-- Kunden
INSERT INTO customer (customer_number, first_name, last_name, email, phone) VALUES
    ('KC-001', 'Max', 'Mustermann', 'max.mustermann@example.com', '+49 123 456789'),
    ('KC-002', 'Erika', 'Musterfrau', 'erika.musterfrau@example.com', '+49 987 654321'),
    ('KC-003', 'Hans', 'Schmidt', 'hans.schmidt@example.com', '+49 555 123456'),
    ('KC-004', 'Anna', 'Müller', 'anna.mueller@example.com', NULL),
    ('KC-005', 'Peter', 'Wagner', 'peter.wagner@example.com', '+49 333 999888');

-- Adressen
INSERT INTO address (customer_id, address_type, street, house_number, postal_code, city, is_default) VALUES
    (1, 'BOTH', 'Musterstraße', '123', '12345', 'Berlin', TRUE),
    (2, 'BILLING', 'Hauptstraße', '45', '80331', 'München', TRUE),
    (2, 'SHIPPING', 'Nebenstraße', '7', '80333', 'München', FALSE),
    (3, 'BOTH', 'Bahnhofstraße', '89', '50667', 'Köln', TRUE),
    (4, 'BILLING', 'Gartenweg', '12a', '20095', 'Hamburg', TRUE),
    (5, 'BOTH', 'Industriestraße', '200', '70173', 'Stuttgart', TRUE);

-- Bestellungen
INSERT INTO orders (order_number, customer_id, shipping_address_id, billing_address_id, status, total_amount, notes) VALUES
    ('ORD-2024-0001', 1, 1, 1, 'DELIVERED', 149.99, 'Expressversand gewünscht'),
    ('ORD-2024-0002', 2, 3, 2, 'SHIPPED', 299.50, NULL),
    ('ORD-2024-0003', 1, 1, 1, 'CONFIRMED', 89.00, 'Geschenkverpackung'),
    ('ORD-2024-0004', 3, 4, 4, 'NEW', 450.00, NULL),
    ('ORD-2024-0005', 4, 5, 5, 'CANCELLED', 75.25, 'Vom Kunden storniert'),
    ('ORD-2024-0006', 5, 6, 6, 'CONFIRMED', 1250.00, 'Großbestellung'),
    ('ORD-2024-0007', 2, 3, 2, 'NEW', 35.99, NULL);

-- ============================================================
-- Useful Views
-- ============================================================

CREATE OR REPLACE VIEW v_customer_orders AS
SELECT
    c.customer_number,
    c.first_name || ' ' || c.last_name AS customer_name,
    c.email,
    o.order_number,
    o.order_date,
    o.status,
    o.total_amount,
    o.currency
FROM customer c
JOIN orders o ON c.id = o.customer_id
ORDER BY o.order_date DESC;

CREATE OR REPLACE VIEW v_customer_addresses AS
SELECT
    c.customer_number,
    c.first_name || ' ' || c.last_name AS customer_name,
    a.address_type,
    a.street || ' ' || COALESCE(a.house_number, '') AS street_full,
    a.postal_code || ' ' || a.city AS city_full,
    a.country,
    a.is_default
FROM customer c
JOIN address a ON c.id = a.customer_id
ORDER BY c.customer_number, a.address_type;

-- ============================================================
-- Grant permissions (adjust username as needed)
-- ============================================================
-- GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO your_user;
-- GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO your_user;

SELECT 'Database KC_TEST initialized successfully!' AS status;
