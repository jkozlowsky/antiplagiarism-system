export interface RulePresetDTO {
    id: string;
    raportLevelName: string;
    studentTicketCount: number;
    minimumTokensMatch: number;
    enableNormalization: boolean;
    version: number;
    versionHash: string;
}

export interface CreateRulePresetDTO {
    raportLevelName: string;
    studentTicketCount: number;
    minimumTokensMatch: number;
    enableNormalization: boolean;
}

export interface UpdateRulePresetDTO {
    raportLevelName: string;
    studentTicketCount: number;
    minimumTokensMatch: number;
    enableNormalization: boolean;
}