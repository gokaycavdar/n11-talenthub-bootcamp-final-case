CREATE TABLE products (
                          id BIGSERIAL PRIMARY KEY,
                          name VARCHAR(150) NOT NULL,
                          description VARCHAR(1000) NOT NULL,
                          category VARCHAR(100) NOT NULL,
                          price NUMERIC(12, 2) NOT NULL,
                          stock INTEGER NOT NULL,
                          image_url VARCHAR(500),
                          active BOOLEAN NOT NULL DEFAULT TRUE,
                          created_at TIMESTAMP NOT NULL,
                          updated_at TIMESTAMP NOT NULL
);

CREATE INDEX idx_products_active ON products(active);
CREATE INDEX idx_products_category ON products(category);
