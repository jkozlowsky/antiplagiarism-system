export const PATHS = {
    USER_LIST: "/users",
    USER_EDIT: "/users/:id",
    USER_EDIT_ME: "/users/me",
    ABOUT: "/about",
    LOGIN: "/login",
    REGISTER: "/register",
    ACTIVATE: "/activate",
    FORGOT_PASSWORD: "/forgot-password",
    RESET_PASSWORD: "/reset-password",
    PROFILE: "/profile",
    OWN_EMAIL_CHANGE_MAIN: "/email/change",
    OWN_EMAIL_CHANGE_CONFIRM: "/change-email",
    OWN_EMAIL_CHANGE_REVERT: "/revert-email",
    _2FA_VERIFY: "/login/2fa",
    STUDENT_SUBJECT_LIST: "/my-subjects",
    TEACHER_SUBJECT_LIST: "/subjects",
    UNBLOCK_ACCOUNT: "/unblock-account",
    FORCE_PASSWORD_CHANGE: "/force-password-change",
    LOGIN_EMAIL: "/login/email",
    LOGIN_EMAIL_VERIFY: "/login/email/verify",
    CREATE_SUBJECT: '/subjects/create',
    TEACHER_SUBJECT_USERS: "/subjects/:subjectName/users",
    REPORT_LIST: "/reports",
    TEACHER_REPORT_DETAILS: "/reports/:id",
    STUDENT_REPORTS: '/student-reports',
    STUDENT_REPORT_DETAILS: '/student-reports/:id',
    GLOBAL_RULES: "/global-rules",
    STUDENT_SUBJECT_DETAILS: "/my-subjects/:id",
    SUBJECT_DETAILS: "/subjects/:id",
    STUDENT_SCAN: "/student-scan",
    SUBJECT_SCHEDULE_LIST: "/subjects/:id/schedule",
    CHANGE_SUBJECT_MANAGER: "/change-subject-manager",
    AUDIT_LOGS: '/audit'
} as const;

export const getDashboardPath = (role: string | null): string => {
    switch (role?.toUpperCase()) {
        case 'ADMIN':
            return PATHS.USER_LIST;
        case 'TEACHER':
            return PATHS.TEACHER_SUBJECT_LIST;
        case 'STUDENT':
            return PATHS.STUDENT_SUBJECT_LIST;
        default:
            return PATHS.PROFILE;
    }
};

export type AppPaths = typeof PATHS[keyof typeof PATHS];