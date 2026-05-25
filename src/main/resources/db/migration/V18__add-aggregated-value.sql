ALTER TABLE subject
    ADD aggregated_average_similarity DOUBLE PRECISION;

ALTER TABLE report
    ADD average_similarity DOUBLE PRECISION;

ALTER TABLE report
    ALTER COLUMN average_similarity SET NOT NULL;