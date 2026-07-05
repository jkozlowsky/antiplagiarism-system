import { useState } from "react";
import { Link, useSearchParams } from "react-router-dom";
import { CheckCircle2, Loader2, Unlock, XCircle } from "lucide-react";
import { useTranslation } from "react-i18next";
import { PATHS } from "../../../routes/paths.ts";
import axiosInstance from "../../../api/auth/middleware.ts";
import { motion } from "framer-motion";

export default function UnblockAccountPage() {
    const { t } = useTranslation();
    const [searchParams] = useSearchParams();
    const token = searchParams.get("token");

    const [status, setStatus] = useState<"idle" | "loading" | "success" | "error">("idle");

    const handleUnblock = async () => {
        if (!token) {
            setStatus("error");
            return;
        }

        setStatus("loading");
        try {
            await axiosInstance.post(`/account/unblock-inactive?token=${token}`);
            setStatus("success");
        } catch (error) {
            console.error("Unblock error:", error);
            setStatus("error");
        }
    };

    return (
        <motion.div
            initial={{ opacity: 0, scale: 0.95 }}
            animate={{ opacity: 1, scale: 1 }}
            className="flex flex-col items-center justify-center text-center w-full max-w-[440px] mx-auto"
        >
            {status === "idle" && (
                <>
                    <div className="w-24 h-24 mx-auto bg-brand/10 text-brand rounded-full flex items-center justify-center mb-8 shadow-inner border border-brand/20">
                        <Unlock size={48} />
                    </div>
                    <h2 className="text-3xl font-black text-primary mb-3">{t('auth.unblock.heading')}</h2>
                    <p className="text-base text-secondary mb-10 leading-relaxed">{t('auth.unblock.description')}</p>
                    <button
                        onClick={handleUnblock}
                        className="w-full py-4 bg-brand hover:bg-brand-hover text-white shadow-lg shadow-brand/20 font-black text-sm tracking-widest uppercase rounded-2xl transition-all"
                    >
                        {t('auth.unblock.button')}
                    </button>
                </>
            )}

            {status === "loading" && (
                <>
                    <div className="w-24 h-24 mb-8 flex items-center justify-center">
                        <Loader2 className="animate-spin text-brand" size={64}/>
                    </div>
                    <h2 className="text-3xl font-black text-primary mb-3">{t('auth.unblock.loading')}</h2>
                    <p className="text-base text-secondary">{t('auth.unblock.loadingSub')}</p>
                </>
            )}

            {status === "success" && (
                <>
                    <div className="w-24 h-24 mx-auto bg-green-500/10 text-green-600 rounded-full flex items-center justify-center mb-8 shadow-inner border border-green-500/20">
                        <CheckCircle2 size={48} />
                    </div>
                    <h2 className="text-3xl font-black text-primary mb-3">{t('auth.unblock.success')}</h2>
                    <p className="text-base text-secondary mb-10 leading-relaxed">{t('auth.unblock.successSub')}</p>
                    <Link
                        to={PATHS.LOGIN}
                        className="w-full block bg-brand hover:bg-brand-hover text-white py-4 shadow-lg shadow-brand/20 font-black text-sm tracking-widest uppercase rounded-2xl transition-all"
                    >
                        {t('auth.unblock.proceed')}
                    </Link>
                </>
            )}

            {status === "error" && (
                <>
                    <div className="w-24 h-24 mx-auto bg-danger-subtle text-danger rounded-full flex items-center justify-center mb-8 shadow-inner border border-danger-border">
                        <XCircle size={48} />
                    </div>
                    <h2 className="text-3xl font-black text-primary mb-3">{t('auth.unblock.error')}</h2>
                    <p className="text-base text-secondary mb-10 leading-relaxed">
                        {t('auth.unblock.errorSub')}
                    </p>
                    <Link
                        to={PATHS.LOGIN}
                        className="w-full block bg-surface border border-border hover:bg-base text-primary py-4 shadow-sm font-black text-sm tracking-widest uppercase rounded-2xl transition-all"
                    >
                        {t('auth.unblock.back')}
                    </Link>
                </>
            )}
        </motion.div>
    );
}