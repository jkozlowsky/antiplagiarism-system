import axiosInstance from "../api/auth/middleware";
import type {
    AccountWithAccessLevelsDTO,
    UpdateAccountDTO,
    AccountsHalResponse,
    GetUsersParams
} from "../types/user.types";

export const userService = {
    getUsers: async (paramsOrUrl: string | GetUsersParams) => {
        if (typeof paramsOrUrl === 'string') {
            const urlObj = new URL(paramsOrUrl, window.location.origin);
            const response = await axiosInstance.get<AccountsHalResponse>(`/account${urlObj.search}`);
            return response.data;
        }

        const params = new URLSearchParams();
        if (paramsOrUrl.phrase) params.append('phrase', paramsOrUrl.phrase);
        if (paramsOrUrl.page !== undefined) params.append('page', paramsOrUrl.page.toString());
        if (paramsOrUrl.size !== undefined) params.append('size', paramsOrUrl.size.toString());
        if (paramsOrUrl.sortBy !== undefined) params.append('sortBy', paramsOrUrl.sortBy);
        if (paramsOrUrl.sortDesc !== undefined) params.append('sortDesc', paramsOrUrl.sortDesc.toString());

        const response = await axiosInstance.get<AccountsHalResponse>(`/account?${params.toString()}`);
        return response.data;
    },

    getAccountById: async (id: string) => {
        const response = await axiosInstance.get<AccountWithAccessLevelsDTO>(`/account/${id}`);
        return response.data;
    },
    getAccountByLogin: async (login: string | null) => {
        const response = await axiosInstance.get<AccountWithAccessLevelsDTO>(`/account/login/${login}`);
        return response.data;
    },

    grantAccessLevel: async (id: string, accessLevelName: string, versionHash: string) => {
        const response = await axiosInstance.post<AccountWithAccessLevelsDTO>(
            `/account/${id}/access-levels/${accessLevelName}/grant`,
            null,
            {
                headers:
                    {
                        "If-Match": versionHash
                    }
            }
        );
        return response.data;
    },

    revokeAccessLevel: async (id: string, accessLevelName: string, versionHash: string) => {
        const response = await axiosInstance.post<AccountWithAccessLevelsDTO>(
            `/account/${id}/access-levels/${accessLevelName}/revoke`,
            null,
            {
                headers:
                    {
                        "If-Match": versionHash
                    }
            }
        );
        return response.data;
    },

    blockUser: async (id: string, version_hash: string) => {
        const response = await axiosInstance.post(`/account/block/${id}`,
            null,
            {
                headers: {
                    'If-Match': version_hash
                }
            });
        return response.data;
    },

    unblockUser: async (id: string, version_hash: string) => {
        const response = await axiosInstance.post(`/account/unblock/${id}`,
            null,
            {
                headers: {
                    'If-Match': version_hash
                }
            });
        return response.data;
    },

    updateUserDetails: async (id: string, data: UpdateAccountDTO, version_hash: string) => {
        const response = await axiosInstance.patch(`/account/${id}`,
            data,
            {
                headers: {
                    'If-Match': version_hash
                }
            });
        return response.data;
    },

    updateMyDetails: async (data: UpdateAccountDTO, version_hash: string) => {
        const response = await axiosInstance.patch(`/account/update-myself`,
            data,
            {
                headers: {
                    'If-Match': version_hash
                }
            });
        return response.data;
    },

    updateActiveRole: async (data: string) => {
        return await axiosInstance.post(`/account/role/change`, data);
    }
};