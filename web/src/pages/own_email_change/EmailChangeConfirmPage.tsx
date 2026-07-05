import {useState} from "react";
import {useSearchParams} from "react-router-dom";
import {AlertCircle, ArrowLeft, CheckCircle2, Lock, Mail} from "lucide-react";
import {useForm} from "react-hook-form";
import {yupResolver} from "@hookform/resolvers/yup";
import axios from "axios";
import {useTranslation} from "react-i18next";

import {PATHS} from "../../routes/paths.ts";
import {emailChangeService} from "../../services/emailChangeService.ts";
import SubmitButton from "../../shared/components/buttons/SubmitButton.tsx";
import LinkButton from "../../shared/components/buttons/LinkButton.tsx";
import {type EmailChangeFormData, emailChangeSchema} from "../../shared/validators/emailChangeSchema.ts";
import {useAuth} from "../../hooks/useAuth.ts";
import ConfirmationModal from "../../shared/components/modals/ConfirmationPopup.tsx";

export default function EmailChangeConfirmPage() {
    const {t} = useTranslation();
    const [searchParams] = useSearchParams();
    const token = searchParams.get("token");
    const {logout} = useAuth();

    const [status, setStatus] = useState<'idle' | 'loading' | 'success' | 'error'>('idle');
    const [errorMessage, setErrorMessage] = useState("");

    const [isConfirmModalOpen, setIsConfirmModalOpen] = useState(false);
    const [pendingData, setPendingData] = useState<EmailChangeFormData | null>(null);

    const {
        register,
        handleSubmit,
        formState: {errors}
    } = useForm<EmailChangeFormData>({
        resolver: yupResolver(emailChangeSchema)
    });

    const onFormSubmit = (data: EmailChangeFormData) => {
        setPendingData(data);
        setIsConfirmModalOpen(true);
    };

    const handleConfirmChange = async () => {
        if (!pendingData) return;

        setStatus('loading');
        setErrorMessage("");

        try {
            await emailChangeService.confirmEmailChange(token!, pendingData.password, pendingData.newEmail);
            setStatus('success');
            setIsConfirmModalOpen(false);
            logout();
        } catch (error) {
            console.error("Failed to change email", error);
            setStatus('error');
            if (axios.isAxiosError(error)) {
                if (error.response?.status === 401) {
                    setErrorMessage(t('emailChange.confirm.error.wrongCredentials'));
                } else {
                    setErrorMessage(error.response?.data?.message || t('emailChange.confirm.error.expired'));
                }
            } else {
                setErrorMessage(t('emailChange.confirm.error.unexpected'));
            }
            setIsConfirmModalOpen(false);
        }
    };

    if (!token) {
        return (
            <div
                className="w-full flex flex-col justify-center animate-in fade-in slide-in-from-bottom-4 duration-500 text-center">
                <div className="flex justify-center mb-4 text-danger">
                    <AlertCircle size={48}/>
                </div>
                <h2 className="text-3xl font-bold text-primary mb-2">{t('emailChange.confirm.invalidLink.title')}</h2>
                <p className="text-sm text-secondary mb-8 max-w-sm mx-auto">
                    {t('emailChange.confirm.invalidLink.description')}
                </p>
                <LinkButton to={PATHS.OWN_EMAIL_CHANGE_MAIN} className="max-w-sm mx-auto">
                    {t('emailChange.confirm.backButton')}
                </LinkButton>
            </div>
        );
    }

    if (status === 'success') {
        return (
            <div
                className="w-full flex flex-col justify-center animate-in fade-in slide-in-from-bottom-4 duration-500 text-center">
                <div className="flex justify-center mb-4 text-green-600 dark:text-green-500">
                    <CheckCircle2 size={48}/>
                </div>
                <h2 className="text-3xl font-bold text-primary mb-2">{t('emailChange.confirm.success.title')}</h2>
                <p className="text-sm text-secondary mb-8 max-w-sm mx-auto">
                    {t('emailChange.confirm.success.description')}
                </p>
                <LinkButton to={PATHS.LOGIN} className="max-w-sm mx-auto">
                    {t('emailChange.confirm.backButton')}
                </LinkButton>
            </div>
        );
    }

    return (
        <div className="w-full flex flex-col justify-center animate-in fade-in slide-in-from-bottom-4 duration-500">
            <h2 className="text-3xl font-bold text-primary mb-2">{t('emailChange.confirm.form.title')}</h2>
            <p className="text-sm text-secondary mb-8">
                {t('emailChange.confirm.form.subtitle')}
            </p>

            {status === 'error' && (
                <div
                    className="mb-6 p-3 bg-danger-subtle text-danger text-sm rounded-md flex items-start gap-2 border border-danger-border">
                    <AlertCircle size={16} className="mt-0.5 flex-shrink-0"/>
                    <span>{errorMessage}</span>
                </div>
            )}

            <form onSubmit={handleSubmit(onFormSubmit)} className="w-full max-w-sm mx-auto">
                <div className="mb-6">
                    <label htmlFor="newEmail"
                           className="block text-xs font-bold text-secondary uppercase tracking-wider mb-2">
                        {t('emailChange.confirm.form.newEmailLabel')}
                    </label>
                    <div className="relative">
                        <div
                            className="absolute inset-y-0 left-0 flex items-center pointer-events-none text-secondary opacity-70">
                            <Mail size={18}/>
                        </div>
                        <input
                            id="newEmail"
                            type="email"
                            placeholder="6sigma7@edu.p.lodz.pl"
                            {...register("newEmail")}
                            className={`w-full border-b py-2 pl-8 focus:outline-none transition-colors bg-transparent text-sm text-primary disabled:opacity-50 ${errors.newEmail ? 'border-danger focus:border-danger' : 'border-border focus:border-brand'}`}
                            disabled={status === 'loading'}
                        />
                    </div>
                    {errors.newEmail?.message && (
                        <p className="text-danger text-xs mt-1">{t(errors.newEmail.message)}</p>
                    )}
                </div>

                <div className="mb-8">
                    <label htmlFor="password"
                           className="block text-xs font-bold text-secondary uppercase tracking-wider mb-2">
                        {t('emailChange.confirm.form.passwordLabel')}
                    </label>
                    <div className="relative">
                        <div
                            className="absolute inset-y-0 left-0 flex items-center pointer-events-none text-secondary opacity-70">
                            <Lock size={18}/>
                        </div>
                        <input
                            id="password"
                            type="password"
                            placeholder="••••••••"
                            {...register("password")}
                            className={`w-full border-b py-2 pl-8 focus:outline-none transition-colors bg-transparent text-sm text-primary disabled:opacity-50 ${errors.password ? 'border-danger focus:border-danger' : 'border-border focus:border-brand'}`}
                            disabled={status === 'loading'}
                        />
                    </div>
                    {errors.password?.message && (
                        <p className="text-danger text-xs mt-1">{t(errors.password.message)}</p>
                    )}
                </div>

                <SubmitButton
                    type="submit"
                    isLoading={status === 'loading'}
                >
                    {t('emailChange.confirm.form.submitButton')}
                </SubmitButton>
            </form>

            <div className="mt-8 text-center max-w-sm mx-auto">
                <LinkButton to={PATHS.USER_LIST} variant="ghost">
                    <ArrowLeft size={16} className="mr-2"/>
                    {t('emailChange.confirm.backButton')}
                </LinkButton>
            </div>

            <ConfirmationModal
                isOpen={isConfirmModalOpen}
                title={t('common.confirmEmailChangeTitle')}
                description={t('common.confirmEmailChangeDesc')}
                confirmText={t('emailChange.confirm.form.submitButton')}
                onConfirm={handleConfirmChange}
                onCancel={() => setIsConfirmModalOpen(false)}
                isLoading={status === 'loading'}
            />
        </div>
    );
}