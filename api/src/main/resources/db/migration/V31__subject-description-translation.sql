CREATE TABLE subject_description_translation
(
    id                     UUID                        NOT NULL,
    version                INTEGER                     NOT NULL,
    created_at             TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at             TIMESTAMP WITHOUT TIME ZONE,
    created_by             UUID,
    modified_by            UUID,
    translated_description VARCHAR(255)                NOT NULL,
    is_original            BOOLEAN                     NOT NULL,
    subject_id             UUID                        NOT NULL,
    language_dict_id       UUID                        NOT NULL,
    CONSTRAINT pk_subject_description_translation PRIMARY KEY (id)
);

ALTER TABLE subject_description_translation
    ADD CONSTRAINT FK_SUBJECT_DESCRIPTION_TRANSLATION_ON_CREATED_BY FOREIGN KEY (created_by) REFERENCES account (id);

CREATE INDEX idx_subject_description_translation_created_by ON subject_description_translation (created_by);

ALTER TABLE subject_description_translation
    ADD CONSTRAINT FK_SUBJECT_DESCRIPTION_TRANSLATION_ON_LANGUAGE_DICT FOREIGN KEY (language_dict_id) REFERENCES language_dict (id);

CREATE INDEX idx_subject_description_translation_language_dict ON subject_description_translation (language_dict_id);

ALTER TABLE subject_description_translation
    ADD CONSTRAINT FK_SUBJECT_DESCRIPTION_TRANSLATION_ON_MODIFIED_BY FOREIGN KEY (modified_by) REFERENCES account (id);

CREATE INDEX idx_subject_description_translation_modified_by ON subject_description_translation (modified_by);

ALTER TABLE subject_description_translation
    ADD CONSTRAINT FK_SUBJECT_DESCRIPTION_TRANSLATION_ON_SUBJECT FOREIGN KEY (subject_id) REFERENCES subject (id);

CREATE INDEX idx_subject_description_translation_subject ON subject_description_translation (subject_id);

ALTER TABLE subject
    DROP COLUMN subject_description;

ALTER TABLE subject_assignment
    ALTER COLUMN subject_role TYPE VARCHAR(255) USING (subject_role::VARCHAR(255));

GRANT SELECT, INSERT, UPDATE, DELETE ON public.subject_description_translation TO ssbd01mpp;
