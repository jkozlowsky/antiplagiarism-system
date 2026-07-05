import type {SubjectDTO, UpdateSubjectDTO, TeacherAssignmentDTO} from '../types/SubjectDTO';
import axiosInstance from '../api/auth/middleware';
import type {
    StudentSubjectDetailsDTO,
    SubjectStudentStatsDTO,
    TranslatedSubjectDescriptionDTO
} from '../types/subject.types';

export const subjectService = {
    createSubject: async (subject: SubjectDTO): Promise<void> => {
        if (subject.templateId) {
            const templateDto = {
                name: subject.name,
                organizationName: subject.organizationName,
                edition: subject.edition,
                subjectDescription: subject.subjectDescription,
                templateId: subject.templateId,
                raportLevelName: subject.manualRules?.raportLevelName,
                teachers: subject.teachers
            };
            await axiosInstance.post('/subjects/template', templateDto);
        } else {
            const manualDto = {
                name: subject.name,
                organizationName: subject.organizationName,
                edition: subject.edition,
                subjectDescription: subject.subjectDescription,
                manualRules: subject.manualRules,
                teachers: subject.teachers
            };
            await axiosInstance.post('/subjects/manual', manualDto);
        }
    },
    getTeacherSubjects: async (): Promise<SubjectDTO[]> => {
        const response = await axiosInstance.get('/subjects');
        return response.data;
    },
    getStudentSubjects: async (): Promise<SubjectDTO[]> => {
        const response = await axiosInstance.get('/subjects/student/list');
        return response.data;
    },
    getAllSubjectsForAdmin: async (): Promise<SubjectDTO[]> => {
        const response = await axiosInstance.get('/subjects/all');
        return response.data;
    },
    getSubjectUsers: async (subjectName: string): Promise<SubjectStudentStatsDTO[]> => {
        const response = await axiosInstance.get<SubjectStudentStatsDTO[]>(
            `/repositories/subject/${encodeURIComponent(subjectName)}/users`
        );
        return response.data;
    },
    changeSubjectManager: async (subjectId: string, newManagerLogin: string, versionHash: string): Promise<void> => {
        await axiosInstance.patch(`/subjects/${subjectId}/manager`, {
            newManagerLogin: newManagerLogin
        }, {
            headers: {
                'If-Match': versionHash
            }
        });
    },
};

export const getTeacherSubjects = async (): Promise<SubjectDTO[]> => {
    const response = await axiosInstance.get<SubjectDTO[]>('/subjects');
    return response.data;
};

export const getStudentSubjects = async (): Promise<SubjectDTO[]> => {
    const response = await axiosInstance.get<SubjectDTO[]>('/subjects/student/list');
    return response.data;
};

export const getSubjectDetails = async (id: string): Promise<SubjectDTO> => {
    const response = await axiosInstance.get<SubjectDTO>(`/subjects/${id}`);
    if (response.headers['etag']) {
        response.data.versionHash = response.headers['etag'].replace(/"/g, '');
    }

    return response.data;
};

export const updateSubject = async (id: string, data: UpdateSubjectDTO, versionHash: string): Promise<void> => {
    await axiosInstance.put(`/subjects/${id}`, data, {
        headers: {
            'If-Match': versionHash
        }
    });
};

export const deleteSubject = async (subjectId: string, versionHash: string, deleteGiteaOrg: boolean = false): Promise<void> => {
    await axiosInstance.delete(`/subjects/${subjectId}`, {
        headers: {
            'If-Match': versionHash,
        },
        params: {
            deleteGiteaOrg: deleteGiteaOrg,
        },
    });
};

export const syncSubjectWithGitea = async (subjectId: string): Promise<void> => {
    await axiosInstance.post(`/subjects/${subjectId}/sync`);
};

export const getStudentSubjectDetails = async (subjectId: string): Promise<StudentSubjectDetailsDTO> => {
    const response = await axiosInstance.get(`/subjects/${subjectId}/student`);
    return response.data;
};

export const getTranslatedDescription = async (subjectId: string | undefined): Promise<TranslatedSubjectDescriptionDTO> => {
    const response = await axiosInstance.get(`/subjects/${subjectId}/translate`);
    return response.data;
};

export const toggleArchiveSubject = async (subjectId: string, versionHash: string): Promise<void> => {
    await axiosInstance.patch(`/subjects/${subjectId}/archive`, {}, {
        headers: {
            'If-Match': versionHash
        }
    });
};

export const getSubjectTeachers = async (subjectId: string): Promise<TeacherAssignmentDTO[]> => {
    const response = await axiosInstance.get<TeacherAssignmentDTO[]>(`/subjects/${subjectId}/users`);
    return response.data;
};