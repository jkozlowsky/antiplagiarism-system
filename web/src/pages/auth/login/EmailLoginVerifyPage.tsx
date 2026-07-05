import { useState } from "react";
import { useNavigate, useLocation, Navigate, Link } from "react-router-dom";
import { KeyRound, AlertCircle, ArrowLeft, ShieldCheck } from "lucide-react";
import { useForm } from "react-hook-form";
import * as yup from "yup";
import { yupResolver } from "@hookform/resolvers/yup";
import axios from "axios";
import { useTranslation } from "react-i18next";
import { motion } from "framer-motion";

import { PATHS } from "../../../routes/paths.ts";
import { authService } from "../../../services/authService.ts";
import { useAuth } from "../../../hooks/useAuth.ts";
import SubmitButton from "../../../shared/components/buttons/SubmitButton.tsx";

const tokenSchema = yup.object().shape({
    token: yup.string()
        .required("validation.emailCode.required")
        .length(10, "validation.emailCode.length")
        .matches(/^[A-Za-z0-9]{10}$/, "validation.emailCode.format")
});

export default function LoginEmailVerifyPage() {
    const { t } = useTranslation();
    const navigate = useNavigate();
    const location = useLocation();
    const { setTokens } = useAuth();

    const [isLoading, setIsLoading] = useState(false);
    const [apiError, setApiError] = useState("");

    const email = location.state?.email;

    const { register, handleSubmit, formState: { errors } } = useForm({
        resolver: yupResolver(tokenSchema)
    });

    if (!email) {
        return <Navigate to={PATHS.LOGIN_EMAIL} replace />;
    }

    const onSubmit = async (data: { token: string }) => {
        setIsLoading(true);
        setApiError("");
        try {
            const response = await authService.verifyEmailCode(data.token, email);
            if (response?.token) {
                setTokens(response.token, response.refreshToken);
                navigate(PATHS.PROFILE, { replace: true });
            }
        } catch (error) {
            if (axios.isAxiosError(error) && error.response?.status === 409) {
                setApiError(t('auth.emailLogin.verify.errorInvalid'));
            } else {
                setApiError(t('auth.emailLogin.verify.errorGeneric'));
            }
        } finally {
            setIsLoading(false);
        }
    };

    return (
        <motion.div
            initial={{ opacity: 0, scale: 0.98 }}
            animate={{ opacity: 1, scale: 1 }}
            transition={{ duration: 0.5, ease: "easeOut" }}
            className="w-full max-w-[440px]"
        >
            <div className="mb-10 text-center lg:text-left">
                <div className="lg:hidden flex items-center justify-center gap-3 text-brand mb-6">
                    <ShieldCheck size={40} />
                    <span className="font-black text-3xl tracking-tight text-primary">Antyplagiat PŁ</span>
                </div>
                <h2 className="text-3xl md:text-4xl font-black text-primary tracking-tight mb-3">
                    {t('auth.emailLogin.verify.heading')}
                </h2>
                <p className="text-base text-secondary">
                    {t('auth.emailLogin.verify.subheading')} <span className="font-bold text-primary">{email}</span>. {t('auth.emailLogin.verify.subheadingTime')}
                </p>
            </div>

            {apiError && (
                <motion.div initial={{ opacity: 0, y: -10 }} animate={{ opacity: 1, y: 0 }} className="mb-8 p-4 bg-danger-subtle text-danger text-sm font-bold rounded-2xl border border-danger-border flex items-start gap-2 shadow-sm">
                    <AlertCircle size={20} className="shrink-0" />
                    <span>{apiError}</span>
                </motion.div>
            )}

            <form onSubmit={handleSubmit(onSubmit)} className="space-y-6">
                <div className="group">
                    <label htmlFor="token" className="block text-[11px] font-black text-secondary tracking-widest mb-2 uppercase ml-1 transition-colors group-focus-within:text-primary">
                        {t('auth.emailLogin.verify.codeLabel')}
                    </label>
                    <div className="relative border border-border rounded-xl focus-within:border-brand transition-all duration-300 focus-within:ring-2 focus-within:ring-brand/20 bg-surface">
                        <KeyRound className="absolute left-4 top-1/2 -translate-y-1/2 text-secondary opacity-60 transition-colors group-focus-within:text-brand" size={20} />
                        <input
                            id="token"
                            type="text"
                            placeholder={t('auth.emailLogin.verify.codePlaceholder')}
                            maxLength={10}
                            {...register("token")}
                            className="w-full pl-12 pr-4 py-3.5 outline-none text-base bg-transparent text-primary tracking-[0.3em] font-mono font-bold disabled:opacity-50"
                            disabled={isLoading}
                        />
                    </div>
                    {errors.token && (
                        <p className="text-danger text-xs mt-1.5 ml-1 font-medium">{t(errors.token.message as string)}</p>
                    )}
                </div>

                <motion.div whileHover={{ scale: 1.015 }} whileTap={{ scale: 0.98 }} className="pt-2">
                    <SubmitButton type="submit" isLoading={isLoading} className="w-full py-4 shadow-lg shadow-brand/20 font-black text-base tracking-wide rounded-2xl transition-all">
                        {t('auth.emailLogin.verify.submitButton')}
                    </SubmitButton>
                </motion.div>
            </form>

            <div className="mt-12 pt-6 border-t border-border/50 text-center">
                <Link to={PATHS.LOGIN} className="inline-flex items-center text-sm font-bold text-secondary hover:text-primary transition-colors group">
                    <ArrowLeft size={16} className="mr-2 group-hover:-translate-x-1 transition-transform" />
                    {t('auth.emailLogin.verify.backToLogin')}
                </Link>
            </div>
        </motion.div>
    );
}