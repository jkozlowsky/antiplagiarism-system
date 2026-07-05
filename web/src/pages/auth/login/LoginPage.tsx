import { useState } from "react";
import { useTranslation } from "react-i18next";
import { Link, useNavigate, useLocation } from "react-router-dom";
import { User, Lock, ShieldCheck } from "lucide-react";
import { useForm } from "react-hook-form";
import { yupResolver } from "@hookform/resolvers/yup";
import axios from "axios";
import { GoogleLogin } from '@react-oauth/google';
import { motion } from "framer-motion";

import { getDashboardPath, PATHS } from "../../../routes/paths.ts";
import { useAuth } from "../../../hooks/useAuth.ts";
import { authService, loginWithGoogle } from "../../../services/authService.ts";
import SubmitButton from "../../../shared/components/buttons/SubmitButton.tsx";
import { loginSchema, type LoginFormData } from "../../../shared/validators/loginSchema.ts";
import { jwtDecode } from "jwt-decode";
import type { JwtPayload } from "../../../hooks/useAuth.ts";

export default function LoginPage() {
    const { t } = useTranslation();
    const navigate = useNavigate();
    const location = useLocation();
    const { setTokens } = useAuth();
    const [globalError, setGlobalError] = useState<string | null>(null);
    const [isLoading, setIsLoading] = useState(false);

    const { register, handleSubmit, formState: { errors } } = useForm<LoginFormData>({
        resolver: yupResolver(loginSchema),
        defaultValues: { login: "", password: "", rememberMe: false }
    });

    const onSubmit = async (data: LoginFormData) => {
        setGlobalError(null);
        setIsLoading(true);
        try {
            const { response, status } = await authService.login(data.login, data.password);
            if (response?.message === "mfa.required") {
                navigate(PATHS._2FA_VERIFY, {
                    state: { login: data.login, from: location.state?.from }
                });
            } else {
                if (status === 202) {
                    setGlobalError(t('auth.login.errorInvalid'));
                } else if (typeof response === 'string') {
                    setGlobalError(response);
                } else {
                    setGlobalError(t('auth.login.errorInvalid'));
                }
            }
        } catch (err) {
            if (axios.isAxiosError(err)) {
                if (err.response?.status === 202) {
                    setGlobalError(t('auth.login.errorInvalid'));
                } else if (err.response?.status === 409) {
                    setGlobalError(t('auth.login.errorBlocked'));
                } else {
                    setGlobalError(err.response?.data?.message || t('auth.login.errorGeneric'));
                }
            } else {
                setGlobalError(t('auth.login.errorGeneric'));
            }
        } finally {
            setIsLoading(false);
        }
    };

    const handleGoogleLogin = async (credential: string) => {
        setGlobalError(null);
        setIsLoading(true);
        try {
            const response = await loginWithGoogle(credential);
            if (response?.token) {
                setTokens(response.token, response.refreshToken);
                const decoded = jwtDecode<JwtPayload>(response.token);
                const newRole = decoded.roles?.includes("ADMIN") ? "ADMIN" : decoded.roles?.[0];
                const targetPath = location.state?.from?.pathname || getDashboardPath(newRole);
                navigate(targetPath, { replace: true });
            }
        } catch {
            setGlobalError(t('auth.login.errorGoogle'));
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
                    {t('auth.login.heading', 'Witaj ponownie')}
                </h2>
                <p className="text-base text-secondary">
                    {t('auth.login.subheading', 'Zaloguj się, aby zarządzać analizami kodu.')}
                </p>
            </div>

            {globalError && (
                <motion.div
                    id="loginErrorBox"
                    initial={{ opacity: 0, y: -10 }} animate={{ opacity: 1, y: 0 }} className="mb-8 p-4 bg-danger-subtle text-danger text-sm font-bold rounded-2xl border border-danger-border flex items-center justify-center gap-2 shadow-sm">
                    {globalError}
                </motion.div>
            )}

            <form onSubmit={handleSubmit(onSubmit)} className="space-y-6">
                <div className="group">
                    <label className="block text-[11px] font-black text-secondary tracking-widest mb-2 uppercase ml-1 transition-colors group-focus-within:text-primary">
                        {t('auth.login.loginLabel')}
                    </label>
                    <div className="relative border border-border rounded-xl focus-within:border-brand transition-all duration-300 focus-within:ring-2 focus-within:ring-brand/20 bg-surface">
                        <User className="absolute left-4 top-1/2 -translate-y-1/2 text-secondary opacity-60 transition-colors group-focus-within:text-brand" size={20}/>
                        <input
                            type="text"
                            placeholder={t('auth.login.loginPlaceholder')}
                            className="w-full pl-12 pr-4 py-3.5 outline-none text-base bg-transparent text-primary placeholder:text-secondary/50 font-medium"
                            {...register("login")}
                        />
                    </div>
                    {errors.login?.message && <p className="text-danger text-xs mt-1.5 ml-1 font-medium">{t(errors.login.message)}</p>}
                </div>

                <div className="group">
                    <label className="block text-[11px] font-black text-secondary tracking-widest mb-2 uppercase ml-1 transition-colors group-focus-within:text-primary">
                        {t('auth.login.passwordLabel')}
                    </label>
                    <div className="relative border border-border rounded-xl focus-within:border-brand transition-all duration-300 focus-within:ring-2 focus-within:ring-brand/20 bg-surface">
                        <Lock className="absolute left-4 top-1/2 -translate-y-1/2 text-secondary opacity-60 transition-colors group-focus-within:text-brand" size={20}/>
                        <input
                            type="password"
                            placeholder="••••••••"
                            className="w-full pl-12 pr-4 py-3.5 outline-none text-base bg-transparent tracking-widest text-primary placeholder:tracking-normal placeholder:text-secondary/50 font-medium"
                            {...register("password")}
                        />
                    </div>
                    {errors.password?.message && <p className="text-danger text-xs mt-1.5 ml-1 font-medium">{t(errors.password.message)}</p>}
                </div>

                <div className="flex items-center justify-between pt-2 pb-4 ml-1">
                    {/*<Controller*/}
                    {/*    name="rememberMe"*/}
                    {/*    control={control}*/}
                    {/*    render={({ field }) => (*/}
                    {/*        <div className="flex items-center gap-3 cursor-pointer group select-none py-1" onClick={() => field.onChange(!field.value)}>*/}
                    {/*            <div className={`transition-colors ${field.value ? "text-brand" : "text-secondary opacity-50 group-hover:opacity-100"}`}>*/}
                    {/*                {field.value ? <CheckSquare size={20}/> : <Square size={20}/>}*/}
                    {/*            </div>*/}
                    {/*            <p className="text-sm text-secondary font-bold transition-colors group-hover:text-primary">*/}
                    {/*                {t('auth.login.rememberMe')}*/}
                    {/*            </p>*/}
                    {/*        </div>*/}
                    {/*    )}*/}
                    {/*/>*/}
                    <Link to={PATHS.FORGOT_PASSWORD} className="text-sm font-bold text-brand hover:text-brand-hover transition-colors hover:underline py-1">
                        {t('auth.login.forgotPassword')}
                    </Link>
                </div>

                <motion.div
                    id="loginSubmitBtn"
                    whileHover={{ scale: 1.015 }} whileTap={{ scale: 0.98 }}>
                    <SubmitButton type="submit" isLoading={isLoading} className="w-full py-4 shadow-lg shadow-brand/20 font-black text-base tracking-wide rounded-2xl transition-all">
                        {t('auth.login.button')}
                    </SubmitButton>
                </motion.div>
                {/*<SubmitButton id="loginSubmitBtn" type="submit" isLoading={isLoading} className="mt-2 tracking-wide">*/}
                {/*    {t('auth.login.button')}*/}
                {/*</SubmitButton>*/}

                <div className="mt-8">
                    <div className="relative">
                        <div className="absolute inset-0 flex items-center"><div className="w-full border-t border-border"></div></div>
                        <div className="relative flex justify-center text-sm font-medium">
                            <span className="px-4 bg-base text-secondary/60">
                                {t('auth.login.orLoginWith')}
                            </span>
                        </div>
                    </div>

                    <div className="mt-6 flex justify-center">
                        <GoogleLogin
                            onSuccess={(res) => { if (res.credential) handleGoogleLogin(res.credential); }}
                            onError={() => setGlobalError(t('auth.login.errorGoogle'))}
                            useOneTap shape="pill" theme="outline" size="large"
                        />
                    </div>
                </div>

                <div className="text-center mt-12 pt-6 border-t border-border/50">
                    <p className="text-sm text-secondary font-medium mb-2">{t('auth.login.noAccount')}</p>
                    <Link to={PATHS.REGISTER} className="text-brand hover:text-brand-hover transition-colors text-sm font-black tracking-widest uppercase hover:underline">
                        {t('auth.login.createAccount')}
                    </Link>
                </div>
            </form>
        </motion.div>
    );
}