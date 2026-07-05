import React, { useState, useEffect } from "react";
import { useTranslation } from "react-i18next";
import { useNavigate, useLocation, Navigate, Link } from "react-router-dom";
import { ShieldCheck, KeyRound, AlertCircle, RefreshCw, ArrowLeft } from "lucide-react";
import { useForm } from "react-hook-form";
import { yupResolver } from "@hookform/resolvers/yup";
import axios from "axios";
import { motion } from "framer-motion";

import { getDashboardPath, PATHS } from "../../../routes/paths.ts";
import { type JwtPayload, useAuth } from "../../../hooks/useAuth.ts";
import { authService } from "../../../services/authService.ts";
import SubmitButton from "../../../shared/components/buttons/SubmitButton.tsx";
import { _2faSchema, type _2faFormData } from "../../../shared/validators/2FASchema.ts";
import { jwtDecode } from "jwt-decode";

export default function TwoFactorVerifyPage() {
    const { t } = useTranslation();
    const navigate = useNavigate();
    const location = useLocation();
    const { setTokens } = useAuth();

    const userLogin = location.state?.login;

    const [globalError, setGlobalError] = useState<string | null>(null);
    const [successMessage, setSuccessMessage] = useState<string | null>(null);
    const [isLoading, setIsLoading] = useState(false);
    const [isResending, setIsResending] = useState(false);
    const [countdown, setCountdown] = useState(60);

    const { register, handleSubmit, formState: { errors } } = useForm<_2faFormData>({
        resolver: yupResolver(_2faSchema),
        defaultValues: { auth2F: "" }
    });

    useEffect(() => {
        if (countdown <= 0) return;
        const timer = setInterval(() => setCountdown((prev) => prev - 1), 1000);
        return () => clearInterval(timer);
    }, [countdown]);

    if (!userLogin) return <Navigate to={PATHS.LOGIN} replace />;

    const onSubmit = async (data: _2faFormData) => {
        setGlobalError(null);
        setSuccessMessage(null);
        setIsLoading(true);
        try {
            const response = await authService.verify2FA(userLogin, data.auth2F);
            setTokens(response.token, response.refreshToken);
            const decoded = jwtDecode<JwtPayload>(response.token);
            const newRole = decoded.roles?.includes("ADMIN") ? "ADMIN" : decoded.roles?.[0];
            const targetPath = location.state?.from?.pathname || getDashboardPath(newRole);
            navigate(targetPath, { replace: true });
        } catch (err) {
            if (axios.isAxiosError(err)) {
                setGlobalError(t("auth.2fa.code.invalid"));
            } else {
                setGlobalError(t("auth.2fa.code.unexpectedError"));
            }
        } finally {
            setIsLoading(false);
        }
    };

    const handleResendCode = async () => {
        setGlobalError(null);
        setSuccessMessage(null);
        setIsResending(true);
        try {
            await authService.resend2FA(userLogin);
            setSuccessMessage(t("auth.2fa.code.retry.success"));
            setCountdown(60);
        } catch (err) {
            if (axios.isAxiosError(err)) setGlobalError(t("auth.2fa.code.retry.fail"));
        } finally {
            setIsResending(false);
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
                <div className="flex items-center justify-center lg:justify-start gap-3 mb-4">
                    <div className="w-12 h-12 bg-brand/10 text-brand rounded-full flex items-center justify-center shadow-inner border border-brand/20">
                        <ShieldCheck size={24} strokeWidth={2.5} />
                    </div>
                </div>
                <h2 className="text-3xl md:text-4xl font-black text-primary tracking-tight mb-3">
                    {t("auth.2fa.heading")}
                </h2>
                <p className="text-base text-secondary">
                    {t("auth.2fa.description")}
                </p>
            </div>

            {globalError && (
                <motion.div initial={{ opacity: 0, y: -10 }} animate={{ opacity: 1, y: 0 }} className="mb-8 p-4 bg-danger-subtle text-danger text-sm font-bold rounded-2xl border border-danger-border flex items-start gap-2 shadow-sm">
                    <AlertCircle size={20} className="shrink-0" />
                    <span>{globalError}</span>
                </motion.div>
            )}

            {successMessage && (
                <motion.div initial={{ opacity: 0, y: -10 }} animate={{ opacity: 1, y: 0 }} className="mb-8 p-4 bg-green-500/10 text-green-600 text-sm font-bold rounded-2xl border border-green-500/20 shadow-sm">
                    {successMessage}
                </motion.div>
            )}

            <form onSubmit={handleSubmit(onSubmit)} className="space-y-6">
                <div className="group">
                    <label className="block text-[11px] font-black text-secondary tracking-widest mb-2 uppercase ml-1 transition-colors group-focus-within:text-primary">
                        {t("auth.2fa.inputLabel")}
                    </label>
                    <div className="relative border border-border rounded-xl focus-within:border-brand transition-all duration-300 focus-within:ring-2 focus-within:ring-brand/20 bg-surface">
                        <KeyRound className="absolute left-4 top-1/2 -translate-y-1/2 text-secondary opacity-60 transition-colors group-focus-within:text-brand" size={20} />
                        <input
                            type="text"
                            inputMode="numeric"
                            placeholder="00000000"
                            maxLength={8}
                            pattern="[0-9]*"
                            autoComplete="one-time-code"
                            className="w-full pl-12 pr-4 py-3.5 outline-none text-xl tracking-[0.3em] font-mono font-bold bg-transparent text-primary placeholder:text-secondary/30 disabled:opacity-50"
                            {...register("auth2F", {
                                onChange: (e: React.ChangeEvent<HTMLInputElement>) => {
                                    e.target.value = e.target.value.replace(/[^0-9]/g, '');
                                }
                            })}
                        />
                    </div>
                    {errors.auth2F && <p className="text-danger text-xs mt-1.5 ml-1 font-medium">{errors.auth2F.message}</p>}
                </div>

                <motion.div whileHover={{ scale: 1.015 }} whileTap={{ scale: 0.98 }} className="pt-2">
                    <SubmitButton type="submit" isLoading={isLoading} className="w-full py-4 shadow-lg shadow-brand/20 font-black text-base tracking-wide rounded-2xl transition-all">
                        {t("auth.2fa.submit")}
                    </SubmitButton>
                </motion.div>

                <div className="flex flex-col items-center gap-6 mt-8">
                    <button
                        type="button"
                        onClick={handleResendCode}
                        disabled={isResending || countdown > 0}
                        className="text-sm font-bold text-secondary hover:text-brand transition-colors flex items-center gap-2 disabled:opacity-50 disabled:cursor-not-allowed disabled:hover:text-secondary"
                    >
                        <RefreshCw size={16} className={isResending ? "animate-spin" : ""} />
                        {countdown > 0 ? `${t("auth.2fa.resend")} (${countdown}s)` : t("auth.2fa.resend")}
                    </button>
                </div>
            </form>

            <div className="mt-10 pt-6 border-t border-border/50 text-center">
                <Link to={PATHS.LOGIN} className="inline-flex items-center text-sm font-bold text-secondary hover:text-primary transition-colors group">
                    <ArrowLeft size={16} className="mr-2 group-hover:-translate-x-1 transition-transform" />
                    {t("auth.2fa.cancel")}
                </Link>
            </div>
        </motion.div>
    );
}