export const RoleEnum = {
    ADMINISTRATOR: 'ADMIN',
    TEACHER: 'TEACHER',
    STUDENT: 'STUDENT',
    FORCE_PASSWORD_CHANGE: 'ROLE_FORCE_PASSWORD_CHANGE',
} as const;

export type Role = typeof RoleEnum[keyof typeof RoleEnum];