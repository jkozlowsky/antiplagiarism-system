export interface ReportDTO {
    id: string;
    tag: string;
    average_similarity: number;
    created_at: string;
    created_by: string;
    subject_name: string;
    scanned_repositories: number;
}

export interface FilteredComparisonDTO {
    matchedWith: string;
    studentSimilarity?: number;
    otherSimilarity?: number;
    averageSimilarity?: number;
}

export interface StudentOwnReportDetailsDTO {
    reportId: string;
    tag: string;
    subjectName: string;
    createdAt: string;
    matches: FilteredComparisonDTO[];
    raportLevel: string
}

export interface TeacherMatchedFile {
    fileA: string;
    fileB: string;
    codeA: string;
    codeB: string;
    matchedLinesA: number[];
    matchedLinesB: number[];
    tokens?: number;
}

export interface TeacherComparison {
    firstSubmission: string;
    secondSubmission: string;
    maxSimilarity: number;
    averageSimilarity: number;
    longestMatch: number;
    similarityA?: number;
    similarityB?: number;
    fileA?: string;
    fileB?: string;
    codeA?: string;
    codeB?: string;
    matchedLinesA?: number[];
    matchedLinesB?: number[];
    matchedFiles?: TeacherMatchedFile[];
}

export interface TeacherReportDetails {
    id: string;
    tag: string;
    subjectName: string;
    createdAt: string;
    averageSimilarity: number;
    comparisons: TeacherComparison[];
}