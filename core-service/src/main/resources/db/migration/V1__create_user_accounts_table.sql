CREATE TABLE user_accounts (
    id                     UUID                     NOT NULL PRIMARY KEY,
    email_address          VARCHAR(320)             NOT NULL,
    password_hash          VARCHAR(255)             NOT NULL,
    account_status         VARCHAR(16)              NOT NULL DEFAULT 'pending',
    email_verified         BOOLEAN                  NOT NULL DEFAULT FALSE,
    registration_timestamp TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    resend_count           SMALLINT                 NOT NULL DEFAULT 0,
    last_resend_at         TIMESTAMP WITH TIME ZONE,
    CONSTRAINT uq_user_accounts_email_address UNIQUE (email_address),
    CONSTRAINT ck_user_accounts_account_status CHECK (account_status IN ('pending', 'active', 'suspended'))
);

CREATE INDEX idx_user_accounts_account_status ON user_accounts (account_status);
