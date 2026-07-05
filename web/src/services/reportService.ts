import axiosInstance from "../api/auth/middleware";
import type {Page} from "../types/user.types";
import type {ReportDTO, StudentOwnReportDetailsDTO, TeacherReportDetails} from "../types/report.types";
import type {StudentReportDetailsDTO} from "../types/studentScan.types.ts";

export const reportService = {
        getAllReportsForSubject: async (
            subjectId: string,
            page?: number,
            size?: number,
            sortBy?: string,
            sortDesc?: boolean
        ) => {
            const params = new URLSearchParams();
            if (page !== undefined) params.append('page', page.toString());
            if (size !== undefined) params.append('size', size.toString());

            if (sortBy !== undefined) params.append('sortBy', sortBy);
            if (sortDesc !== undefined) params.append('sortDesc', sortDesc.toString());

            const response = await axiosInstance.get<Page<ReportDTO>>(
                `/reports/subject/${subjectId}?${params.toString()}`
            );

            return response.data;
        },

        getAllReportsForTeacher: async (
            page?: number,
            size?: number,
            sortBy?: string,
            sortDesc?: boolean
        ): Promise<Page<ReportDTO>> => {
            const params = new URLSearchParams();
            if (page !== undefined) params.append('page', page.toString());
            if (size !== undefined) params.append('size', size.toString());

            if (sortBy !== undefined) params.append('sortBy', sortBy);
            if (sortDesc !== undefined) params.append('sortDesc', sortDesc.toString());

            const response = await axiosInstance.get<Page<ReportDTO>>(
                `/reports/teacher?${params.toString()}`
            );

            return response.data;
        },

        getTeacherReportDetails: async (reportId: string): Promise<TeacherReportDetails> => {
            const response = await axiosInstance.get<TeacherReportDetails>(`/reports/${reportId}`);
            return response.data;
        },

        getReportSummary: async (reportId: string): Promise<string> => {
            const response = await axiosInstance.get<string>(`/reports/${reportId}/summary`, {
                responseType: 'text',
            });
            return response.data;
        },

        deleteReport: async (id: string) => {
            const response = await axiosInstance.delete<Page<ReportDTO>>(
                `/reports/${id}`
            );
            return response.data;
        },

        getMyReports: async (): Promise<ReportDTO[]> => {
            const response = await axiosInstance.get<ReportDTO[]>('/reports/my-reports');
            return response.data;
        },

        getMyReportDetails: async (reportId: string): Promise<StudentOwnReportDetailsDTO> => {
            const response = await axiosInstance.get<StudentOwnReportDetailsDTO>(`/reports/my-reports/${reportId}`);
            return response.data;
        },

        generateStudentScan: async (
            repositoryId: string,
            studentTag: string,
            taskTag: string
        ): Promise<StudentReportDetailsDTO> => {
            const response = await axiosInstance.post<StudentReportDetailsDTO>(
                `/reports/student-scan/${repositoryId}`,
                null,
                {params: {studentTag, taskTag}}
            );
            return response.data;
        },

        postStartSubjectAnalysis: async (
            subjectId: string | undefined,
            tag: string
        ): Promise<ReportDTO> => {
            const response = await axiosInstance.post<ReportDTO>(
                `reports/demand/${subjectId}`,
                null,
                {params: {tag}}
            )
            return response.data;
        }
    };

export const formatDate = (isoString: string) => {
    const date = new Date(isoString);

    const locale = localStorage.getItem('i18nextLng') || navigator.language || 'pl-PL';

    const optionsDate: Intl.DateTimeFormatOptions = { month: 'short', day: 'numeric', year: 'numeric' };
    const optionsTime: Intl.DateTimeFormatOptions = { hour: '2-digit', minute: '2-digit', hour12: false };

    return `${date.toLocaleDateString(locale, optionsDate)} • ${date.toLocaleTimeString(locale, optionsTime)}`;
};