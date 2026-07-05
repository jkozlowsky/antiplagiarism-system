import axiosInstance from '../api/auth/middleware';
import type {StudentRepositoryDTO} from '../types/studentScan.types';
import type {RepositoryWithStudentDTO} from "../types/subject.types.ts";

export const repositoryService = {
    getMyRepositories: async (): Promise<StudentRepositoryDTO[]> => {
        const response = await axiosInstance.get<StudentRepositoryDTO[]>('/repositories/my-repositories');
        return response.data;
    },

    getRepositoriesForSubject: async (id: string): Promise<RepositoryWithStudentDTO[]> => {
        const response = await axiosInstance.get<RepositoryWithStudentDTO[]>(`/repositories/subject/${id}`);
        return response.data;
    }


};