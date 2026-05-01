CREATE TABLE notifications (
                               id BIGSERIAL PRIMARY KEY,
                               user_id BIGINT NOT NULL,
                               order_id BIGINT NOT NULL,
                               conversation_id VARCHAR(100) NOT NULL,
                               type VARCHAR(50) NOT NULL,
                               status VARCHAR(30) NOT NULL,
                               message VARCHAR(500) NOT NULL,
                               created_at TIMESTAMP NOT NULL,
                               CONSTRAINT uk_notifications_conversation_type UNIQUE (conversation_id, type)
);

CREATE INDEX idx_notifications_user_id ON notifications(user_id);
CREATE INDEX idx_notifications_order_id ON notifications(order_id);
CREATE INDEX idx_notifications_type ON notifications(type);
CREATE INDEX idx_notifications_created_at ON notifications(created_at);
