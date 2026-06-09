ALTER TABLE report
    ADD initiating_repository UUID;

ALTER TABLE report
    ADD CONSTRAINT FK_REPORT_ON_INITIATING_REPOSITORY FOREIGN KEY (initiating_repository) REFERENCES repository (id);

CREATE INDEX idx_report_repository ON report (initiating_repository);