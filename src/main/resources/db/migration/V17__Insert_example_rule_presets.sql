INSERT INTO raport_level_dict (id, version, name)
VALUES
    (gen_random_uuid(), 0, 'FULL_VIEW'),
    (gen_random_uuid(), 0, 'ONLY_PERCENTAGES'),
    (gen_random_uuid(), 0, 'NOTHING');

INSERT INTO rule_preset (
    id,
    version,
    created_at,
    raport_level_id,
    student_ticket_count,
    is_preset,
    subject_id
)
VALUES
    (
        gen_random_uuid(),
        0,
        CURRENT_TIMESTAMP,
        (SELECT id FROM raport_level_dict WHERE name = 'FULL_VIEW'),
        10,
        true,
        NULL
    ),
    (
        gen_random_uuid(),
        0,
        CURRENT_TIMESTAMP,
        (SELECT id FROM raport_level_dict WHERE name = 'ONLY_PERCENTAGES'),
        5,
        true,
        NULL
    ),
    (
        gen_random_uuid(),
        0,
        CURRENT_TIMESTAMP,
        (SELECT id FROM raport_level_dict WHERE name = 'NOTHING'),
        0,
        true,
        NULL
    );