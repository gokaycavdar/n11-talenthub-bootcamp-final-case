CREATE TABLE orders (
                        id BIGSERIAL PRIMARY KEY,
                        order_number VARCHAR(50) NOT NULL UNIQUE,
                        user_id BIGINT NOT NULL,
                        status VARCHAR(30) NOT NULL,
                        total_amount NUMERIC(12, 2) NOT NULL,
                        payment_conversation_id VARCHAR(100),
                        shipping_full_name VARCHAR(150) NOT NULL,
                        shipping_address_line VARCHAR(255) NOT NULL,
                        city VARCHAR(100) NOT NULL,
                        district VARCHAR(100) NOT NULL,
                        postal_code VARCHAR(20) NOT NULL,
                        created_at TIMESTAMP NOT NULL
);

CREATE INDEX idx_orders_user_id ON orders(user_id);
CREATE INDEX idx_orders_status ON orders(status);
CREATE INDEX idx_orders_created_at ON orders(created_at);
