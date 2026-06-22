ALTER TABLE email_revert_token
    ADD created_at TIMESTAMP WITHOUT TIME ZONE;

ALTER TABLE email_revert_token
    ADD created_by UUID;

ALTER TABLE email_revert_token
    ADD modified_by UUID;

ALTER TABLE email_revert_token
    ADD updated_at TIMESTAMP WITHOUT TIME ZONE;

ALTER TABLE email_revert_token
    ALTER COLUMN created_at SET NOT NULL;

ALTER TABLE rule_preset
    ADD minimum_tokens_match INTEGER default 20;

ALTER TABLE rule_preset
    ADD normalization BOOLEAN default false;

ALTER TABLE rule_preset
    ALTER COLUMN minimum_tokens_match SET NOT NULL;

ALTER TABLE rule_preset
    ALTER COLUMN normalization SET NOT NULL;

ALTER TABLE subject
    ADD organization_name VARCHAR(64) default 'PAS';

ALTER TABLE subject
    ALTER COLUMN organization_name SET NOT NULL;

ALTER TABLE report
    ADD tag VARCHAR(16) default 'v1';

ALTER TABLE report
    ALTER COLUMN tag SET NOT NULL;

ALTER TABLE email_revert_token
    ADD CONSTRAINT FK_EMAIL_REVERT_TOKEN_ON_CREATED_BY FOREIGN KEY (created_by) REFERENCES account (id);

ALTER TABLE email_revert_token
    ADD CONSTRAINT FK_EMAIL_REVERT_TOKEN_ON_MODIFIED_BY FOREIGN KEY (modified_by) REFERENCES account (id);

ALTER TABLE email_revert_token
    ALTER COLUMN old_email TYPE VARCHAR(255) USING (old_email::VARCHAR(255));

ALTER TABLE email_revert_token
    ALTER COLUMN username TYPE VARCHAR(255) USING (username::VARCHAR(255));

ALTER TABLE email_revert_token
    DROP COLUMN version;

ALTER TABLE email_revert_token
    ADD version INTEGER NOT NULL default 0;