import { useState } from 'react';
import { useForm } from 'react-hook-form';
import { yupResolver } from '@hookform/resolvers/yup';
import * as yup from 'yup';
import { useTranslation } from 'react-i18next';
import { ShieldAlert, Lock, AlertCircle } from 'lucide-react';
import { motion } from 'framer-motion';

import { passwordSchema } from '../../../shared/validators/passwordSchema.ts';
import { changeOwnPassword, getAccountByLogin } from '../../../services/accountService.ts';
import SubmitButton from '../../../shared/components/buttons/SubmitButton.tsx';
import { useAuth } from '../../../hooks/useAuth.ts';

const forcePasswordSchema = yup.object({
    oldPassword: yup.string().required("validation.required")
}).concat(passwordSchema);

const ForcePasswordChangePage = () => {
    const { t } = useTranslation();
    const [serverError, setServerError] = useState<string | null>(null);
    const auth = useAuth();

    const {
        register,
        handleSubmit,
        formState: { errors, isSubmitting },
    } = useForm({
        resolver: yupResolver(forcePasswordSchema),
    });

    const onSubmit = async (data: any) => {
        setServerError(null);
        try {
            const login = auth.userLogin;

            if (!login) throw new Error("Hook useAuth nie zwrócił loginu. Sprawdź stan w konsoli.");

            const freshAccountResponse = await getAccountByLogin(login);
            const responseData = freshAccountResponse?.data ? freshAccountResponse.data : freshAccountResponse;
            const actualAccountData = responseData?.account || responseData;
            const version = actualAccountData?.version ?? actualAccountData?.versionHash ?? "";

            await changeOwnPassword({
                oldPassword: data.oldPassword,
                newPassword: data.newPassword,
            }, String(version));

            if (auth && auth.logout) {
                auth.logout();
            } else {
                localStorage.clear();
            }
            window.location.href = '/';

        } catch (err: any) {
            console.error("BŁĄD ZMIANY HASŁA", err);
            if (err.response) {
                const status = err.response.status;
                const responseData = err.response.data;

                if (status === 400) {
                    if (typeof responseData === 'object') {
                        if (responseData.errors) {
                            const msgs = Object.values(responseData.errors).map(msg => t(String(msg)));
                            setServerError(`${t('error.validation')}: ${msgs.join(' | ')}`);
                        } else if (responseData.message) {
                            setServerError(t(String(responseData.message)));
                        } else {
                            setServerError(t('error.passwordRequirements'));
                        }
                    } else {
                        setServerError(`${t('error.rejected')}: ${responseData}`);
                    }
                }
                else if (status === 409) {
                    const backendMessage = responseData?.message || responseData;
                    if (backendMessage === 'error.concurrent.update') {
                        setServerError(t('error.concurrent.update', 'Błąd wersji. Odśwież stronę i spróbuj ponownie.'));
                    } else if (backendMessage === 'error.password.same.as.old') {
                        setServerError(t('error.password.same.as.old', 'Nowe hasło nie może być takie samo jak stare!'));
                    } else {
                        setServerError(t(String(backendMessage)) || t('error.changePasswordFailed'));
                    }
                }
                else {
                    const backendMessage = responseData?.message || responseData;
                    if (typeof backendMessage === 'string') {
                        setServerError(t(backendMessage));
                    } else {
                        setServerError(t('error.changePasswordFailed', 'Nie udało się zmienić hasła.'));
                    }
                }
            } else {
                setServerError(`${t('error.communication')}: ` + err.message);
            }
        }
    };

    return (
        <div className="min-h-screen flex items-center justify-center bg-base p-6 md:p-12">
            <motion.div
                initial={{ opacity: 0, scale: 0.98 }}
                animate={{ opacity: 1, scale: 1 }}
                transition={{ duration: 0.5, ease: "easeOut" }}
                className="w-full max-w-[480px] bg-surface p-10 md:p-12 rounded-3xl border border-border shadow-2xl"
            >
                <div className="text-center mb-10">
                    <div className="w-16 h-16 mx-auto bg-amber-500/10 text-amber-500 rounded-full flex items-center justify-center mb-6 shadow-inner border border-amber-500/20">
                        <ShieldAlert size={32} />
                    </div>
                    <h2 className="text-3xl font-black text-primary tracking-tight mb-3">
                        {t('force_password_change.title')}
                    </h2>
                    <p className="text-sm text-secondary">
                        {t('force_password_change.description')}
                    </p>
                </div>

                {serverError && (
                    <motion.div initial={{ opacity: 0, y: -10 }} animate={{ opacity: 1, y: 0 }} className="mb-8 p-4 bg-danger-subtle text-danger text-sm font-bold rounded-2xl border border-danger-border flex items-start gap-2 shadow-sm">
                        <AlertCircle size={20} className="shrink-0" />
                        <span>{serverError}</span>
                    </motion.div>
                )}

                <form className="space-y-6" onSubmit={handleSubmit(onSubmit)}>
                    <div className="group">
                        <label className="block text-[11px] font-black text-secondary tracking-widest mb-2 uppercase ml-1 transition-colors group-focus-within:text-primary">
                            {t('password.old')}
                        </label>
                        <div className="relative border border-border rounded-xl focus-within:border-brand transition-all duration-300 focus-within:ring-2 focus-within:ring-brand/20 bg-base/50">
                            <Lock className="absolute left-4 top-1/2 -translate-y-1/2 text-secondary opacity-60 transition-colors group-focus-within:text-brand" size={20} />
                            <input
                                {...register('oldPassword')}
                                type="password"
                                placeholder="••••••••"
                                className="w-full pl-12 pr-4 py-3.5 outline-none text-base bg-transparent tracking-widest text-primary font-medium"
                            />
                        </div>
                        {errors.oldPassword && <p className="text-danger text-xs mt-1.5 ml-1 font-medium">{t(errors.oldPassword.message as string)}</p>}
                    </div>

                    <div className="group">
                        <label className="block text-[11px] font-black text-secondary tracking-widest mb-2 uppercase ml-1 transition-colors group-focus-within:text-primary">
                            {t('password.new')}
                        </label>
                        <div className="relative border border-border rounded-xl focus-within:border-brand transition-all duration-300 focus-within:ring-2 focus-within:ring-brand/20 bg-base/50">
                            <Lock className="absolute left-4 top-1/2 -translate-y-1/2 text-secondary opacity-60 transition-colors group-focus-within:text-brand" size={20} />
                            <input
                                {...register('newPassword')}
                                type="password"
                                placeholder="••••••••"
                                className="w-full pl-12 pr-4 py-3.5 outline-none text-base bg-transparent tracking-widest text-primary font-medium"
                            />
                        </div>
                        {errors.newPassword && <p className="text-danger text-xs mt-1.5 ml-1 font-medium">{t(errors.newPassword.message as string)}</p>}
                    </div>

                    <div className="group">
                        <label className="block text-[11px] font-black text-secondary tracking-widest mb-2 uppercase ml-1 transition-colors group-focus-within:text-primary">
                            {t('password.confirm')}
                        </label>
                        <div className="relative border border-border rounded-xl focus-within:border-brand transition-all duration-300 focus-within:ring-2 focus-within:ring-brand/20 bg-base/50">
                            <Lock className="absolute left-4 top-1/2 -translate-y-1/2 text-secondary opacity-60 transition-colors group-focus-within:text-brand" size={20} />
                            <input
                                {...register('confirmPassword')}
                                type="password"
                                placeholder="••••••••"
                                className="w-full pl-12 pr-4 py-3.5 outline-none text-base bg-transparent tracking-widest text-primary font-medium"
                            />
                        </div>
                        {errors.confirmPassword && <p className="text-danger text-xs mt-1.5 ml-1 font-medium">{t(errors.confirmPassword.message as string)}</p>}
                    </div>

                    <motion.div whileHover={{ scale: 1.015 }} whileTap={{ scale: 0.98 }} className="pt-4">
                        <SubmitButton isLoading={isSubmitting} className="w-full py-4 shadow-lg shadow-brand/20 font-black text-base tracking-wide rounded-2xl transition-all">
                            {t('password.change_button')}
                        </SubmitButton>
                    </motion.div>
                </form>
            </motion.div>
        </div>
    );
};

export default ForcePasswordChangePage;