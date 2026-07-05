import * as yup from "yup";

export const registerSchema = yup.object({
    name: yup.string()
        .required("validation.required")
        .min(3, "validation.user.nameMin")
        .max(32, "validation.user.nameMax")
        .matches(/^[\p{L}][\p{L}\s\-']+$/u, "validation.user.nameFormat"),

    surname: yup.string()
        .required("validation.required")
        .min(3, "validation.user.surnameMin")
        .max(32, "validation.user.surnameMax")
        .matches(/^[\p{L}][\p{L}\s\-']+$/u, "validation.user.surnameFormat"),

    login: yup.string()
        .required("validation.required")
        .min(3, "validation.user.loginMin")
        .max(32, "validation.user.loginMax")
        .matches(/^[a-zA-Z0-9]+$/, "validation.user.loginFormat"),

    email: yup.string()
        .required("validation.required")
        .email("validation.email.invalid"),

    password: yup.string()
        .required("validation.required")
        .min(12, "validation.password.minLength")
        .max(64, "validation.password.maxLength")
        .matches(/^(?=.*[A-Z])(?=.*[!@#$%^&*(),.?":{}|<>]).*$/, "validation.password.specialChar"),

    confirmPassword: yup.string()
        .required("validation.password.confirmRequired")
        .oneOf([yup.ref('password')], "validation.password.mismatch"),

}).required();

export type RegisterFormData = yup.InferType<typeof registerSchema>;