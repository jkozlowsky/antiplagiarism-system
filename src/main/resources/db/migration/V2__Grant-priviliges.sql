GRANT SELECT, INSERT, UPDATE ON public.account TO ssbd01mok;
GRANT SELECT, INSERT, UPDATE ON public.access_level TO ssbd01mok;
GRANT SELECT, INSERT, UPDATE ON public.admin TO ssbd01mok;
GRANT SELECT, INSERT, UPDATE ON public.teacher TO ssbd01mok;
GRANT SELECT, INSERT, UPDATE ON public.student TO ssbd01mok;

GRANT SELECT ON public.account TO ssbd01mpp;
GRANT SELECT ON public.access_level TO ssbd01mpp;
GRANT SELECT ON public.admin TO ssbd01mpp;
GRANT SELECT ON public.teacher TO ssbd01mpp;
GRANT SELECT ON public.student TO ssbd01mpp;
GRANT SELECT, INSERT, UPDATE, DELETE ON public.Subject TO ssbd01mpp;
GRANT SELECT, INSERT, UPDATE, DELETE ON public.subject_assignment TO ssbd01mpp;
GRANT SELECT, INSERT, UPDATE, DELETE ON public.rule_preset TO ssbd01mpp;
GRANT SELECT, INSERT, UPDATE, DELETE ON public.schedule TO ssbd01mpp;
GRANT SELECT, INSERT, UPDATE, DELETE ON public.repository TO ssbd01mpp;
GRANT SELECT, INSERT, UPDATE, DELETE ON public.repository_student TO ssbd01mpp;
GRANT SELECT, INSERT, UPDATE, DELETE ON public.report TO ssbd01mpp;
GRANT SELECT, INSERT, UPDATE, DELETE ON public.report_repository TO ssbd01mpp;

GRANT SELECT ON public.access_level_dict TO ssbd01mok, ssbd01mpp;
GRANT SELECT ON public.language_dict TO ssbd01mok, ssbd01mpp;
GRANT SELECT ON public.raport_level_dict TO ssbd01mpp;