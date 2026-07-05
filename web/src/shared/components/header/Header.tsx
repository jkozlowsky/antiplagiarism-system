import { useLocation, useNavigate, Link } from "react-router-dom";
import { Globe, Moon, Palette, Settings, Shield, Sun, LogOut, ShieldCheck } from "lucide-react";
import { useTranslation } from "react-i18next";
import { useAuth } from "../../../hooks/useAuth";
import { PATHS } from "../../../routes/paths";
import { useBreadcrumb } from "../../../contexts/BreadcrumbContext";
import { useEffect, useRef, useState } from "react";
import { useTheme } from "../../../hooks/useTheme.ts";
import { LanguageSelector } from "./LanguageSelector.tsx";
import { motion, AnimatePresence } from "framer-motion";

export default function Header() {
    const { logout, availableRoles, changeActiveRole, activeRole, userLogin } = useAuth();
    const location = useLocation();
    const navigate = useNavigate();
    const { dynamicBreadcrumb } = useBreadcrumb();
    const { t } = useTranslation();

    const [isSettingsOpen, setIsSettingsOpen] = useState(false);
    const settingsRef = useRef<HTMLDivElement>(null);
    const { theme, setTheme } = useTheme();

    useEffect(() => {
        const handleClickOutside = (event: MouseEvent) => {
            if (settingsRef.current && !settingsRef.current.contains(event.target as Node)) {
                setIsSettingsOpen(false);
            }
        };
        document.addEventListener("mousedown", handleClickOutside);
        return () => document.removeEventListener("mousedown", handleClickOutside);
    }, []);

    const handleLogout = () => {
        logout();
        navigate(PATHS.LOGIN);
    };

    const generateBreadcrumbs = () => {
        const paths = location.pathname.split('/').filter(p => p !== '');
        if (paths.length === 0) return [{ name: t('header.dashboard'), path: "/", isLast: true }];

        let currentPath = "";
        return paths.map((path, index) => {
            currentPath += `/${path}`;
            const isLast = index === paths.length - 1;
            const pathLower = path.toLowerCase();

            let formattedName = path.replace(/-/g, ' ');
            formattedName = formattedName.charAt(0).toUpperCase() + formattedName.slice(1);

            if (isLast && dynamicBreadcrumb) {
                formattedName = t(dynamicBreadcrumb);
            } else if (pathLower === 'users') {
                formattedName = t('header.userManagement');
            } else if (pathLower === 'student' || pathLower === 'teacher' || pathLower === 'admin') {
                formattedName = t(`userEdit.roles.${pathLower}`);
            } else if (pathLower === 'reports') {
                formattedName = activeRole === 'STUDENT' ? t('studentReportList.title') : t('reportList.title');
            } else if (pathLower === 'subjects') {
                formattedName = t('sidebar.subjectList');
            } else if (pathLower === 'scan') {
                formattedName = t('sidebar.studentScan');
            } else if (pathLower === 'profile') {
                formattedName = t('profile.accountSettings');
            }

            return { name: formattedName, path: currentPath, isLast };
        });
    };

    const breadcrumbs = generateBreadcrumbs();

    const getRoleIndicatorColor = (role: string | null) => {
        switch (role?.toUpperCase()) {
            case 'ADMIN': return 'bg-blue-500 shadow-[0_0_10px_rgba(59,130,246,0.5)]';
            case 'TEACHER': return 'bg-red-500 shadow-[0_0_10px_rgba(239,68,68,0.5)]';
            case 'STUDENT': return 'bg-green-500 shadow-[0_0_10px_rgba(34,197,94,0.5)]';
            default: return 'bg-transparent';
        }
    };

    const getDefaultLocationPath = (role: string | null) => {
        switch (role?.toUpperCase()) {
            case 'ADMIN': return PATHS.USER_LIST;
            case 'TEACHER': return PATHS.TEACHER_SUBJECT_LIST;
            case 'STUDENT': return PATHS.STUDENT_SUBJECT_LIST;
            default: return '/';
        }
    };

    return (
        <header className="sticky top-0 z-50 flex items-center justify-between w-full px-4 md:px-6 py-3 bg-surface/80 backdrop-blur-xl border-b border-border shadow-sm transition-colors duration-300">

            <div className="flex items-center gap-4 md:gap-6">
                <Link to={getDefaultLocationPath(activeRole)} className="flex items-center gap-3 group select-none">
                    <div className="p-2 bg-brand/10 text-brand rounded-xl border border-brand/20 transition-all group-hover:bg-brand/20 group-hover:scale-105">
                        <ShieldCheck size={22} strokeWidth={2.5} />
                    </div>
                    <h1 className="text-xl md:text-2xl font-black text-primary tracking-tight leading-none group-hover:text-brand transition-colors">
                        {t('header.title', 'System Antyplagiatowy')}
                    </h1>
                </Link>

                <div className="h-6 w-px bg-border hidden md:block"></div>

                <nav className="hidden xl:flex items-center gap-3 text-sm font-medium">
                    {breadcrumbs.map((crumb, index) => (
                        <div key={index} className="flex items-center gap-3">
                            {index > 0 && <span className="text-secondary/40 font-light">/</span>}
                            {crumb.isLast ? (
                                <span className="text-brand font-bold bg-brand/10 px-2.5 py-1 rounded-md border border-brand/20 shadow-sm">
                                    {crumb.name}
                                </span>
                            ) : (
                                <Link to={crumb.path} className="text-secondary hover:text-primary transition-colors">
                                    {crumb.name}
                                </Link>
                            )}
                        </div>
                    ))}
                </nav>
            </div>

            <div className="flex items-center gap-4 md:gap-6">
                <div className="relative" ref={settingsRef}>
                    <motion.button
                        whileHover={{ scale: 1.05 }}
                        whileTap={{ scale: 0.95 }}
                        className={`p-2 rounded-xl transition-all border ${isSettingsOpen ? 'bg-brand/10 text-brand border-brand/20' : 'bg-base border-border hover:border-brand/30 hover:text-brand'}`}
                        onClick={() => setIsSettingsOpen(!isSettingsOpen)}
                    >
                        <Settings size={20} />
                    </motion.button>

                    <AnimatePresence>
                        {isSettingsOpen && (
                            <motion.div
                                initial={{ opacity: 0, y: 10, scale: 0.95 }}
                                animate={{ opacity: 1, y: 0, scale: 1 }}
                                exit={{ opacity: 0, y: 10, scale: 0.95 }}
                                transition={{ duration: 0.2 }}
                                className="absolute right-0 mt-3 w-80 bg-surface rounded-2xl shadow-2xl border border-border p-6 z-50"
                            >
                                <h3 className="text-lg font-black text-primary mb-6 tracking-tight">{t("header.settings.name")}</h3>
                                <div className="mb-6">
                                    <p className="text-[10px] font-black text-secondary mb-3 tracking-widest flex items-center gap-2 uppercase">
                                        <Palette size={14} /> Appearance
                                    </p>
                                    <div className="flex bg-base/50 border border-border rounded-xl p-1">
                                        <button onClick={() => setTheme('light')} className={`flex-1 flex items-center justify-center gap-2 py-2.5 text-sm font-bold rounded-lg transition-all ${theme === 'light' ? 'bg-surface shadow text-primary border border-border' : 'text-secondary hover:text-primary hover:bg-surface/50'}`}><Sun size={16} /> Light</button>
                                        <button onClick={() => setTheme('dark')} className={`flex-1 flex items-center justify-center gap-2 py-2.5 text-sm font-bold rounded-lg transition-all ${theme === 'dark' ? 'bg-surface shadow text-primary border border-border' : 'text-secondary hover:text-primary hover:bg-surface/50'}`}><Moon size={16} /> Dark</button>
                                    </div>
                                </div>
                                <div className="mb-5 w-full">
                                    <p className="text-[11px] font-bold text-secondary mb-2 tracking-wider flex items-center gap-2 uppercase">
                                        <Globe size={14} /> {t("header.settings.language")}
                                    </p>
                                    <LanguageSelector />
                                </div>
                                {availableRoles.length > 1 && (
                                    <div className="mb-2">
                                        <p className="text-[10px] font-black text-secondary mb-3 tracking-widest flex items-center gap-2 uppercase">
                                            <Shield size={14} /> {t("header.settings.activeRole")}
                                        </p>
                                        <select value={activeRole || ""} onChange={(e) => changeActiveRole(e.target.value)} className="w-full bg-base/50 text-sm font-bold text-primary border border-border rounded-xl py-2.5 px-4 outline-none cursor-pointer focus:border-brand focus:ring-2 focus:ring-brand/20 transition-all appearance-none">
                                            {availableRoles.map(role => (
                                                <option key={role} value={role}>{t("userEdit.roles." + role.toLowerCase())}</option>
                                            ))}
                                        </select>
                                    </div>
                                )}
                            </motion.div>
                        )}
                    </AnimatePresence>
                </div>

                <div className="h-6 w-px bg-border hidden sm:block"></div>

                <div className="flex items-center gap-4">
                    <div className="text-right hidden sm:block">
                        <p className="text-sm font-black text-primary leading-tight">{userLogin || t('header.defaultUser')}</p>
                        <p className="text-[10px] text-secondary font-bold tracking-widest uppercase">{t("userEdit.roles." + activeRole?.toLowerCase())}</p>
                    </div>
                    <Link to={PATHS.PROFILE}>
                        <motion.div whileHover={{ scale: 1.05 }} className="relative group cursor-pointer">
                            <div className="w-10 h-10 bg-brand/10 rounded-xl overflow-hidden flex items-center justify-center border border-brand/20 group-hover:border-brand transition-colors">
                                <img src={`https://api.dicebear.com/7.x/avataaars/svg?seed=${userLogin || 'default'}`} alt="Avatar" className="w-full h-full object-cover" />
                            </div>
                            <span className="absolute -bottom-1 -right-1 w-3 h-3 bg-green-500 border-2 border-surface rounded-full shadow-[0_0_8px_rgba(34,197,94,0.6)]"></span>
                        </motion.div>
                    </Link>
                </div>

                <motion.button
                    id={"logoutBtn"}
                    whileHover={{ scale: 1.05 }} whileTap={{ scale: 0.95 }} onClick={handleLogout} className="hidden sm:flex items-center justify-center w-10 h-10 rounded-xl bg-danger-subtle text-danger border border-danger-border hover:bg-danger hover:text-white transition-colors ml-1" title={t('header.logout')}>
                    <LogOut size={18} />
                </motion.button>
                {/*<button*/}
                {/*    id="logoutBtn"*/}
                {/*    onClick={handleLogout}*/}
                {/*    className="flex items-center gap-2 text-sm font-bold text-brand hover:text-brand-hover transition-colors ml-2"*/}
                {/*>*/}
                {/*    {t('header.logout')}*/}
                {/*</button>*/}
            </div>

            <div className={`absolute bottom-0 left-0 w-full h-[2px] transition-colors duration-300 ${getRoleIndicatorColor(activeRole)}`} />
        </header>
    );
}