ALTER TABLE public.subject_assignment
ALTER COLUMN has_edit_permissions TYPE VARCHAR(50)
USING CASE
    WHEN has_edit_permissions = true THEN 'OWNER'
    WHEN has_edit_permissions = false THEN 'VIEWER'
    ELSE 'VIEWER'
END;

ALTER TABLE public.subject_assignment
    RENAME COLUMN has_edit_permissions TO subject_role;