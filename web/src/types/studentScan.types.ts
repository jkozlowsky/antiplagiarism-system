export interface CheckCodeWithPresetSetDTO {
    similarity: number;
    matchedLines: number;
}

export interface StudentReportDetailsDTO {
    reportId: string;
    tag: string;
    subjectName: string;
    createdAt: string;
    matches: CheckCodeWithPresetSetDTO[];
}

export interface StudentRepositoryDTO {
    repositoryId: string;
    repositoryName: string;
    subjectName: string;
    usedScans: number;
    maxScans: number;
}