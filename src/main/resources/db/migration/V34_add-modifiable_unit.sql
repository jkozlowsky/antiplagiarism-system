ALTER TABLE schedule
    ADD modifiable_until TIMESTAMP WITHOUT TIME ZONE;

ALTER TABLE schedule
    ALTER COLUMN modifiable_until SET NOT NULL;