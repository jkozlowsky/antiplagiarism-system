CREATE TABLE email_revert_token
(
    id          UUID PRIMARY KEY,
    token_value VARCHAR(255) NOT NULL UNIQUE,
    username    VARCHAR(50)  NOT NULL,
    old_email   VARCHAR(100) NOT NULL,
    expires_at  TIMESTAMP    NOT NULL,
    is_used     BOOLEAN      NOT NULL DEFAULT FALSE,
    version     BIGINT       NOT NULL
);

GRANT SELECT, INSERT, UPDATE ON email_revert_token TO ssbd01mok;