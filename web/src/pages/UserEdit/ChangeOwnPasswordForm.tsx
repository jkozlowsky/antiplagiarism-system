import {useState} from 'react';
import {useForm} from 'react-hook-form';
import {yupResolver} from '@hookform/resolvers/yup';
import * as yup from 'yup';
import {useTranslation} from 'react-i18next';
import {changeOwnPassword} from '../../services/accountService.ts';
import SubmitButton from '../../shared/components/buttons/SubmitButton.tsx';
import axios from "axios";
import ConfirmationModal from "../../shared/components/modals/ConfirmationPopup.tsx";

const passwordChangeSchema = yup.object({
    oldPassword: yup.string()
        .required("validation.required"),
    newPassword: yup.string()
        .required("validation.required")
        .min(12, "validation.password.minLength")
        .matches(/[A-Z]/, "validation.password.uppercase")
        .matches(/[a-z]/, "validation.password.lowercase")
        .matches(/[0-9]/, "validation.password.number"),
    confirmPassword: yup.string()
        .required("validation.password.confirmRequired")
        .oneOf([yup.ref('newPassword')], "validation.password.mismatch")
}).required();

type PasswordChangeFormData = yup.InferType<typeof passwordChangeSchema>;

export interface ChangeOwnPasswordFormProps {
    version: string;
    onSuccess: () => void;
}

export function ChangeOwnPasswordForm({version, onSuccess}: ChangeOwnPasswordFormProps) {
    const {t} = useTranslation();

    const [pendingData, setPendingData] = useState<PasswordChangeFormData | null>(null);
    const [isLoading, setIsLoading] = useState(false);
    const [isModalOpen, setIsModalOpen] = useState(false);
    const [apiError, setApiError] = useState<string | null>(null);

    const {
        register,
        handleSubmit,
        reset,
        formState: {errors}
    } = useForm<PasswordChangeFormData>({
        resolver: yupResolver(passwordChangeSchema)
    });

    const onFormSubmit = (data: PasswordChangeFormData) => {
        setPendingData(data);
        setIsModalOpen(true);
    };

    const handleConfirmChange = async () => {
        if (!pendingData) return;

        setIsLoading(true);
        setApiError(null);

        try {
            await changeOwnPassword({
                    oldPassword: pendingData.oldPassword,
                    newPassword: pendingData.newPassword
                },
                version
            );

            reset();
            setIsModalOpen(false);
            onSuccess();
        } catch (error) {
            console.error("Failed to change password", error);
            if (axios.isAxiosError(error)) {
                const backendMsg = error.response?.data?.message || error.response?.data;
                if (error.response?.status === 409 && backendMsg === 'error.password.already.used') {
                    setApiError('error.password.already.used');
                } else {
                    setApiError(backendMsg || 'error.changePasswordFailed');
                }
            } else {
                setApiError('error.changePasswordFailed');
            }
        } finally {
            setIsLoading(false);
            setIsModalOpen(false);
        }
    };

    return (
        <form onSubmit={handleSubmit(onFormSubmit)} className="space-y-4 max-w-sm">
            {apiError && (
                <div className="p-3 bg-danger-subtle text-danger border border-danger-border rounded-md text-sm">
                    {t(apiError)}
                </div>
            )}

            <div>
                <input
                    id="oldPasswordInput"
                    type="password"
                    placeholder={t('profile.oldPassword')}
                    className={`w-full bg-surface text-primary border p-2 rounded shadow-sm focus:outline-none focus:ring-2 focus:ring-brand transition-colors ${errors.oldPassword ? 'border-danger' : 'border-border'}`}
                    {...register('oldPassword')}
                />
                {errors.oldPassword &&
                    <p className="text-danger text-xs mt-1">{t(errors.oldPassword.message as string)}</p>}
            </div>

            <div>
                <input
                    id="newPasswordInput"
                    type="password"
                    placeholder={t('profile.newPassword')}
                    className={`w-full bg-surface text-primary border p-2 rounded shadow-sm focus:outline-none focus:ring-2 focus:ring-brand transition-colors ${errors.newPassword ? 'border-danger' : 'border-border'}`}
                    {...register('newPassword')}
                />
                {errors.newPassword &&
                    <p className="text-danger text-xs mt-1">{t(errors.newPassword.message as string)}</p>}
            </div>

            <div>
                <input
                    type="password"
                    placeholder={t('profile.confirmNewPassword')}
                    className={`w-full bg-surface text-primary border p-2 rounded shadow-sm focus:outline-none focus:ring-2 focus:ring-brand transition-colors ${errors.confirmPassword ? 'border-danger' : 'border-border'}`}
                    {...register('confirmPassword')}
                />
                {errors.confirmPassword &&
                    <p className="text-danger text-xs mt-1">{t(errors.confirmPassword.message as string)}</p>}
            </div>

            <SubmitButton type="submit" className="w-auto px-6 py-2 text-xs tracking-widest uppercase">
                {t('profile.applyChanges')}
            </SubmitButton>

            <ConfirmationModal
                isOpen={isModalOpen}
                title={t('common.confirmPasswordChangeTitle')}
                description={t('common.confirmPasswordChangeDesc')}
                confirmText={t('profile.applyChanges')}
                onConfirm={handleConfirmChange}
                onCancel={() => setIsModalOpen(false)}
                isLoading={isLoading}
            />
        </form>
    );
}