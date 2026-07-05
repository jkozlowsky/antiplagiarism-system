import {
    ChartColumnIncreasing,
    FileText,
    LucideGraduationCap,
    ScanSearch,
    Settings,
    Users,
    UserCheck,
    History
} from "lucide-react";
import {useTranslation} from "react-i18next";
import {PATHS} from "../../../routes/paths.ts";
import SidebarItem from "./SidebarItem.tsx";
import {RoleEnum} from "../../../types/role.types.ts";

export default function Sidebar() {
    const {t} = useTranslation();

    return (
        <aside className="w-72 bg-surface border-r border-border flex flex-col justify-between hidden lg:flex shrink-0">
            <div className="py-8 px-4 space-y-2">
                <div className="px-4 mb-5 text-xs font-black text-secondary/80 tracking-widest uppercase">
                    Menu
                </div>

                <div className="flex flex-col gap-1.5">
                    {/** Elementy dla admina */}
                    <SidebarItem to={PATHS.USER_LIST} icon={Users} label={t('sidebar.userManagement')}
                                 allowedRoles={[RoleEnum.ADMINISTRATOR]}/>
                    <SidebarItem to={PATHS.CHANGE_SUBJECT_MANAGER} icon={UserCheck}
                                 label={t('sidebar.changeSubjectManager')} allowedRoles={[RoleEnum.ADMINISTRATOR]}/>

                    {/** Elementy dla studenta */}
                    <SidebarItem to={PATHS.STUDENT_SUBJECT_LIST} icon={LucideGraduationCap}
                                 label={t('sidebar.subjectList')} allowedRoles={[RoleEnum.STUDENT]}/>
                    <SidebarItem to={PATHS.STUDENT_REPORTS} icon={FileText} label={t("studentReportList.title")}
                                 allowedRoles={[RoleEnum.STUDENT]}/>
                    <SidebarItem to={PATHS.STUDENT_SCAN} icon={ScanSearch} label={t('sidebar.studentScan')}
                                 allowedRoles={[RoleEnum.STUDENT]}/>
                    {/*<aside*/}
                    {/*    className="w-64 bg-base border-r border-border flex flex-col justify-between hidden md:flex shrink-0">*/}
                    {/*    <div className="py-6 px-3 space-y-1">*/}
                    {/*        /!** Elementy dla admina *!/*/}
                    {/*        <SidebarItem*/}
                    {/*            to={PATHS.USER_LIST}*/}
                    {/*            icon={Users}*/}
                    {/*            label={t('sidebar.userManagement', 'User Management')}*/}
                    {/*            allowedRoles={[RoleEnum.ADMINISTRATOR]}*/}
                    {/*        />*/}
                    {/*        <SidebarItem*/}
                    {/*            to={PATHS.CHANGE_SUBJECT_MANAGER}*/}
                    {/*            icon={UserCheck}*/}
                    {/*            label={t('sidebar.changeSubjectManager', 'Change Subject Manager')}*/}
                    {/*            allowedRoles={[RoleEnum.ADMINISTRATOR]}*/}
                    {/*        />*/}
                    {/*        <SidebarItem*/}
                    {/*            to={PATHS.AUDIT_LOGS}*/}
                    {/*            icon={History}*/}
                    {/*            label={t('sidebar.auditLogs', 'System Audit')}*/}
                    {/*            allowedRoles={[RoleEnum.ADMINISTRATOR]}*/}
                    {/*        />*/}
                    {/*        /!** Elementy dla studenta *!/*/}
                    {/*        <SidebarItem*/}
                    {/*            to={PATHS.STUDENT_SUBJECT_LIST}*/}
                    {/*            icon={LucideGraduationCap}*/}
                    {/*            label={t('sidebar.subjectList', 'Subject list')}*/}
                    {/*            allowedRoles={[RoleEnum.STUDENT]}*/}
                    {/*        />*/}
                    {/*        <SidebarItem*/}
                    {/*            to={PATHS.STUDENT_REPORTS}*/}
                    {/*            icon={FileText}*/}
                    {/*            label={t("studentReportList.title")}*/}
                    {/*            allowedRoles={[RoleEnum.STUDENT]}*/}
                    {/*        />*/}
                    {/*        <SidebarItem*/}
                    {/*            to={PATHS.STUDENT_SCAN}*/}
                    {/*            icon={ScanSearch}*/}
                    {/*            label={t('sidebar.studentScan')}*/}
                    {/*            allowedRoles={[RoleEnum.STUDENT]}*/}
                    {/*        />*/}

                    {/** Elementy dla Prowadzącego */}
                    <SidebarItem
                        to={PATHS.AUDIT_LOGS}
                        icon={History}
                        label={t('sidebar.auditLogs', 'System Audit')}
                        allowedRoles={[RoleEnum.ADMINISTRATOR]}
                    />
                    <SidebarItem to={PATHS.TEACHER_SUBJECT_LIST} icon={LucideGraduationCap}
                                 label={t('sidebar.subjectList')} allowedRoles={[RoleEnum.TEACHER]}/>
                    <SidebarItem to={PATHS.REPORT_LIST} icon={ChartColumnIncreasing} label={t('sidebar.reportList')}
                                 allowedRoles={[RoleEnum.TEACHER]}/>
                    <SidebarItem to={PATHS.GLOBAL_RULES} icon={Settings} label={t('sidebar.globalRules')}
                                 allowedRoles={[RoleEnum.TEACHER]}/>
                </div>
            </div>
        </aside>
    );
}