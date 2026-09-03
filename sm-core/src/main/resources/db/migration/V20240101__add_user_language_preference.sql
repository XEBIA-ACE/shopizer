-- ---------------------------------------------------------------------------
-- Migration: add USER_LANGUAGE_PREFERENCE table
--
-- This script is ADDITIVE — no existing tables or columns are modified.
-- Safe to run against Shopizer 3.2.5 databases.
--
-- Naming follows the existing Shopizer convention (UPPER_SNAKE_CASE tables,
-- BIGINT surrogate PKs, FK columns named <ENTITY>_ID).
-- ---------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS USER_LANGUAGE_PREFERENCE (
    ID          BIGINT          NOT NULL AUTO_INCREMENT,
    CUSTOMER_ID BIGINT          NOT NULL,
    LANGUAGE_TAG VARCHAR(20)    NOT NULL DEFAULT 'en',
    LOCALE       VARCHAR(20)    NOT NULL DEFAULT 'en-US',
    CONSTRAINT PK_LANG_PREF         PRIMARY KEY (ID),
    CONSTRAINT UQ_LANG_PREF_CUSTOMER UNIQUE (CUSTOMER_ID)
);

-- Optional: add a FK to the CUSTOMER table if it exists in the target schema.
-- Commented out by default so the migration can run in isolation during tests.
--
-- ALTER TABLE USER_LANGUAGE_PREFERENCE
--     ADD CONSTRAINT FK_LANG_PREF_CUSTOMER
--     FOREIGN KEY (CUSTOMER_ID) REFERENCES CUSTOMER (CUSTOMER_ID)
--     ON DELETE CASCADE;
