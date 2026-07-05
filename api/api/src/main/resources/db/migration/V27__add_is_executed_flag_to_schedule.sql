ALTER TABLE schedule ADD COLUMN is_executed BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE schedule ADD COLUMN has_failed BOOLEAN NOT NULL DEFAULT FALSE;

Create INDEX idx_schedule_is_executed_date ON schedule (is_executed, schedule_date_time);