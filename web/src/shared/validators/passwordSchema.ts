import * as yup from "yup";

export const passwordSchema = yup.object({
    newPassword: yup.string()
        .required("validation.required")
        .min(12, "validation.password.minLength")
        .matches(/[A-Z]/, "validation.password.uppercase")
        .matches(/[a-z]/, "validation.password.lowercase")
        .matches(/[0-9]/, "validation.password.number"),
    confirmPassword: yup.string()
        .required("validation.password.confirmRequired")
        .oneOf([yup.ref('newPassword')], "validation.password.mismatch")
}).required();

export type PasswordFormData = yup.InferType<typeof passwordSchema>;