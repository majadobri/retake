CREATE SEQUENCE IF NOT EXISTS customer_seq START 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS address_seq START 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS product_seq START 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS order_gen START 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS order_line_gen START 1 INCREMENT BY 1;



CREATE TABLE customer (
                          id BIGINT PRIMARY KEY,
                          first_name VARCHAR(100) NOT NULL,
                          last_name VARCHAR(100) NOT NULL,
                          email VARCHAR(255) NOT NULL UNIQUE,
                          phone BIGINT NOT NULL
);

CREATE TABLE address (
                         id BIGINT PRIMARY KEY,
                         customer_id BIGINT NOT NULL,
                         street VARCHAR(255) NOT NULL,
                         city VARCHAR(100) NOT NULL,
                         postal_code BIGINT NOT NULL,
                         country VARCHAR(100) NOT NULL,
                         FOREIGN KEY (customer_id) REFERENCES customer(id) ON DELETE CASCADE
);

CREATE TABLE product (
                         id BIGINT PRIMARY KEY,
                         product_name VARCHAR(255) NOT NULL UNIQUE,
                         description TEXT,
                         price DECIMAL(10, 2) NOT NULL,
                         quantity INT NOT NULL DEFAULT 0,
                         status VARCHAR(50) NOT NULL DEFAULT 'IN_STOCK'
);

CREATE TABLE orders (
                        id BIGINT PRIMARY KEY,
                        customer_id BIGINT NOT NULL,
                        shipping_address_id BIGINT NOT NULL,
                        order_date TIMESTAMP NOT NULL,
                        order_status VARCHAR(50) NOT NULL,
                        order_type VARCHAR(50) NOT NULL,
                        shipped BOOLEAN DEFAULT FALSE,
                        shipping_charge DECIMAL(10, 2),
                        total_price DECIMAL(10, 2),
                        FOREIGN KEY (customer_id) REFERENCES customer(id) ON DELETE CASCADE,
                        FOREIGN KEY (shipping_address_id) REFERENCES address(id)
);

CREATE TABLE order_lines (
                             id BIGINT PRIMARY KEY,
                             order_id BIGINT NOT NULL,
                             product_id BIGINT NOT NULL,
                             quantity INT NOT NULL,
                             price_at_purchase DECIMAL(10, 2) NOT NULL,
                             FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
                             FOREIGN KEY (product_id) REFERENCES product(id)
);

CREATE INDEX idx_customer_email ON customer(email);
CREATE INDEX idx_address_customer_id ON address(customer_id);
CREATE INDEX idx_order_customer_id ON orders(customer_id);
CREATE INDEX idx_order_shipped ON orders(shipped);
CREATE INDEX idx_order_line_order_id ON order_lines(order_id);
CREATE INDEX idx_order_line_product_id ON order_lines(product_id);

