import * as yup from "yup";

const emailRegex = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/;

export const emailChangeSchema = yup.object({
    password: yup.string()
        .required("validation.required"),
    newEmail: yup.string()
        .required("validation.required")
        .matches(emailRegex, "validation.email.invalid")
}).required();

export type EmailChangeFormData = yup.InferType<typeof emailChangeSchema>;