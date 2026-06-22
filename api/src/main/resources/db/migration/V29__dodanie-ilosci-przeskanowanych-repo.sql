ALTER TABLE report
    ADD scanned_repositories BIGINT default 0;

ALTER TABLE report
    ALTER COLUMN scanned_repositories SET NOT NULL;
