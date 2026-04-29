CREATE TABLE IF NOT EXISTS user_schema.refresh_tokens (
                                                          id BIGSERIAL PRIMARY KEY,
                                                          token VARCHAR(500) NOT NULL,
    user_id BIGINT NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    revoked BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_refresh_tokens_token UNIQUE (token),
    CONSTRAINT fk_refresh_tokens_user FOREIGN KEY (user_id)
    REFERENCES user_schema.users (id)
    ON DELETE CASCADE
    );
