import { useEffect, useState, useRef } from "react";
import { useSearchParams, Link } from "react-router-dom";
import { CheckCircle2, XCircle, Loader2 } from "lucide-react";
import { useTranslation } from "react-i18next";
import { motion } from "framer-motion";
import { PATHS } from "../../../routes/paths.ts";
import axiosInstance from "../../../api/auth/middleware.ts";

export default function ActivatePage() {
    const { t } = useTranslation();
    const [searchParams] = useSearchParams();
    const token = searchParams.get("token");

    const [status, setStatus] = useState<"loading" | "success" | "error">("loading");
    const hasAttempted = useRef(false);

    useEffect(() => {
        if (!token) {
            setStatus("error");
            return;
        }
        if (hasAttempted.current) return;
        hasAttempted.current = true;

        const activateAccount = async () => {
            try {
                await axiosInstance.post(`/account/activate?token=${token}`);
                setStatus("success");
            } catch (error) {
                console.error("Activation error:", error);
                setStatus("error");
            }
        };

        activateAccount();
    }, [token]);

    return (
        <motion.div
            initial={{ opacity: 0, scale: 0.95 }}
            animate={{ opacity: 1, scale: 1 }}
            className="flex flex-col items-center justify-center text-center w-full max-w-[440px] mx-auto"
        >
            {status === "loading" && (
                <>
                    <div className="w-24 h-24 mb-8 flex items-center justify-center">
                        <Loader2 className="animate-spin text-brand" size={64}/>
                    </div>
                    <h2 className="text-3xl font-black text-primary mb-3">{t('auth.activate.loading')}</h2>
                    <p className="text-base text-secondary">{t('auth.activate.loadingSub')}</p>
                </>
            )}

            {status === "success" && (
                <>
                    <div className="w-24 h-24 mx-auto bg-green-500/10 text-green-600 rounded-full flex items-center justify-center mb-8 shadow-inner border border-green-500/20">
                        <CheckCircle2 size={48} />
                    </div>
                    <h2 className="text-3xl font-black text-primary mb-3">{t('auth.activate.success')}</h2>
                    <p className="text-base text-secondary mb-10 leading-relaxed">{t('auth.activate.successSub')}</p>
                    <Link
                        to={PATHS.LOGIN}
                        className="w-full block bg-brand hover:bg-brand-hover text-white py-4 shadow-lg shadow-brand/20 font-black text-sm tracking-widest uppercase rounded-2xl transition-all"
                    >
                        {t('auth.activate.proceed')}
                    </Link>
                </>
            )}

            {status === "error" && (
                <>
                    <div className="w-24 h-24 mx-auto bg-danger-subtle text-danger rounded-full flex items-center justify-center mb-8 shadow-inner border border-danger-border">
                        <XCircle size={48} />
                    </div>
                    <h2 className="text-3xl font-black text-primary mb-3">{t('auth.activate.error')}</h2>
                    <p className="text-base text-secondary mb-10 leading-relaxed">
                        {t('auth.activate.errorSub')}
                    </p>
                    <Link
                        to={PATHS.LOGIN}
                        className="w-full block bg-surface border border-border hover:bg-base text-primary py-4 shadow-sm font-black text-sm tracking-widest uppercase rounded-2xl transition-all"
                    >
                        {t('auth.activate.back')}
                    </Link>
                </>
            )}
        </motion.div>
    );
}