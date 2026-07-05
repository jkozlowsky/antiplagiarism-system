import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { Mail, ArrowLeft, CheckCircle2, AlertCircle, ShieldCheck } from "lucide-react";
import { useForm } from "react-hook-form";
import { yupResolver } from "@hookform/resolvers/yup";
import axios from "axios";
import { useTranslation } from "react-i18next";
import { motion } from "framer-motion";

import { PATHS } from "../../../routes/paths.ts";
import { passwordResetService } from "../../../services/passwordResetService.ts";
import SubmitButton from "../../../shared/components/buttons/SubmitButton.tsx";
import { emailSchema, type EmailFormData } from "../../../shared/validators/emailSchema.ts";

export default function PasswordResetInitPage() {
    const { t } = useTranslation();
    const navigate = useNavigate();
    const [status, setStatus] = useState<'idle' | 'loading' | 'success' | 'error'>('idle');
    const [apiError, setApiError] = useState("");

    const {
        register,
        handleSubmit,
        getValues,
        formState: { errors }
    } = useForm<EmailFormData>({
        resolver: yupResolver(emailSchema)
    });

    const onSubmit = async (data: EmailFormData) => {
        setStatus('loading');
        setApiError("");
        try {
            await passwordResetService.requestReset(data.email);
            setStatus('success');
        } catch (error) {
            console.error("Failed to request password reset", error);
            setStatus('error');
            if (axios.isAxiosError(error)) {
                setApiError(error.response?.data?.message || t("passwordReset.init.errorSend"));
            } else {
                setApiError(t("passwordReset.init.errorUnexpected"));
            }
        }
    };

    if (status === 'success') {
        return (
            <motion.div
                id="resetPasswordSuccessMsg"
                initial={{ opacity: 0, scale: 0.95 }}
                animate={{ opacity: 1, scale: 1 }}
                className="w-full max-w-[440px] flex flex-col justify-center text-center"
            >
                <div className="w-20 h-20 mx-auto bg-green-500/10 text-green-600 rounded-full flex items-center justify-center mb-6 shadow-inner border border-green-500/20">
                    <CheckCircle2 size={40} />
                </div>
                <h2 className="text-3xl font-black text-primary tracking-tight mb-3">
                    {t("passwordReset.init.successTitle")}
                </h2>
                <p className="text-base text-secondary mb-10 max-w-sm mx-auto leading-relaxed">
                    {t("passwordReset.init.successDesc1")} <br/>
                    <span className="font-bold text-primary">{getValues("email")}</span> <br/>
                    {t("passwordReset.init.successDesc2")}
                </p>
                <Link
                    to={PATHS.LOGIN}
                    className="inline-block w-full bg-brand text-white font-black py-4 rounded-2xl hover:bg-brand-hover transition-colors shadow-lg tracking-widest uppercase text-sm"
                >
                    {t("passwordReset.init.returnToLogin")}
                </Link>
            </motion.div>
        );
    }

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
                    {t("passwordReset.init.title")}
                </h2>
                <p className="text-base text-secondary">
                    {t("passwordReset.init.subtitle")}
                </p>
            </div>

            {status === 'error' && (
                <motion.div initial={{ opacity: 0, y: -10 }} animate={{ opacity: 1, y: 0 }} className="mb-8 p-4 bg-danger-subtle text-danger text-sm font-bold rounded-2xl border border-danger-border flex items-center justify-center gap-2 shadow-sm">
                    <AlertCircle size={20} className="flex-shrink-0" />
                    <span>{apiError}</span>
                </motion.div>
            )}

            <form onSubmit={handleSubmit(onSubmit)} className="space-y-6">
                <div className="group">
                    <label className="block text-[11px] font-black text-secondary tracking-widest mb-2 uppercase ml-1 transition-colors group-focus-within:text-primary">
                        {t("passwordReset.init.email")}
                    </label>
                    <div className="relative border border-border rounded-xl focus-within:border-brand transition-all duration-300 focus-within:ring-2 focus-within:ring-brand/20 bg-surface">
                        <Mail className="absolute left-4 top-1/2 -translate-y-1/2 text-secondary opacity-60 transition-colors group-focus-within:text-brand" size={20}/>
                        <input
                            id="email"
                            type="text"
                            placeholder={t("passwordReset.init.emailPlaceholder", "213767@edu.p.lodz.pl")}
                            {...register("email")}
                            className="w-full pl-12 pr-4 py-3.5 outline-none text-base bg-transparent text-primary placeholder:text-secondary/50 font-medium disabled:opacity-50"
                            disabled={status === 'loading'}
                        />
                    </div>
                    {errors.email && (
                        <p className="text-danger text-xs mt-1.5 ml-1 font-medium">{t(errors.email.message as string)}</p>
                    )}
                </div>

                <div className="space-y-3 pt-4">
                    <motion.div whileHover={{ scale: 1.015 }} whileTap={{ scale: 0.98 }}>
                        <SubmitButton
                            id="resetPasswordSubmitBtn"
                            type="submit"
                            isLoading={status === 'loading'}
                            className="w-full py-4 shadow-lg shadow-brand/20 font-black text-base tracking-wide rounded-2xl transition-all"
                        >
                            {t("passwordReset.init.submit")}
                        </SubmitButton>
                    </motion.div>

                    <motion.div whileHover={{ scale: 1.015 }} whileTap={{ scale: 0.98 }}>
                        <button
                            type="button"
                            onClick={() => navigate(PATHS.LOGIN_EMAIL)}
                            disabled={status === 'loading'}
                            className="w-full py-4 bg-surface border border-border text-primary font-black text-sm tracking-widest rounded-2xl hover:bg-base transition-all shadow-sm"
                        >
                            {t("passwordReset.init.codeLogin")}
                        </button>
                    </motion.div>
                </div>
            </form>

            <div className="mt-12 pt-6 border-t border-border/50 text-center">
                <Link to={PATHS.LOGIN} className="inline-flex items-center text-sm font-bold text-secondary hover:text-primary transition-colors group">
                    <ArrowLeft size={16} className="mr-2 group-hover:-translate-x-1 transition-transform" />
                    {t("passwordReset.init.backToLogin")}
                </Link>
            </div>
        </motion.div>
    );
}