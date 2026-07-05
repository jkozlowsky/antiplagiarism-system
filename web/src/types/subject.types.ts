export interface SubjectStudentStatsDTO {
    studentId: string;
    firstName: string;
    lastName: string;
    repositoryId: string;
    repositoryName: string;
    plagiarismRatio: number | null;
}

export interface StudentSubjectDetailsDTO {
    subjectId: string;
    name: string;
    organizationName: string;
    edition: string;
    subjectDescription?: string | null;
    giteaURL: string;
    studentRepository?: {
        repositoryName: string;
        ticketCount: number;
        students?: any[];
    } | null;
    reportVisibilityLevel: string;
    archived?: boolean | null;
}

export const ReportVisibilityLevel = {
    FULL_INSIGHT: 'FULL_INSIGHT',
    SCORE_ONLY: 'SCORE_ONLY',
    HIDDEN: 'HIDDEN',
    ONLY_HIGHEST_PERCENT: 'ONLY_HIGHEST_PERCENT'
} as const;

export interface RepositoryWithStudentDTO {
    repositoryName: string;
    ticketCount: number;
    students: {
        name: string;
        surname: string;
    }[]
}

export interface TranslatedSubjectDescriptionDTO{
    translatedSubjectDescription: string
}