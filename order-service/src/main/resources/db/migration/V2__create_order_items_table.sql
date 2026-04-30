CREATE TABLE order_items (
                             id BIGSERIAL PRIMARY KEY,
                             order_id BIGINT NOT NULL,
                             product_id BIGINT NOT NULL,
                             product_name VARCHAR(150) NOT NULL,
                             unit_price NUMERIC(12, 2) NOT NULL,
                             quantity INTEGER NOT NULL,
                             line_total NUMERIC(12, 2) NOT NULL,
                             CONSTRAINT fk_order_items_order
                                 FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE
);

CREATE INDEX idx_order_items_order_id ON order_items(order_id);
