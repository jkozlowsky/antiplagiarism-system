CREATE TABLE one_time_token
(
    id          UUID                        NOT NULL,
    version     INTEGER                     NOT NULL,
    created_at  TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at  TIMESTAMP WITHOUT TIME ZONE,
    created_by  UUID,
    modified_by UUID,
    token_value VARCHAR(255)                NOT NULL,
    username    VARCHAR(50)                 NOT NULL,
    expires_at  TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    is_used     BOOLEAN                     NOT NULL,
    CONSTRAINT pk_one_time_token PRIMARY KEY (id)
);

ALTER TABLE one_time_token
    ADD CONSTRAINT uc_one_time_token_token_value UNIQUE (token_value);

ALTER TABLE one_time_token
    ADD CONSTRAINT FK_ONE_TIME_TOKEN_ON_CREATED_BY FOREIGN KEY (created_by) REFERENCES account (id);

ALTER TABLE one_time_token
    ADD CONSTRAINT FK_ONE_TIME_TOKEN_ON_MODIFIED_BY FOREIGN KEY (modified_by) REFERENCES account (id);
