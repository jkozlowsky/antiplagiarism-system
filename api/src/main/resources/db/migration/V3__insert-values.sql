DO $$
    DECLARE
        admin_id UUID := gen_random_uuid();
        teacher_id UUID := gen_random_uuid();
        student_id UUID := gen_random_uuid();

        -- Słowniki
        lang_pl_id UUID := gen_random_uuid();
        lang_en_id UUID := gen_random_uuid();

        role_admin_id UUID := gen_random_uuid();
        role_teacher_id UUID := gen_random_uuid();
        role_student_id UUID := gen_random_uuid();

        rep_level_id UUID := gen_random_uuid();

        -- Elementy MPP
        sub_id UUID := gen_random_uuid();
        al_admin_id UUID := gen_random_uuid();
        al_teacher_id UUID := gen_random_uuid();
        al_student_id UUID := gen_random_uuid();

        czas_obecny TIMESTAMP := NOW();
    BEGIN
        -- SŁOWNIKI (Dostosowane do braku pól audytowych)
        INSERT INTO public.language_dict (id, language, version) VALUES (lang_pl_id, 'pl', 0), (lang_en_id, 'en', 0);
        INSERT INTO public.access_level_dict (id, access_level_name, version) VALUES
            (role_admin_id, 'ADMIN', 0),
            (role_teacher_id, 'TEACHER', 0),
            (role_student_id, 'STUDENT', 0);
        INSERT INTO public.raport_level_dict (id, name, version) VALUES
            (rep_level_id, 'PEŁNY_WGLĄD', 0), (gen_random_uuid(), 'TYLKO_PROCENTY', 0), (gen_random_uuid(), 'BRAK_WGLĄDU', 0);

        -- 1. KONTO ADMINA
        INSERT INTO public.account (id, login, email, password_hash, name, surname, language_version_id, is_blocked, version, created_by, created_at)
        VALUES (admin_id, 'admin', 'admin@ssbd.edu.pl', '$2a$12$NqL7C7wYxGz4mFhX1Rj8dO2Bw4oF7h.v5.1oZq4e6xZ2o2X0X3z6e', 'Adam', 'Root', lang_pl_id, false, 0, admin_id, czas_obecny);

        INSERT INTO public.access_level (id, account_id, access_level_id, version, created_by, created_at)
        VALUES (al_admin_id, admin_id, role_admin_id, 0, admin_id, czas_obecny);
        INSERT INTO public.admin (id) VALUES (al_admin_id);

        -- 2. KONTO PROWADZĄCEGO
        INSERT INTO public.account (id, login, email, password_hash, name, surname, language_version_id, is_blocked, version, created_by, created_at)
        VALUES (teacher_id, 'jkowalski', 'jkowalski@ssbd.edu.pl', '$2a$12$NqL7C7wYxGz4mFhX1Rj8dO2Bw4oF7h.v5.1oZq4e6xZ2o2X0X3z6e', 'Jan', 'Kowalski', lang_pl_id, false, 0, admin_id, czas_obecny);

        INSERT INTO public.access_level (id, account_id, access_level_id, version, created_by, created_at)
        VALUES (al_teacher_id, teacher_id, role_teacher_id, 0, admin_id, czas_obecny);
        INSERT INTO public.teacher (id) VALUES (al_teacher_id);

        -- 3. KONTO STUDENTA
        INSERT INTO public.account (id, login, email, password_hash, name, surname, language_version_id, is_blocked, version, created_by, created_at)
        VALUES (student_id, 'anowak', 'anowak@ssbd.edu.pl', '$2a$12$NqL7C7wYxGz4mFhX1Rj8dO2Bw4oF7h.v5.1oZq4e6xZ2o2X0X3z6e', 'Anna', 'Nowak', lang_pl_id, false, 0, admin_id, czas_obecny);

        INSERT INTO public.access_level (id, account_id, access_level_id, version, created_by, created_at)
        VALUES (al_student_id, student_id, role_student_id, 0, admin_id, czas_obecny);
        INSERT INTO public.student (id) VALUES (al_student_id);

        -- 4. INICJALIZACJA MPP
        INSERT INTO public.Subject (id, name, edition, gitea_url, archived, version, created_by, created_at)
        VALUES (sub_id, 'Algorytmy', '2026', 'https://gitea.it.p.lodz.pl/asd_2026', false, 0, teacher_id, czas_obecny);

        INSERT INTO public.subject_assignment (id, teacher_id, subject_id, has_edit_permissions, version, created_by, created_at)
        VALUES (gen_random_uuid(), al_teacher_id, sub_id, true, 0, teacher_id, czas_obecny);

        INSERT INTO public.rule_preset (id, subject_id, raport_level_id, student_ticket_count, is_preset, version, created_by, created_at)
        VALUES (gen_random_uuid(), sub_id, rep_level_id, 3, true, 0, teacher_id, czas_obecny);
    END $$;