import axiosInstance from "../api/auth/middleware";
import type {AccountDTO, ChangeEmailDTO} from "../types/user.types.ts";

export const emailChangeService = {
    requestEmailChange: async () => {
        const response = await axiosInstance.post('/account/email/change');
        return response.data;
    },

    confirmEmailChange: async (token: string, password: string, newEmail: string) => {
        const response = await axiosInstance.post('/account/email/confirm', {
            token,
            password,
            newEmail
        });
        return response.data;
    },

    changeEmailByAdmin: async (login: string, dto: ChangeEmailDTO, versionHash: string): Promise<AccountDTO> => {
        const response = await axiosInstance.patch(`/account/${login}/email`,
            dto,
            {
                headers: {
                    "If-Match": versionHash
                }
            }
        );
        return response.data;
    },

    resendEmailChangeRequest: async () => {
        const response = await axiosInstance.post('/account/email/change/resend');
        return response.data;
    },

    revertEmailChange: async (token: string) => {
        const response = await axiosInstance.post(`/account/email/revert?token=${token}`);
        return response.data;
    }
};