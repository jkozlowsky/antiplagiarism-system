export interface PropertyChangeDTO {
    property: string;
    oldValue: string;
    newValue: string;
}

export interface AuditRecordDTO {
    commitId: string;
    author: string;
    commitDate: string;
    entityType: string;
    entityId: string;
    changeType: 'ADD' | 'UPDATE' | 'REMOVE';
    changes: PropertyChangeDTO[];
}