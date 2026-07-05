import * as yup from "yup";

export const _2faSchema = yup.object({
    auth2F: yup.string()
        .required("auth.2fa.code.required")
        .matches(/^[0-9]{8}$/, "auth.2fa.code.validationError")
}).required();

export type _2faFormData = yup.InferType<typeof _2faSchema>;