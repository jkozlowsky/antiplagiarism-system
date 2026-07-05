import { useTranslation } from "react-i18next";

export function Footer() {
    const { t } = useTranslation();
    const currentYear = new Date().getFullYear();

    return (
        <footer className="h-10 w-full flex items-center justify-center bg-surface border-t border-border/40 mt-auto">
            <p className="text-xs font-mono font-bold text-secondary/70 tracking-[0.15em] uppercase text-center px-4">
                {t('auth.footer', {
                    year: currentYear,
                    defaultValue: `Politechnika Łódzka • Zespół 1 • ${currentYear} SSBD`
                })}
            </p>
        </footer>
    );
}