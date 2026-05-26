GRANT SELECT ON TABLE account TO ssbd01mpp;
GRANT SELECT ON TABLE access_level TO ssbd01mpp;
GRANT SELECT ON TABLE teacher TO ssbd01mpp;
GRANT SELECT ON TABLE access_level_dict TO ssbd01mpp;

GRANT SELECT, INSERT, UPDATE, DELETE ON TABLE subject TO ssbd01mpp;
GRANT SELECT, INSERT, UPDATE, DELETE ON TABLE subject_assignment TO ssbd01mpp;

INSERT INTO subject (id, version, created_at, created_by, archived, name, edition, subject_description, gitea_url)
VALUES (
           'a1b2c3d4-e5f6-7a8b-9c0d-1e2f3a4b5c6d',
           0,
           CURRENT_TIMESTAMP,
           (SELECT id FROM account WHERE login = 'admin' LIMIT 1),
    false,
    'Sieciowe Systemy Baz Danych',
    '2026',
    'Przedmiot realizowany w semestrze letnim',
    'https://gitea.example.com/ssbd2026/subject-repo'
    );

INSERT INTO subject_assignment (id, version, created_at, created_by, has_edit_permissions, subject_id, teacher_id)
VALUES (
           'f1e2d3c4-b5a6-9f8e-7d6c-5b4a3c2d1e0f',
           0,
           CURRENT_TIMESTAMP,
           (SELECT id FROM account WHERE login = 'admin' LIMIT 1),
    true,
    'a1b2c3d4-e5f6-7a8b-9c0d-1e2f3a4b5c6d',
       (SELECT a.id FROM access_level a JOIN access_level_dict d ON a.access_level_id = d.id WHERE d.access_level_name = 'TEACHER' LIMIT 1)
    );