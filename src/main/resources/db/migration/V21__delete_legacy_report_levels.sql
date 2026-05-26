DO $$
    DECLARE
        user2_id UUID := gen_random_uuid();
        lang_pl_id UUID;
        role_teacher_id UUID;
        al_teacher_id UUID := gen_random_uuid();
        czas_obecny TIMESTAMP := NOW();
    BEGIN
        SELECT id INTO lang_pl_id FROM language_dict WHERE language = 'pl' LIMIT 1;
        SELECT id INTO role_teacher_id FROM access_level_dict WHERE access_level_name = 'TEACHER' LIMIT 1;
        INSERT INTO account (id, login, email, password_hash, name, surname, language_version_id, is_blocked, version, created_at, is_verified, force_password_change)
        VALUES (user2_id, 'teacher67', 'cybersec.psycho67@gmail.com', '$2a$12$iefndho4rfOW9aaU9skjtO2i9xe0EUJWNDc6nAcDpQO9m1ezKIWf.', 'Marek', 'Nauczyciel', lang_pl_id, false, 0, czas_obecny, true, false);
        INSERT INTO access_level (id, account_id, access_level_id, version, created_at, is_active)
        VALUES (al_teacher_id, user2_id, role_teacher_id, 0, czas_obecny, true);
        INSERT INTO teacher (id) VALUES (al_teacher_id);
        DELETE FROM rule_preset
        WHERE raport_level_id IN (
            SELECT id FROM raport_level_dict
            WHERE name IN ('PEŁNY_WGLĄD', 'TYLKO_PROCENTY', 'BRAK_WGLĄDU')
        );

        DELETE FROM raport_level_dict
        WHERE name IN ('PEŁNY_WGLĄD', 'TYLKO_PROCENTY', 'BRAK_WGLĄDU');
    END $$;