CREATE TABLE user_sessions (
    id                 UUID                     NOT NULL PRIMARY KEY,
    user_account_id    UUID                     NOT NULL,
    session_identifier VARCHAR(255)             NOT NULL,
    issued_at          TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expiry_timestamp   TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT uq_user_sessions_session_identifier UNIQUE (session_identifier),
    CONSTRAINT fk_user_sessions_user_account FOREIGN KEY (user_account_id) REFERENCES user_accounts (id)
);

CREATE INDEX idx_user_sessions_user_account_id ON user_sessions (user_account_id);
