import { useState } from "react";
import { useTranslation } from "react-i18next";
import { Link } from "react-router-dom";
import { User, Mail, Lock, IdCard, MailCheck, ShieldCheck } from "lucide-react";
import { useForm } from "react-hook-form";
import { yupResolver } from "@hookform/resolvers/yup";
import { motion } from "framer-motion";

import { PATHS } from "../../../routes/paths.ts";
import axiosInstance from "../../../api/auth/middleware.ts";
import LinkButton from "../../../shared/components/buttons/LinkButton.tsx";
import SubmitButton from "../../../shared/components/buttons/SubmitButton.tsx";
import { registerSchema, type RegisterFormData } from "../../../shared/validators/registerSchema.ts";

export default function RegisterPage() {
    const { t } = useTranslation();
    const [globalError, setGlobalError] = useState<string | null>(null);
    const [isLoading, setIsLoading] = useState(false);
    const [isSuccess, setIsSuccess] = useState(false);

    const {
        register,
        handleSubmit,
        formState: { errors }
    } = useForm<RegisterFormData>({
        resolver: yupResolver(registerSchema),
        defaultValues: {
            name: "",
            surname: "",
            login: "",
            email: "",
            password: "",
            confirmPassword: ""
        }
    });

    const onSubmit = async (data: RegisterFormData) => {
        setGlobalError(null);
        setIsLoading(true);

        try {
            const { ...payload } = data;
            await axiosInstance.post("/account/register", payload);
            setIsSuccess(true);
        } catch (err: any) {
            const errorData = err.response?.data;
            if (errorData?.violations) {
                const messages = errorData.violations.map((v: any) => v.message).join(", ");
                setGlobalError(messages);
            } else {
                setGlobalError(t('error.unexpected'));
            }
            setIsLoading(false);
        }
    };

    if (isSuccess) {
        return (
            <motion.div
                initial={{ opacity: 0, scale: 0.95 }}
                animate={{ opacity: 1, scale: 1 }}
                className="flex flex-col items-center justify-center text-center w-full max-w-[440px]"
            >
                <div className="w-20 h-20 bg-brand/10 text-brand rounded-full flex items-center justify-center mb-6 shadow-inner border border-brand/20">
                    <MailCheck size={40} />
                </div>
                <h2 className="text-3xl font-black text-primary mb-4 tracking-tight">
                    {t('auth.register.successHeading')}
                </h2>
                <p className="text-base text-secondary mb-2 leading-relaxed">
                    {t('auth.register.successText')}
                </p>
                <p className="text-xs text-secondary/60 mb-10 italic">
                    {t('auth.register.spamNote')}
                </p>
                <LinkButton
                    to={PATHS.LOGIN}
                    className="w-full py-3.5 shadow-lg font-black text-sm tracking-widest uppercase rounded-2xl"
                >
                    {t('auth.register.backToLogin')}
                </LinkButton>
            </motion.div>
        );
    }

    return (
        <motion.div
            initial={{ opacity: 0, scale: 0.98 }}
            animate={{ opacity: 1, scale: 1 }}
            transition={{ duration: 0.5, ease: "easeOut" }}
            className="w-full max-w-[480px]"
        >
            <div className="mb-10 text-center lg:text-left">
                <div className="lg:hidden flex items-center justify-center gap-3 text-brand mb-6">
                    <ShieldCheck size={40} />
                    <span className="font-black text-3xl tracking-tight text-primary">Antyplagiat PŁ</span>
                </div>
                <h2 className="text-3xl md:text-4xl font-black text-primary tracking-tight mb-3">
                    {t('auth.register.heading')}
                </h2>
                <p className="text-base text-secondary">
                    {t('auth.register.subheading')}
                </p>
            </div>

            {globalError && (
                <motion.div
                    id="registerGlobalError"
                    initial={{ opacity: 0, y: -10 }} animate={{ opacity: 1, y: 0 }} className="mb-8 p-4 bg-danger-subtle text-danger text-sm font-bold rounded-2xl border border-danger-border flex items-center justify-center gap-2 shadow-sm">
                    {globalError}
                </motion.div>
            )}

            <form onSubmit={handleSubmit(onSubmit)} className="space-y-5">

                <div className="flex flex-col sm:flex-row gap-5">
                    <div className="w-full sm:w-1/2 group">
                        <label className="block text-[11px] font-black text-secondary tracking-widest mb-2 uppercase ml-1 transition-colors group-focus-within:text-primary">
                            {t('auth.register.name')}
                        </label>
                        <div className="relative border border-border rounded-xl focus-within:border-brand transition-all duration-300 focus-within:ring-2 focus-within:ring-brand/20 bg-surface">
                            <User className="absolute left-4 top-1/2 -translate-y-1/2 text-secondary opacity-60 transition-colors group-focus-within:text-brand" size={20}/>
                            <input
                                type="text"
                                placeholder="Jan"
                                className="w-full pl-11 pr-4 py-3 outline-none text-base bg-transparent text-primary placeholder:text-secondary/50 font-medium"
                                {...register("name")}
                            />
                        </div>
                        {errors.name?.message && <p className="text-danger text-[10px] mt-1.5 ml-1 font-medium">{t(errors.name.message)}</p>}
                    </div>

                    <div className="w-full sm:w-1/2 group">
                        <label className="block text-[11px] font-black text-secondary tracking-widest mb-2 uppercase ml-1 transition-colors group-focus-within:text-primary">
                            {t('auth.register.surname')}
                        </label>
                        <div className="relative border border-border rounded-xl focus-within:border-brand transition-all duration-300 focus-within:ring-2 focus-within:ring-brand/20 bg-surface">
                            <input
                                type="text"
                                placeholder="Kowalski"
                                className="w-full pl-4 pr-4 py-3 outline-none text-base bg-transparent text-primary placeholder:text-secondary/50 font-medium"
                                {...register("surname")}
                            />
                        </div>
                        {errors.surname?.message && <p className="text-danger text-[10px] mt-1.5 ml-1 font-medium">{t(errors.surname.message)}</p>}
                    </div>
                </div>

                <div className="group">
                    <label className="block text-[11px] font-black text-secondary tracking-widest mb-2 uppercase ml-1 transition-colors group-focus-within:text-primary">
                        {t('auth.register.login')}
                    </label>
                    <div className="relative border border-border rounded-xl focus-within:border-brand transition-all duration-300 focus-within:ring-2 focus-within:ring-brand/20 bg-surface">
                        <IdCard className="absolute left-4 top-1/2 -translate-y-1/2 text-secondary opacity-60 transition-colors group-focus-within:text-brand" size={20}/>
                        <input
                            type="text"
                            placeholder="jkowalski"
                            className="w-full pl-12 pr-4 py-3 outline-none text-base bg-transparent text-primary placeholder:text-secondary/50 font-medium"
                            {...register("login")}
                        />
                    </div>
                    {errors.login?.message && <p className="text-danger text-xs mt-1.5 ml-1 font-medium">{t(errors.login.message)}</p>}
                </div>

                <div className="group">
                    <label className="block text-[11px] font-black text-secondary tracking-widest mb-2 uppercase ml-1 transition-colors group-focus-within:text-primary">
                        {t('auth.register.email')}
                    </label>
                    <div className="relative border border-border rounded-xl focus-within:border-brand transition-all duration-300 focus-within:ring-2 focus-within:ring-brand/20 bg-surface">
                        <Mail className="absolute left-4 top-1/2 -translate-y-1/2 text-secondary opacity-60 transition-colors group-focus-within:text-brand" size={20}/>
                        <input
                            type="email"
                            placeholder="name@edu.p.lodz.pl"
                            className="w-full pl-12 pr-4 py-3 outline-none text-base bg-transparent text-primary placeholder:text-secondary/50 font-medium"
                            {...register("email")}
                        />
                    </div>
                    {errors.email?.message && <p className="text-danger text-xs mt-1.5 ml-1 font-medium">{t(errors.email.message)}</p>}
                </div>

                <div className="flex flex-col sm:flex-row gap-5">
                    <div className="w-full sm:w-1/2 group">
                        <label className="block text-[11px] font-black text-secondary tracking-widest mb-2 uppercase ml-1 transition-colors group-focus-within:text-primary">
                            {t('auth.register.password')}
                        </label>
                        <div className="relative border border-border rounded-xl focus-within:border-brand transition-all duration-300 focus-within:ring-2 focus-within:ring-brand/20 bg-surface">
                            <Lock className="absolute left-4 top-1/2 -translate-y-1/2 text-secondary opacity-60 transition-colors group-focus-within:text-brand" size={18}/>
                            <input
                                type="password"
                                placeholder="••••••••"
                                className="w-full pl-11 pr-4 py-3 outline-none text-base bg-transparent tracking-widest text-primary placeholder:tracking-normal placeholder:text-secondary/50"
                                {...register("password")}
                            />
                        </div>
                        {errors.password?.message ?
                            <p className="text-danger text-[10px] mt-1.5 ml-1 font-medium">{t(errors.password.message)}</p> :
                            <p className="text-[10px] text-secondary/70 mt-1.5 ml-1">{t('auth.register.passwordHelper')}</p>
                        }
                    </div>

                    <div className="w-full sm:w-1/2 group">
                        <label className="block text-[11px] font-black text-secondary tracking-widest mb-2 uppercase ml-1 transition-colors group-focus-within:text-primary">
                            {t('auth.register.confirmPassword')}
                        </label>
                        <div className="relative border border-border rounded-xl focus-within:border-brand transition-all duration-300 focus-within:ring-2 focus-within:ring-brand/20 bg-surface">
                            <Lock className="absolute left-4 top-1/2 -translate-y-1/2 text-secondary opacity-60 transition-colors group-focus-within:text-brand" size={18}/>
                            <input
                                type="password"
                                placeholder="••••••••"
                                className="w-full pl-11 pr-4 py-3 outline-none text-base bg-transparent tracking-widest text-primary placeholder:tracking-normal placeholder:text-secondary/50"
                                {...register("confirmPassword")}
                            />
                        </div>
                        {errors.confirmPassword?.message && <p className="text-danger text-[10px] mt-1.5 ml-1 font-medium">{t(errors.confirmPassword.message)}</p>}
                    </div>
                </div>

                <motion.div whileHover={{ scale: 1.015 }} whileTap={{ scale: 0.98 }} className="pt-4">
                    <SubmitButton
                        id="registerSubmitBtn"
                        type="submit"
                        isLoading={isLoading}
                        className="w-full py-4 shadow-lg shadow-brand/20 font-black text-base tracking-wide rounded-2xl transition-all"
                    >
                        {t('auth.register.button')}
                    </SubmitButton>
                </motion.div>

                <div className="text-center mt-6 pt-6 border-t border-border/50">
                    <p className="text-sm text-secondary font-medium">
                        {t('auth.register.haveAccount')}{' '}
                        <Link to={PATHS.LOGIN} className="text-brand font-black tracking-widest uppercase hover:text-brand-hover hover:underline transition-colors ml-1">
                            {t('auth.register.loginLink')}
                        </Link>
                    </p>
                </div>
            </form>
        </motion.div>
    );
}