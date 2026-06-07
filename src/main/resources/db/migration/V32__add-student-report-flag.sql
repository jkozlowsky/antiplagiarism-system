ALTER TABLE report
    ADD is_student_report BOOLEAN DEFAULT false;

ALTER TABLE report
    ALTER COLUMN is_student_report SET NOT NULL;