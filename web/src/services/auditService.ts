import axiosInstance from "../api/auth/middleware";
import type {AuditRecordDTO} from "../types/audit.types";
import type {Page} from "../types/user.types";

export const auditService = {
    getAuditLogs: async (
        page: number,
        size: number,
        sortBy: string,
        sortDesc: boolean,
        entityFilter: string
    ): Promise<Page<AuditRecordDTO>> => {
        const response = await axiosInstance.get<Page<AuditRecordDTO>>('/admin/audit', {
            params: {page, size, sortBy, sortDesc, entityFilter}
        });
        return response.data;
    }
};