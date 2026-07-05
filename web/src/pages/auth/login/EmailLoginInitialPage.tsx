import { useState } from "react";
import { useNavigate, Link } from "react-router-dom";
import { Mail, ArrowLeft, AlertCircle, ShieldCheck } from "lucide-react";
import { useForm } from "react-hook-form";
import { yupResolver } from "@hookform/resolvers/yup";
import axios from "axios";
import { useTranslation } from "react-i18next";
import { motion } from "framer-motion";

import { PATHS } from "../../../routes/paths.ts";
import { authService } from "../../../services/authService.ts";
import SubmitButton from "../../../shared/components/buttons/SubmitButton.tsx";
import { emailSchema, type EmailFormData } from "../../../shared/validators/emailSchema.ts";

export default function LoginEmailInitialPage() {
    const { t } = useTranslation();
    const navigate = useNavigate();
    const [isLoading, setIsLoading] = useState(false);
    const [apiError, setApiError] = useState("");

    const { register, handleSubmit, formState: { errors } } = useForm<EmailFormData>({
        resolver: yupResolver(emailSchema)
    });

    const onSubmit = async (data: EmailFormData) => {
        setIsLoading(true);
        setApiError("");
        try {
            await authService.loginWithEmail(data.email);
            navigate(PATHS.LOGIN_EMAIL_VERIFY, { state: { email: data.email } });
        } catch (error) {
            if (axios.isAxiosError(error)) {
                if (error.response?.status === 409) {
                    setApiError(t('auth.emailLogin.errorBlocked'));
                } else if (error.response?.status === 404) {
                    setApiError(t('auth.emailLogin.errorNotFound'));
                } else {
                    setApiError(error.response?.data?.message || t('auth.emailLogin.errorGeneric'));
                }
            } else {
                setApiError(t('auth.emailLogin.errorGeneric'));
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
                    {t('auth.emailLogin.heading')}
                </h2>
                <p className="text-base text-secondary">
                    {t('auth.emailLogin.subheading')}
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
                    <label htmlFor="email" className="block text-[11px] font-black text-secondary tracking-widest mb-2 uppercase ml-1 transition-colors group-focus-within:text-primary">
                        {t('auth.emailLogin.emailLabel')}
                    </label>
                    <div className="relative border border-border rounded-xl focus-within:border-brand transition-all duration-300 focus-within:ring-2 focus-within:ring-brand/20 bg-surface">
                        <Mail className="absolute left-4 top-1/2 -translate-y-1/2 text-secondary opacity-60 transition-colors group-focus-within:text-brand" size={20} />
                        <input
                            id="email"
                            type="text"
                            placeholder={t('auth.emailLogin.emailPlaceholder')}
                            {...register("email")}
                            className="w-full pl-12 pr-4 py-3.5 outline-none text-base bg-transparent text-primary placeholder:text-secondary/50 font-medium disabled:opacity-50"
                            disabled={isLoading}
                        />
                    </div>
                    {errors.email && (
                        <p className="text-danger text-xs mt-1.5 ml-1 font-medium">{t(errors.email.message as string)}</p>
                    )}
                </div>

                <motion.div whileHover={{ scale: 1.015 }} whileTap={{ scale: 0.98 }} className="pt-2">
                    <SubmitButton type="submit" isLoading={isLoading} className="w-full py-4 shadow-lg shadow-brand/20 font-black text-base tracking-wide rounded-2xl transition-all">
                        {t('auth.emailLogin.submitButton')}
                    </SubmitButton>
                </motion.div>
            </form>

            <div className="mt-12 pt-6 border-t border-border/50 text-center">
                <Link to={PATHS.LOGIN} className="inline-flex items-center text-sm font-bold text-secondary hover:text-primary transition-colors group">
                    <ArrowLeft size={16} className="mr-2 group-hover:-translate-x-1 transition-transform" />
                    {t('auth.emailLogin.backToLogin')}
                </Link>
            </div>
        </motion.div>
    );
}