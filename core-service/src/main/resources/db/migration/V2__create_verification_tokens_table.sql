CREATE TABLE verification_tokens (
    id               UUID                     NOT NULL PRIMARY KEY,
    user_account_id  UUID                     NOT NULL,
    token_value      VARCHAR(128)             NOT NULL,
    expiry_timestamp TIMESTAMP WITH TIME ZONE NOT NULL,
    consumed         BOOLEAN                  NOT NULL DEFAULT FALSE,
    created_at       TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_verification_tokens_token_value UNIQUE (token_value),
    CONSTRAINT fk_verification_tokens_user_account FOREIGN KEY (user_account_id) REFERENCES user_accounts (id)
);

CREATE INDEX idx_verification_tokens_user_account_id ON verification_tokens (user_account_id);
