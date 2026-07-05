import api from '../api/auth/middleware';

export interface ChangeOwnPasswordPayload {
    oldPassword: string;
    newPassword: string;
}

export interface ChangeOtherPasswordPayload {
    newPassword: string;
}

export const getAccountByLogin = async (login: string) => {
    const response = await api.get(`/account/login/${login}`);
    return response.data;
};

export const changeOwnPassword = async (data: ChangeOwnPasswordPayload, version: string) => {
    return await api.post('/account/security/password', data, {
        headers: {
            'If-Match': version
        }
    });
};

export const changeOtherPassword = async (login: string, data: ChangeOtherPasswordPayload, version: string) => {
    return await api.post(`/account/${login}/password`, data, {
        headers: {
            'If-Match': version
        }
    });
};

export const changeLanguage = async (languageCode: string) => {
    return await api.post(
        `/account/language`, null,
        {
            params: {
                language: languageCode
            }
        });
}