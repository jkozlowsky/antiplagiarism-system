export interface ScheduleDTO {
    id: string;
    scheduleDateTime: string;
    tag: string;
    versionHash: string;
    isExecuted: boolean;
    hasFailed: boolean;
}

export interface ScheduleResponse {
    schedules: ScheduleDTO[];
    serverTimeMs: number;
}

export interface CreateSchedulePayload {
    tag: string;
    scheduleDateTime: string;
}

export interface UpdateSchedulePayload {
    scheduleDateTime: string;
    tag: string;
    versionHash: string;
}

export const ScheduleStatus = {
    EXECUTED: "schedule.status.executed",
    PLANNED: "schedule.status.planned",
    FAILED: "schedule.status.failed",
} as const;

export type ScheduleStatus = typeof ScheduleStatus[keyof typeof ScheduleStatus];
export type FilterStatus = ScheduleStatus | 'ALL';