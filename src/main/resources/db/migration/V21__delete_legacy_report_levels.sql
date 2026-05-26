DO $$
    BEGIN
        DELETE FROM rule_preset
        WHERE raport_level_id IN (
            SELECT id FROM raport_level_dict
            WHERE name IN ('PEŁNY_WGLĄD', 'TYLKO_PROCENTY', 'BRAK_WGLĄDU')
        );

        DELETE FROM raport_level_dict
        WHERE name IN ('PEŁNY_WGLĄD', 'TYLKO_PROCENTY', 'BRAK_WGLĄDU');
    END $$;