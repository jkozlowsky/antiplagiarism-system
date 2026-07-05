import { useTranslation } from 'react-i18next';
import { changeLanguage } from "../../../services/accountService.ts";

export const LanguageSelector = () => {
    const { i18n } = useTranslation();

    const handleLanguageChange = async (newLangCode: string) => {
        try {
            await i18n.changeLanguage(newLangCode);

            if (sessionStorage.getItem('access_token')) {
                await changeLanguage(newLangCode);
            }
        } catch (error) {
            console.error("Failed to sync language with the backend", error);
        }
    };

    return (
        <div className="relative w-full">
            <select
                value={i18n.resolvedLanguage || 'pl'}
                onChange={(e) => handleLanguageChange(e.target.value)}
                className="w-full bg-base/50 text-sm font-bold text-primary border border-border rounded-xl py-2.5 pl-4 pr-10 outline-none cursor-pointer focus:border-brand focus:ring-2 focus:ring-brand/20 transition-all appearance-none"
            >
                <option value="en">🇺🇸 English (US)</option>
                <option value="pl">🇵🇱 Polish (PL)</option>
                <option value="uk">🇺🇦 Ukrainian (UK)</option>
            </select>

            <div className="pointer-events-none absolute inset-y-0 right-0 flex items-center px-2 text-secondary">
                <svg className="fill-current h-4 w-4" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 20 20">
                    <path d="M9.293 12.95l.707.707L15.657 8l-1.414-1.414L10 10.828 5.757 6.586 4.343 8z" />
                </svg>
            </div>
        </div>
    );
};