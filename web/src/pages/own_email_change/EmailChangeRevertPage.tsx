import {useEffect, useRef, useState} from "react";
import {useSearchParams} from "react-router-dom";
import {AlertCircle, CheckCircle2, Loader2} from "lucide-react";
import {useTranslation} from "react-i18next";

import {PATHS} from "../../routes/paths.ts";
import {emailChangeService} from "../../services/emailChangeService.ts";
import LinkButton from "../../shared/components/buttons/LinkButton.tsx";

export default function EmailChangeRevertPage() {
    const {t} = useTranslation();
    const [searchParams] = useSearchParams();
    const token = searchParams.get("token");

    const [status, setStatus] = useState<'loading' | 'success' | 'error'>('loading');
    const called = useRef(false);

    useEffect(() => {
        if (!token) {
            setStatus('error');
            return;
        }

        const revertEmail = async () => {
            if (called.current) return;
            called.current = true;

            try {
                await emailChangeService.revertEmailChange(token);
                setStatus('success');
            } catch (error) {
                console.error("Failed to revert email", error);
                setStatus('error');
            }
        };

        revertEmail();
    }, [token]);

    return (
        <div
            className="w-full flex flex-col items-center justify-center animate-in fade-in slide-in-from-bottom-4 duration-500 text-center">

            {status === 'loading' && (
                <>
                    <Loader2 size={48} className="animate-spin text-brand mb-4"/>
                    <h2 className="text-3xl font-bold text-primary mb-2">{t('emailChange.revert.loading')}</h2>
                </>
            )}

            {status === 'success' && (
                <>
                    <div className="flex justify-center mb-4 text-green-600 dark:text-green-500">
                        <CheckCircle2 size={48}/>
                    </div>
                    <h2 className="text-3xl font-bold text-primary mb-2">{t('emailChange.revert.success.title')}</h2>
                    <p className="text-sm text-secondary mb-8 max-w-sm mx-auto">
                        {t('emailChange.revert.success.description')}
                    </p>
                    <LinkButton to={PATHS.LOGIN} className="max-w-sm mx-auto">
                        {t('common.goToLogin')}
                    </LinkButton>
                </>
            )}

            {status === 'error' && (
                <>
                    <div className="flex justify-center mb-4 text-danger">
                        <AlertCircle size={48}/>
                    </div>
                    <h2 className="text-3xl font-bold text-primary mb-2">{t('emailChange.revert.error.title')}</h2>
                    <p className="text-sm text-secondary mb-8 max-w-sm mx-auto">
                        {t('emailChange.revert.error.description')}
                    </p>
                    <LinkButton to={PATHS.LOGIN} className="max-w-sm mx-auto">
                        {t('common.goToLogin')}
                    </LinkButton>
                </>
            )}
        </div>
    );
}