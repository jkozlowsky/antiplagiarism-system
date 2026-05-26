DO $$
    DECLARE
admin_acc_id   CONSTANT UUID := 'a0000000-0000-0000-0000-000000000001';
        teacher_acc_id CONSTANT UUID := 'a0000000-0000-0000-0000-000000000002';
        student_acc_id CONSTANT UUID := 'a0000000-0000-0000-0000-000000000003';

        admin_al_id    CONSTANT UUID := 'b0000000-0000-0000-0000-000000000001';
        teacher_al_id  CONSTANT UUID := 'b0000000-0000-0000-0000-000000000002';
        student_al_id  CONSTANT UUID := 'b0000000-0000-0000-0000-000000000003';

        pass_hash      CONSTANT VARCHAR := '$2a$10$96eUveJsD5XjsWbKdSkZC.iWWpCkeCxS6sVqUddb0OanT5T7lWBxa';

        lang_pl_id     UUID;
        role_admin_id  UUID;
        role_teacher_id UUID;
        role_student_id UUID;

        now_ts         CONSTANT TIMESTAMP := NOW();
BEGIN
SELECT id INTO lang_pl_id      FROM public.language_dict      WHERE language = 'pl' LIMIT 1;
SELECT id INTO role_admin_id   FROM public.access_level_dict  WHERE access_level_name = 'ADMIN' LIMIT 1;
SELECT id INTO role_teacher_id FROM public.access_level_dict  WHERE access_level_name = 'TEACHER' LIMIT 1;
SELECT id INTO role_student_id FROM public.access_level_dict  WHERE access_level_name = 'STUDENT' LIMIT 1;


IF NOT EXISTS (SELECT 1 FROM public.account WHERE login = 'test_admin') THEN
            INSERT INTO public.account (
                id, login, email, password_hash, name, surname,
                language_version_id, is_blocked, is_verified, force_password_change,
                version, created_by, created_at, second_email_sent
            ) VALUES (
                admin_acc_id, 'test_admin', 'test_admin@ssbd.edu.pl', pass_hash, 'Adam', 'Root',
                lang_pl_id, false, true, false,
                0, admin_acc_id, now_ts, false
            );

INSERT INTO public.access_level (id, account_id, access_level_id, version, is_active, created_by, created_at)
VALUES (admin_al_id, admin_acc_id, role_admin_id, 0, true, admin_acc_id, now_ts);

INSERT INTO public.admin (id)
VALUES (admin_al_id);
END IF;


        IF NOT EXISTS (SELECT 1 FROM public.account WHERE login = 'test_teacher') THEN
            INSERT INTO public.account (
                id, login, email, password_hash, name, surname,
                language_version_id, is_blocked, is_verified, force_password_change,
                version, created_by, created_at, second_email_sent
            ) VALUES (
                teacher_acc_id, 'test_teacher', 'test_teacher@ssbd.edu.pl', pass_hash, 'Jan', 'Kowalski',
                lang_pl_id, false, true, false,
                0, admin_acc_id, now_ts, false
            );

INSERT INTO public.access_level (id, account_id, access_level_id, version, is_active, created_by, created_at)
VALUES (teacher_al_id, teacher_acc_id, role_teacher_id, 0, true, admin_acc_id, now_ts);

INSERT INTO public.teacher (id)
VALUES (teacher_al_id);
END IF;


        IF NOT EXISTS (SELECT 1 FROM public.account WHERE login = 'test_student') THEN
            INSERT INTO public.account (
                id, login, email, password_hash, name, surname,
                language_version_id, is_blocked, is_verified, force_password_change,
                version, created_by, created_at, second_email_sent
            ) VALUES (
                student_acc_id, 'test_student', 'test_student@ssbd.edu.pl', pass_hash, 'Anna', 'Nowak',
                lang_pl_id, false, true, false,
                0, admin_acc_id, now_ts, false
            );

INSERT INTO public.access_level (id, account_id, access_level_id, version, is_active, created_by, created_at)
VALUES (student_al_id, student_acc_id, role_student_id, 0, true, admin_acc_id, now_ts);

INSERT INTO public.student (id)
VALUES (student_al_id);
END IF;

END $$;