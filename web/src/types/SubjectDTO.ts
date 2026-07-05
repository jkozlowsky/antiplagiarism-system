export interface RulePresetDTO {
    id: string;
    raportLevelName: string;
    studentTicketCount: number;
    minimumTokensMatch: number;
    enableNormalization: boolean;
}

export interface SubjectDTO {
    id?: string | null;
    versionHash?: string | null;
    name: string;
    organizationName: string;
    edition: string;
    subjectDescription?: string | null;
    giteaURL?: string;
    archived?: boolean | null;
    canEdit?: boolean | null;
    canManageTeachers?: boolean | null;
    canViewStats?: boolean | null;
    templateId?: string | null;
    manualRules?: RulePresetDTO | null;
    teachers?: TeacherAssignmentDTO[];
    repositoryList?: any[];
    aggregatedAverageSimilarity?: number | null;
    nextScan?: string | null;
}

export type TeacherSubjectRole = 'OWNER' | 'EDITOR' | 'VIEWER';

export interface TeacherAssignmentDTO {
    login: string;
    role: TeacherSubjectRole;
}

export interface UpdateRulePresetDTO {
    raportLevelName: string;
    studentTicketCount: number;
    minimumTokensMatch: number;
    enableNormalization: boolean;
}

export interface UpdateSubjectDTO {
    name: string;
    subjectDescription?: string | null;
    teachers: TeacherAssignmentDTO[];
    rules?: UpdateRulePresetDTO;
}

export interface TeacherSearchDTO {
    login: string;
    firstName: string;
    lastName: string;
    email: string;
}