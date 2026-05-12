CREATE TABLE password_history (
                                  version integer NOT NULL,
                                  created_at timestamp(6) NOT NULL,
                                  updated_at timestamp(6),
                                  id uuid NOT NULL,
                                  created_by uuid,
                                  modified_by uuid,
                                  account_id uuid NOT NULL,
                                  password_hash varchar(64) NOT NULL,
                                  PRIMARY KEY (id)
);

CREATE INDEX idx_password_history_account_id ON password_history (account_id);
CREATE INDEX idx_password_history_created_by ON password_history (created_by);
CREATE INDEX idx_password_history_modified_by ON password_history (modified_by);

ALTER TABLE IF EXISTS password_history ADD CONSTRAINT fk_password_history_account_id FOREIGN KEY (account_id) REFERENCES account;
ALTER TABLE IF EXISTS password_history ADD CONSTRAINT fk_password_history_created_by FOREIGN KEY (created_by) REFERENCES account;
ALTER TABLE IF EXISTS password_history ADD CONSTRAINT fk_password_history_modified_by FOREIGN KEY (modified_by) REFERENCES account;

INSERT INTO password_history (id, version, created_at, updated_at, created_by, modified_by, account_id, password_hash)
SELECT gen_random_uuid(), 0, created_at, updated_at, created_by, modified_by, id, password_hash FROM account;

GRANT SELECT, INSERT, UPDATE, DELETE ON password_history TO ssbd01admin;
GRANT SELECT, INSERT, UPDATE, DELETE ON password_history TO ssbd01mok;