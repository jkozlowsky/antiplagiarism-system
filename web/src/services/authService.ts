import axiosInstance from "../api/auth/middleware";

export const authService = {
    login: async (login: string, password: string) => {
        const response = await axiosInstance.post('/auth/login', { login, password });
        return {response: response.data, status: response.status};
    },
    verify2FA: async (login: string, auth2F: string) => {
        const response = await axiosInstance.post('/auth/check2FA', { login, auth2F });
        return response.data;
    },
    resend2FA: async (login: string) => {
        const response = await axiosInstance.post('/auth/resend2FA', { login });
        return response.data;
    },
    refreshSession: async (refreshToken: string) => {
        const response = await axiosInstance.post('/auth/refresh', { refreshToken });
        return response.data;
    },
    loginWithEmail: async (email: string) => {
        const response = await axiosInstance.post('/auth/loginEmail', { email });
        return { response: response.data, status: response.status };
    },
    verifyEmailCode: async (token: string, email: string) => {
        const response = await axiosInstance.post('/auth/checkEmail', { token, email });
        return response.data;
    }
};

export const loginWithGoogle = async (idToken: string) => {
    const response = await axiosInstance.post('/auth/google', { idToken });
    return response.data;
};