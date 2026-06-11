CREATE TABLE subject_statistics
(
    id                 UUID                        NOT NULL,
    version            INTEGER                     NOT NULL,
    created_at         TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at         TIMESTAMP WITHOUT TIME ZONE,
    created_by         UUID,
    modified_by        UUID,
    average_similarity DOUBLE PRECISION,
    subject_id         UUID                        NOT NULL,
    CONSTRAINT pk_subject_statistics PRIMARY KEY (id)
);

ALTER TABLE subject_statistics
    ADD CONSTRAINT uc_subject_statistics_subject UNIQUE (subject_id);

ALTER TABLE subject_statistics
    ADD CONSTRAINT FK_SUBJECT_STATISTICS_ON_CREATED_BY FOREIGN KEY (created_by) REFERENCES account (id);

CREATE INDEX idx_subject_statistics_created_by ON subject_statistics (created_by);

ALTER TABLE subject_statistics
    ADD CONSTRAINT FK_SUBJECT_STATISTICS_ON_MODIFIED_BY FOREIGN KEY (modified_by) REFERENCES account (id);

CREATE INDEX idx_subject_statistics_modified_by ON subject_statistics (modified_by);

ALTER TABLE subject_statistics
    ADD CONSTRAINT FK_SUBJECT_STATISTICS_ON_SUBJECT FOREIGN KEY (subject_id) REFERENCES subject (id);

CREATE INDEX idx_subject_statistics_subject ON subject_statistics (subject_id);

ALTER TABLE subject
    DROP COLUMN aggregated_average_similarity;

GRANT SELECT, INSERT, UPDATE, DELETE ON public.subject_statistics TO ssbd01mpp;
