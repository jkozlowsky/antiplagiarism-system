import * as yup from "yup";

export const personalDetailsSchema = yup.object().shape({
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
});