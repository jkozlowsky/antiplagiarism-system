export interface AccessLevelDTO {
    accessLevelName: string;
    active: boolean;
}

export interface AccountDTO {
    id: string;
    name: string;
    surname: string;
    login: string;
    email: string;
    lastLoginSuccessDateTime?: string | null;
    lastLoginFailureDateTime?: string | null;
    versionHash: string;
    isBlocked: boolean;
    verified?: boolean;
    createdAt?: string | null;
    updatedAt?: string | null;
    createdBy?: string | null;
    modifiedBy?: string | null;

    listPageSize?: number | null;
    listSortBy?: string | null;
    listSortDesc?: boolean | null;
}

export interface AccountWithAccessLevelsDTO {
    account: AccountDTO;
    accessLevels: AccessLevelDTO[];
}

export interface SortObject {
    property: string;
    direction: string;
    ascending: boolean;
    descending: boolean;
}

export interface PageableObject {
    sort?: SortObject[];
}

export interface Page<T> {
    content: T[];
    totalElements: number;
    totalPages: number;
    size: number;
    number: number;
    first: boolean;
    last: boolean;
    empty: boolean;
    sort?: SortObject[];
    pageable?: PageableObject;
}

export interface ChangeEmailDTO {
    email: string
}

export interface UpdateAccountDTO {
    name: string;
    surname: string;
}

export interface HalLink {
    href: string;
}

export interface HalLinks {
    first?: HalLink;
    prev?: HalLink;
    self?: HalLink;
    next?: HalLink;
    last?: HalLink;
}

export interface HalPageInfo {
    size: number;
    totalElements: number;
    totalPages: number;
    number: number;
}

export interface AccountsHalResponse {
    _embedded?: {
        accountWithAccessLevelsDTOList: AccountWithAccessLevelsDTO[];
    };
    _links?: HalLinks;
    page: HalPageInfo;
}

export interface GetUsersParams {
    page: number;
    size?: number;
    phrase?: string;
    sortBy?: string;
    sortDesc?: boolean;
}
