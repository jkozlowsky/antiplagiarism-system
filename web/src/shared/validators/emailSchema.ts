import * as yup from "yup";

const strictEmailRegex = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/;

export const emailSchema = yup.object({
    email: yup.string()
        .required("validation.required")
        .matches(strictEmailRegex, "validation.email.invalid")
}).required();

export type EmailFormData = yup.InferType<typeof emailSchema>;