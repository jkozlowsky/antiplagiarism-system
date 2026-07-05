import { useNavigate } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { ShieldAlert, ArrowLeft } from 'lucide-react';

export default function NotFoundPage() {
    const { t } = useTranslation();
    const navigate = useNavigate();

    return (
        <div className="flex flex-col items-center justify-center min-h-[80vh] w-full text-center px-6">
            <div className="text-brand mb-6">
                <ShieldAlert size={80} strokeWidth={1.5} />
            </div>

            <h1 className="text-6xl font-extrabold text-primary dark:text-primary-dark mb-4">
                404
            </h1>

            <h2 className="text-2xl font-bold text-secondary dark:text-secondary-dark mb-2">
                {t('notFound.title', 'Strona nie znaleziona')}
            </h2>

            <p className="text-secondary dark:text-secondary-dark max-w-md mb-8">
                {t('notFound.description', 'Strona, której szukasz, nie istnieje lub została przeniesiona.')}
            </p>

            <button
                onClick={() => navigate(-1)}
                className="flex items-center justify-center gap-2 px-6 py-3 bg-brand text-white font-bold rounded-md hover:bg-brand-hover transition-colors outline-none focus:ring-2 focus:ring-brand/50"
            >
                <ArrowLeft size={20} />
                {t('notFound.goBack', 'Wróć')}
            </button>
        </div>
    );
}