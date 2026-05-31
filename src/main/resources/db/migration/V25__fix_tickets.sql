ALTER TABLE repository DROP COLUMN ticket_count;
ALTER TABLE repository ADD COLUMN ticket_count integer not null;