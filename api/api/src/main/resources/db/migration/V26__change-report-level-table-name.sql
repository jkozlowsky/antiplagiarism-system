ALTER TABLE raport_level_dict RENAME TO report_level_dict;

ALTER TABLE rule_preset RENAME COLUMN raport_level_id TO report_level_id;

ALTER INDEX idx_rule_preset_raport_level_id RENAME TO idx_rule_preset_report_level_id;

UPDATE report_level_dict
SET name = 'ONLY_HIGHEST_PERCENT'
WHERE name = 'FULL_VIEW';
