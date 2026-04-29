ALTER TABLE account
    ADD token_version INTEGER;
UPDATE account SET token_version = 1 WHERE token_version IS NULL;
ALTER TABLE account
    ALTER COLUMN token_version SET NOT NULL;