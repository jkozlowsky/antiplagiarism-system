ALTER TABLE schedule
    ADD modifiable_until TIMESTAMP WITHOUT TIME ZONE;

UPDATE schedule SET modifiable_until = schedule_date_time;

ALTER TABLE schedule
    ALTER COLUMN modifiable_until SET NOT NULL;