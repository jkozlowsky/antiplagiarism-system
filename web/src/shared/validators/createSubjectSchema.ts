import * as yup from "yup";

export const createSubjectSchema = yup.object({
    name: yup.string().required("validation.required"),
    organizationName: yup.string().required("validation.required"),
    edition: yup.string().required("validation.required"),
    subjectDescription: yup.string().nullable().default(null),
    giteaURL: yup.string()
        .matches(/^https?:\/\/.+/, "validation.url.invalid")
        .required("validation.required"),
    templateId: yup.string().uuid("validation.uuid.invalid").nullable().default(null),
    manualRules: yup.object({
        studentTicketCount: yup.number()
            .typeError("validation.number.required")
            .integer("validation.integer.invalid")
            .min(1, "validation.min.one")
            .required("validation.required"),
        minimumTokensMatch: yup.number()
            .typeError("validation.number.required")
            .integer("validation.integer.invalid")
            .min(1, "validation.min.one")
            .required("validation.required"),
        enableNormalization: yup.boolean().required("validation.required")
    }).nullable().optional()
}).required();

export type CreateSubjectFormData = yup.InferType<typeof createSubjectSchema>;