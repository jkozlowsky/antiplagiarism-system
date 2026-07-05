import { useState } from "react";
import { Link, useSearchParams } from "react-router-dom";
import { Lock, ArrowLeft, CheckCircle2, AlertCircle } from "lucide-react";
import { PATHS } from "../../../routes/paths.ts";
import { passwordResetService } from "../../../services/passwordResetService.ts";
import { useForm } from "react-hook-form";
import { yupResolver } from "@hookform/resolvers/yup";
import { type PasswordFormData, passwordSchema } from "../../../shared/validators/passwordSchema.ts";
import axios from "axios";
import SubmitButton from "../../../shared/components/buttons/SubmitButton.tsx";
import { useTranslation } from "react-i18next";
import ConfirmationModal from "../../../shared/components/modals/ConfirmationPopup.tsx";
import { motion } from "framer-motion";

export default function PasswordResetConfirmPage() {
    const [searchParams] = useSearchParams();
    const token = searchParams.get("token");
    const [status, setStatus] = useState<'idle' | 'loading' | 'success' | 'error'>('idle');
    const [errorMessage, setErrorMessage] = useState("");
    const [isConfirmModalOpen, setIsConfirmModalOpen] = useState(false);
    const [pendingData, setPendingData] = useState<PasswordFormData | null>(null);
    const { t } = useTranslation();

    const { register, handleSubmit, formState: { errors } } = useForm<PasswordFormData>({
        resolver: yupResolver(passwordSchema)
    });

    const onFormSubmit = (data: PasswordFormData) => {
        setPendingData(data);
        setIsConfirmModalOpen(true);
    };

    const handleConfirmChange = async () => {
        if (!pendingData) return;

        setStatus('loading');
        setErrorMessage("");

        try {
            await passwordResetService.confirmReset(token!, pendingData.newPassword);
            setStatus('success');
            setIsConfirmModalOpen(false);
        } catch (error) {
            setStatus('error');
            if (axios.isAxiosError(error)) {
                const backendMsg = error.response?.data?.message || error.response?.data;
                if (error.response?.status === 409 && backendMsg === 'error.password.already.used') {
                    setErrorMessage(t('error.password.already.used'));
                } else {
                    setErrorMessage(backendMsg || t("passwordReset.confirm.errorExpired"));
                }
            } else if (error instanceof Error) {
                setErrorMessage(error.message);
            } else {
                setErrorMessage(t("passwordReset.confirm.errorUnexpected"));
            }
            setIsConfirmModalOpen(false);
        }
    };

    if (!token) {
        return (
            <motion.div initial={{ opacity: 0, scale: 0.95 }} animate={{ opacity: 1, scale: 1 }} className="w-full max-w-[440px] flex flex-col justify-center text-center">
                <div className="w-20 h-20 mx-auto bg-danger-subtle text-danger rounded-full flex items-center justify-center mb-6 shadow-inner border border-danger-border">
                    <AlertCircle size={40}/>
                </div>
                <h2 className="text-3xl font-black text-primary tracking-tight mb-3">{t("passwordReset.confirm.invalidLinkTitle")}</h2>
                <p className="text-base text-secondary mb-10 max-w-sm mx-auto">
                    {t("passwordReset.confirm.invalidLinkDesc")}
                </p>
                <Link
                    to={PATHS.RESET_PASSWORD}
                    className="inline-block w-full bg-brand text-white font-black py-4 rounded-2xl hover:bg-brand-hover transition-colors shadow-lg tracking-widest uppercase text-sm"
                >
                    {t("passwordReset.confirm.requestNewLink")}
                </Link>
            </motion.div>
        );
    }

    if (status === 'success') {
        return (
            <motion.div initial={{ opacity: 0, scale: 0.95 }} animate={{ opacity: 1, scale: 1 }} className="w-full max-w-[440px] flex flex-col justify-center text-center">
                <div className="w-20 h-20 mx-auto bg-green-500/10 text-green-600 rounded-full flex items-center justify-center mb-6 shadow-inner border border-green-500/20">
                    <CheckCircle2 size={40}/>
                </div>
                <h2 className="text-3xl font-black text-primary tracking-tight mb-3">{t("passwordReset.confirm.successTitle")}</h2>
                <p className="text-base text-secondary mb-10 max-w-sm mx-auto">
                    {t("passwordReset.confirm.successDesc")}
                </p>
                <Link
                    to={PATHS.LOGIN}
                    className="inline-block w-full bg-brand text-white font-black py-4 rounded-2xl hover:bg-brand-hover transition-colors shadow-lg tracking-widest uppercase text-sm"
                >
                    {t("passwordReset.confirm.returnToLogin")}
                </Link>
            </motion.div>
        );
    }

    return (
        <motion.div initial={{ opacity: 0, scale: 0.98 }} animate={{ opacity: 1, scale: 1 }} transition={{ duration: 0.5, ease: "easeOut" }} className="w-full max-w-[440px]">
            <div className="mb-10 text-center lg:text-left">
                <h2 className="text-3xl md:text-4xl font-black text-primary tracking-tight mb-3">{t("passwordReset.confirm.title")}</h2>
                <p className="text-base text-secondary">
                    {t("passwordReset.confirm.subtitle")}
                </p>
            </div>

            {status === 'error' && (
                <motion.div initial={{ opacity: 0, y: -10 }} animate={{ opacity: 1, y: 0 }} className="mb-8 p-4 bg-danger-subtle text-danger text-sm font-bold rounded-2xl border border-danger-border flex items-start gap-2 shadow-sm">
                    <AlertCircle size={20} className="shrink-0"/>
                    <span>{errorMessage}</span>
                </motion.div>
            )}

            <form onSubmit={handleSubmit(onFormSubmit)} className="space-y-6">
                <div className="group">
                    <label htmlFor="newPassword" className="block text-[11px] font-black text-secondary tracking-widest mb-2 uppercase ml-1 transition-colors group-focus-within:text-primary">
                        {t("passwordReset.confirm.newPassword")}
                    </label>
                    <div className="relative border border-border rounded-xl focus-within:border-brand transition-all duration-300 focus-within:ring-2 focus-within:ring-brand/20 bg-surface">
                        <Lock className="absolute left-4 top-1/2 -translate-y-1/2 text-secondary opacity-60 transition-colors group-focus-within:text-brand" size={20}/>
                        <input
                            id="newPassword"
                            type="password"
                            placeholder="••••••••••••"
                            {...register("newPassword")}
                            className="w-full pl-12 pr-4 py-3.5 outline-none text-base bg-transparent text-primary tracking-widest font-medium disabled:opacity-50"
                            disabled={status === 'loading'}
                        />
                    </div>
                    {errors.newPassword?.message && (
                        <p className="text-danger text-xs mt-1.5 ml-1 font-medium">{t(errors.newPassword.message)}</p>
                    )}
                </div>

                <div className="group">
                    <label htmlFor="confirmPassword" className="block text-[11px] font-black text-secondary tracking-widest mb-2 uppercase ml-1 transition-colors group-focus-within:text-primary">
                        {t("passwordReset.confirm.confirmPassword")}
                    </label>
                    <div className="relative border border-border rounded-xl focus-within:border-brand transition-all duration-300 focus-within:ring-2 focus-within:ring-brand/20 bg-surface">
                        <Lock className="absolute left-4 top-1/2 -translate-y-1/2 text-secondary opacity-60 transition-colors group-focus-within:text-brand" size={20}/>
                        <input
                            id="confirmPassword"
                            type="password"
                            placeholder="••••••••••••"
                            {...register("confirmPassword")}
                            className="w-full pl-12 pr-4 py-3.5 outline-none text-base bg-transparent text-primary tracking-widest font-medium disabled:opacity-50"
                            disabled={status === 'loading'}
                        />
                    </div>
                    {errors.confirmPassword?.message && (
                        <p className="text-danger text-xs mt-1.5 ml-1 font-medium">{t(errors.confirmPassword.message)}</p>
                    )}
                </div>

                <motion.div whileHover={{ scale: 1.015 }} whileTap={{ scale: 0.98 }} className="pt-4">
                    <SubmitButton type="submit" isLoading={status === 'loading'} className="w-full py-4 shadow-lg shadow-brand/20 font-black text-base tracking-wide rounded-2xl transition-all">
                        {t("passwordReset.confirm.submit")}
                    </SubmitButton>
                </motion.div>
            </form>

            <div className="mt-12 pt-6 border-t border-border/50 text-center">
                <Link to={PATHS.LOGIN} className="inline-flex items-center text-sm font-bold text-secondary hover:text-primary transition-colors group">
                    <ArrowLeft size={16} className="mr-2 group-hover:-translate-x-1 transition-transform"/>
                    {t("passwordReset.confirm.backToLogin")}
                </Link>
            </div>

            <ConfirmationModal
                isOpen={isConfirmModalOpen}
                title={t('common.confirmPasswordResetTitle', 'Confirm Password Reset?')}
                description={t('common.confirmPasswordResetDesc', 'Are you sure you want to change your password? Once changed, you must use the new password to log in.')}
                confirmText={t('passwordReset.confirm.submit')}
                onConfirm={handleConfirmChange}
                onCancel={() => setIsConfirmModalOpen(false)}
                isLoading={status === 'loading'}
            />
        </motion.div>
    );
}