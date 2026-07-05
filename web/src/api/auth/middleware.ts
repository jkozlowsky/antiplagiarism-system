import axios from 'axios';
import type {FailedQueueItem} from "../../types/failed_query.type.ts";
import {toast} from "react-toastify";
import i18n from "i18next";
import { router } from "../../routes";
import {PATHS} from "../../routes/paths.ts";

// const baseURL = 'http://localhost:8081/api';
const baseURL = '/api';
// przed wrzucaniem na produkcję podmienić :)

const axiosInstance = axios.create({
    baseURL: baseURL,
    headers: {'Content-Type': 'application/json'},
});

let isRefreshing = false;
let failedQueue: FailedQueueItem[] = [];

const processQueue = (error: unknown, token: string | null = null) => {
    failedQueue.forEach(prom => {
        if (error) {
            prom.reject(error);
        } else {
            prom.resolve(token);
        }
    });
    failedQueue = [];
};

axiosInstance.interceptors.request.use((config) => {
    const token = sessionStorage.getItem('access_token');
    if (token) {
        config.headers.Authorization = `Bearer ${token}`;
    }
    config.headers['Accept-Language'] = localStorage.getItem('i18nextLng') || 'pl';
    return config;
});

axiosInstance.interceptors.response.use(
    (response) => {
        const method = response.config.method?.toUpperCase();
        const url = response.config.url || '';
        const silentUrls = ['/auth/refresh', '/auth/login', '/auth/check2FA', '/auth/google'];
        const isSilentUrl = silentUrls.some(silent => url.includes(silent));
        if (['POST', 'PUT', 'PATCH', 'DELETE'].includes(method || '') && !isSilentUrl) {
            const backendMessage = response.data?.message;
            const toastMessage = backendMessage || i18n.t('success.generic');
            toast.success(toastMessage, {
                autoClose: false,
                closeOnClick: true,
                toastId: `success-${method}-${url}`
            });
        }
        return response;
    },
    async (error) => {
        const originalRequest = error.config;

        if (error.response?.status === 401 &&
            !originalRequest._retry &&
            !originalRequest.url?.includes('/auth/login')) {
            if (isRefreshing) {
                return new Promise(function (resolve, reject) {
                    failedQueue.push({resolve, reject});
                }).then(token => {
                    originalRequest.headers['Authorization'] = 'Bearer ' + token;
                    return axiosInstance(originalRequest);
                }).catch(err => {
                    return Promise.reject(err);
                });
            }

            originalRequest._retry = true;
            isRefreshing = true;
            const refToken = sessionStorage.getItem('refresh_token');
            if (refToken) {
                try {
                    const res = await axiosInstance.post(`/auth/refresh`, {
                        refreshToken: refToken
                    });

                    if (res.status === 200) {
                        const {token: accessToken, refreshToken: newRefreshToken} = res.data;

                        sessionStorage.setItem('access_token', accessToken);
                        sessionStorage.setItem('refresh_token', newRefreshToken);

                        processQueue(null, accessToken);

                        originalRequest.headers.Authorization = `Bearer ${accessToken}`;
                        return axiosInstance(originalRequest);
                    }
                } catch (refreshError) {
                    processQueue(refreshError, null);
                    sessionStorage.clear();
                    toast.warn(i18n.t('errors.unauthorized'), {
                        toastId: 'session-expired-toast'
                    });
                    router.navigate(PATHS.LOGIN || '/login');
                    return Promise.reject(refreshError);
                } finally {
                    isRefreshing = false;
                }
            } else {
                sessionStorage.clear();
                toast.warn(i18n.t('errors.unauthorized'), {
                    toastId: 'session-expired-toast'
                });
                isRefreshing = false;
                processQueue(new Error('Session expired'), null);
                router.navigate(PATHS.LOGIN || '/login');
                return Promise.reject(error);
            }
        }
        if (error.response) {
            const status = error.response.status;
            if (status !== 401 && status !== 400) {
                const backendMessage = error.response.data;
                let toastMessage = backendMessage;
                if (!toastMessage) {
                    let errorMessageKey = 'errors.generic';
                    switch (status) {
                        case 403:
                            errorMessageKey = 'errors.forbidden';
                            break;
                        case 404:
                            errorMessageKey = 'errors.notFound';
                            break;
                        case 409:
                            errorMessageKey = 'errors.conflict';
                            break;
                        case 412:
                            errorMessageKey = 'errors.preconditionFailed';
                            break;
                        case 500:
                        case 502:
                        case 503:
                        case 504:
                            errorMessageKey = 'errors.serverError';
                            break;
                    }
                    toastMessage = i18n.t(errorMessageKey);
                }

                toast.error(toastMessage, {
                    toastId: toastMessage
                });
            }
        } else if (error.request) {
            toast.error(i18n.t('errors.networkDown'), {
                toastId: 'global-network-error'
            });
        }
        return Promise.reject(error);
    }
);

export default axiosInstance;