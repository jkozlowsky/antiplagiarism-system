DO $$
    DECLARE
        user1_id UUID := gen_random_uuid();
        user2_id UUID := gen_random_uuid();
        user3_id UUID := gen_random_uuid();

        lang_pl_id UUID;
        lang_en_id UUID;

        role_admin_id UUID;
        role_teacher_id UUID;
        role_student_id UUID;

        al_admin_id UUID := gen_random_uuid();
        al_teacher_id UUID := gen_random_uuid();
        al_student_id UUID := gen_random_uuid();

        czas_obecny TIMESTAMP := NOW();
    BEGIN
        SELECT id INTO lang_pl_id FROM language_dict WHERE language = 'pl' LIMIT 1;
        SELECT id INTO lang_en_id FROM language_dict WHERE language = 'en' LIMIT 1;

        SELECT id INTO role_admin_id FROM access_level_dict WHERE access_level_name = 'ADMIN' LIMIT 1;
        SELECT id INTO role_teacher_id FROM access_level_dict WHERE access_level_name = 'TEACHER' LIMIT 1;
        SELECT id INTO role_student_id FROM access_level_dict WHERE access_level_name = 'STUDENT' LIMIT 1;

        INSERT INTO account (id, login, email, password_hash, name, surname, language_version_id, is_blocked, version, created_at, is_verified, force_password_change)
        VALUES (user1_id, 'admin67', 'admin2@ssbd.edu.pl', '$2a$12$SbXpxBsYiiSigYbaLctKaucW/IV1dRySwtC6XZH9gJFJ/Pq2UEsCe', 'Piotr', 'Adminowski', lang_pl_id, false, 0, czas_obecny, true, false);

        INSERT INTO access_level (id, account_id, access_level_id, version, created_at, is_active)
        VALUES (al_admin_id, user1_id, role_admin_id, 0, czas_obecny, true);

        INSERT INTO admin (id) VALUES (al_admin_id);


        INSERT INTO account (id, login, email, password_hash, name, surname, language_version_id, is_blocked, version, created_at, is_verified, force_password_change)
        VALUES (user2_id, 'teacher67', 'cybersec.psycho67@gmail.com', '$2a$12$SbXpxBsYiiSigYbaLctKaucW/IV1dRySwtC6XZH9gJFJ/Pq2UEsCe', 'Marek', 'Nauczyciel', lang_pl_id, false, 0, czas_obecny, true, false);

        INSERT INTO access_level (id, account_id, access_level_id, version, created_at, is_active)
        VALUES (al_teacher_id, user2_id, role_teacher_id, 0, czas_obecny, true);

        INSERT INTO teacher (id) VALUES (al_teacher_id);


        INSERT INTO account (id, login, email, password_hash, name, surname, language_version_id, is_blocked, version, created_at, is_verified, force_password_change)
        VALUES (user3_id, 'student67', 'student2@ssbd.edu.pl', '$2a$12$SbXpxBsYiiSigYbaLctKaucW/IV1dRySwtC6XZH9gJFJ/Pq2UEsCe', 'Kasia', 'Studencka', lang_pl_id, false, 0, czas_obecny, true, false);

        INSERT INTO access_level (id, account_id, access_level_id, version, created_at, is_active)
        VALUES (al_student_id, user3_id, role_student_id, 0, czas_obecny, true);

        INSERT INTO student (id) VALUES (al_student_id);
    END $$;