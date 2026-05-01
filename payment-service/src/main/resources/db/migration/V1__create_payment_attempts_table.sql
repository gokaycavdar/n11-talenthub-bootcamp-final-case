CREATE TABLE payment_attempts (
                                  id BIGSERIAL PRIMARY KEY,
                                  order_id BIGINT NOT NULL,
                                  user_id BIGINT NOT NULL,
                                  conversation_id VARCHAR(100) NOT NULL UNIQUE,
                                  provider VARCHAR(30) NOT NULL,
                                  status VARCHAR(30) NOT NULL,
                                  paid_price NUMERIC(12, 2) NOT NULL,
                                  external_payment_id VARCHAR(100),
                                  failure_reason VARCHAR(500),
                                  created_at TIMESTAMP NOT NULL,
                                  updated_at TIMESTAMP NOT NULL
);

CREATE INDEX idx_payment_attempts_order_id ON payment_attempts(order_id);
CREATE INDEX idx_payment_attempts_user_id ON payment_attempts(user_id);
CREATE INDEX idx_payment_attempts_status ON payment_attempts(status);
