import axiosInstance from '../api/auth/middleware.ts';
import type {ScheduleDTO, ScheduleResponse, CreateSchedulePayload, UpdateSchedulePayload} from "../types/schedule.types.ts";


export const scheduleService = {
    getSchedulesForSubject: async (subjectId: string): Promise<ScheduleResponse> => {
        const response = await axiosInstance.get<ScheduleDTO[]>(`/schedules/subject/${subjectId}`);
        const serverDateHeader = response.headers['date']; // Pobieramy czas z nagłówka by czas się nie rozjechał z backendem.
        const serverTimeMs = serverDateHeader ? new Date(serverDateHeader).getTime() : Date.now();

        return { schedules: response.data, serverTimeMs: serverTimeMs }
    },
    createSchedule: async (subjectId: string, payload: CreateSchedulePayload): Promise<ScheduleDTO> => {
        const fullDto: Partial<ScheduleDTO> = {
            id: null as any,
            scheduleDateTime: payload.scheduleDateTime,
            tag: payload.tag,
            isExecuted: false,
            hasFailed: false,
            versionHash: ""
        }

        const response = await axiosInstance
            .post<ScheduleDTO>(`/schedules/subject/${subjectId}`, fullDto);
        return response.data
    },
    deleteSchedule: async (scheduleId: string, versionHash: string): Promise<void> => {
        await axiosInstance.delete(`/schedules/${scheduleId}`, {
            headers: {
                'If-Match': versionHash
            }
        });
    },

    updateSchedule: async (scheduleId: string, data: UpdateSchedulePayload): Promise<void> => {
        await axiosInstance.put(`/schedules/${scheduleId}`, data, {
            headers: {
                'If-Match': data.versionHash
            }
        });
    }
}