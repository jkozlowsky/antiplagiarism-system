import axiosInstance from '../api/auth/middleware';
import type { TeacherSearchDTO } from '../types/SubjectDTO';

export const searchTeachers = async (query: string): Promise<TeacherSearchDTO[]> => {
    const response = await axiosInstance.get<TeacherSearchDTO[]>('/teachers/search', {
        params: { query }
    });
    return response.data;
};