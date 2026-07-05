export interface FailedQueueItem {
    resolve: (value: string | null) => void;
    reject: (reason?: unknown) => void;
}