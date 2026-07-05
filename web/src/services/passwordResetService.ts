import axiosInstance from "../api/auth/middleware";

export const passwordResetService = {
    /**
     * Step 1: Send the user an email with a reset link
     */
    requestReset: async (email: string) => {
        const response = await axiosInstance.post('/auth/reset/request', {
            email
        });
        return response.data;
    },

    /**
     * Step 2: Submit the new password using the token from the email link
     */
    confirmReset: async (token: string, newPassword: string) => {
        const response = await axiosInstance.post('/auth/reset/confirm', {
            token,
            newPassword
        });
        return response.data;
    }
};