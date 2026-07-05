import {useState} from 'react';
import {useForm} from 'react-hook-form';
import {yupResolver} from '@hookform/resolvers/yup';
import type {InferType} from 'yup';
import {useTranslation} from 'react-i18next';
import axios from 'axios';

import type {AccountDTO} from '../../types/user.types';
import SubmitButton from '../../shared/components/buttons/SubmitButton';
import {passwordSchema} from '../../shared/validators/passwordSchema';
import ConfirmationModal from '../../shared/components/modals/ConfirmationPopup.tsx';
import {changeOtherPassword} from "../../services/accountService.ts";

interface Props {
    isOpen: boolean;
    user: AccountDTO;
    onClose: () => void;
    onSuccess: () => void;
}

const schema = passwordSchema;
type FormValues = InferType<typeof schema>;

export default function ChangeOtherPasswordModal({isOpen, user, onClose, onSuccess}: Props) {
    const {t} = useTranslation();

    const [apiError, setApiError] = useState<string | null>(null);
    const [isConfirmModalOpen, setIsConfirmModalOpen] = useState(false);
    const [pendingData, setPendingData] = useState<FormValues | null>(null);
    const [isSubmittingApi, setIsSubmittingApi] = useState(false);

    const {register, handleSubmit, reset, formState: {errors}} = useForm<FormValues>({
        resolver: yupResolver(schema),
        mode: 'onTouched'
    });

    if (!isOpen) return null;

    const onFormSubmit = (data: FormValues) => {
        setPendingData(data);
        setIsConfirmModalOpen(true);
    };

    const handleConfirmChange = async () => {
        if (!pendingData) return;

        setApiError(null);
        setIsSubmittingApi(true);

        try {
            await changeOtherPassword(user.login, {newPassword: pendingData.newPassword}, user.versionHash);
            reset();
            setIsConfirmModalOpen(false);
            onSuccess();
        } catch (err: any) {
            console.error("Failed to change password for user", err);
            if (axios.isAxiosError(err)) {
                const backendMsg = err.response?.data?.message || err.response?.data;
                if (err.response?.status === 409 && backendMsg === 'error.password.already.used') {
                    setApiError(t('error.password.already.used'));
                } else {
                    setApiError(backendMsg || t('error.changePasswordFailed'));
                }
            } else {
                setApiError(t('error.changePasswordFailed'));
            }
            setIsConfirmModalOpen(false);
        } finally {
            setIsSubmittingApi(false);
        }
    };
    return (
        <div
            className="fixed inset-0 bg-black/50 backdrop-blur-sm flex items-center justify-center p-4 z-50 animate-in fade-in duration-200">
            <div
                className="bg-surface border border-border rounded-lg p-8 max-w-md w-full shadow-xl animate-in zoom-in-95 duration-200">
                <h2 className="text-xl font-bold mb-6 text-primary border-b border-border pb-2">
                    {t('userList.changePasswordFor', 'Change password for')} <span
                    className="text-brand">{user.login}</span>
                </h2>

                <form onSubmit={handleSubmit(onFormSubmit)} className="space-y-4">
                    {apiError && (
                        <div
                            className="p-3 bg-danger-subtle text-danger rounded-md text-sm border border-danger-border">
                            {apiError}
                        </div>
                    )}

                    <div>
                        <input
                            id="newPasswordInput"
                            type="password"
                            placeholder={t('profile.newPassword')}
                            className={`w-full bg-surface text-primary border p-3 rounded-md shadow-sm focus:outline-none focus:ring-2 focus:ring-brand transition-colors ${errors.newPassword ? 'border-danger' : 'border-border'}`}
                            {...register('newPassword')}
                            disabled={isSubmittingApi}
                        />
                        {errors.newPassword &&
                            <p className="text-danger text-xs mt-1">{t(errors.newPassword.message as string)}</p>}
                    </div>

                    <div>
                        <input
                            id="confirmNewPasswordInput"
                            type="password"
                            placeholder={t('profile.confirmNewPassword')}
                            className={`w-full bg-surface text-primary border p-3 rounded-md shadow-sm focus:outline-none focus:ring-2 focus:ring-brand transition-colors ${errors.confirmPassword ? 'border-danger' : 'border-border'}`}
                            {...register('confirmPassword')}
                            disabled={isSubmittingApi}
                        />
                        {errors.confirmPassword &&
                            <p className="text-danger text-xs mt-1">{t(errors.confirmPassword.message as string)}</p>}
                    </div>

                    <div className="flex gap-4 pt-4">
                        <button
                            type="button"
                            onClick={onClose}
                            disabled={isSubmittingApi}
                            className="flex-1 px-4 py-3 border border-border text-secondary rounded-md font-bold hover:bg-base hover:text-primary transition-colors disabled:opacity-50"
                        >
                            {t('profile.cancel', 'Cancel')}
                        </button>
                        <div className="flex-1">
                            <SubmitButton id="submitOtherPasswordBtn" isLoading={isSubmittingApi} className="!mt-0">
                                {t('profile.applyChanges')}
                            </SubmitButton>
                        </div>
                    </div>
                </form>
            </div>

            <ConfirmationModal
                isOpen={isConfirmModalOpen}
                title={t('common.confirmPasswordChangeTitle')}
                description={t('common.confirmPasswordChangeAdminDesc', `Are you sure you want to forcibly change the password for ${user.login}? They will lose access using their current credentials.`)}
                confirmText={t('profile.applyChanges')}
                onConfirm={handleConfirmChange}
                onCancel={() => setIsConfirmModalOpen(false)}
                isLoading={isSubmittingApi}
            />
        </div>
    );
}