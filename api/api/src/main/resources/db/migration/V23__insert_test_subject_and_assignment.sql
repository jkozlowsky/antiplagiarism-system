DO $$
DECLARE
teacher_acc_id CONSTANT UUID := 'a0000000-0000-0000-0000-000000000002';
    test_subject_id UUID := gen_random_uuid();
    test_assignment_id UUID := gen_random_uuid();
    test_rule_preset_id UUID := gen_random_uuid();
    full_view_raport_level_id UUID;
    now_ts CONSTANT TIMESTAMP := NOW();
BEGIN

SELECT id INTO full_view_raport_level_id FROM public.raport_level_dict WHERE name = 'FULL_VIEW' LIMIT 1;

-- Tworzenie testowego przedmiotu
INSERT INTO public.subject (
    id, version, created_at, created_by,
    archived, edition, name, gitea_url, subject_description
) VALUES (
             test_subject_id, 0, now_ts, teacher_acc_id,
             false, '2024/2025', 'Programowanie Obiektowe Test', 'http://gitea.local/byleby_nie_null', 'Testowy przedmiot dla MPP14'
         );

-- Przypisanie nauczyciela do przedmiotu poprzez encję asocjacyjną SubjectAssignment
INSERT INTO public.subject_assignment (
    id, version, created_at, created_by,
    has_edit_permissions, subject_id, teacher_id
) VALUES (
             test_assignment_id, 0, now_ts, teacher_acc_id,
             true, test_subject_id, (SELECT id FROM public.teacher WHERE id IN (SELECT id FROM public.access_level WHERE account_id = teacher_acc_id))
         );

-- Tworzenie reguły RulePreset powiązanej bezpośrednio z nowym przedmiotem
INSERT INTO public.rule_preset (
    id, version, created_at, created_by,
    is_preset, student_ticket_count, raport_level_id, subject_id,
    minimum_tokens_match, normalization
) VALUES (
             test_rule_preset_id, 0, now_ts, teacher_acc_id,
             false, 10, full_view_raport_level_id, test_subject_id,
             20, false
         );
END $$;