import {useEffect, useState, type KeyboardEvent} from "react";
import {
    RefreshCw,
    Filter,
    ChevronLeft,
    ChevronRight,
    ChevronsLeft,
    ChevronsRight,
    ChevronsUpDownIcon
} from "lucide-react";
import {useTranslation} from "react-i18next";
import {useNavigate} from "react-router-dom";
import {userService} from "../../services/userService";
import {PATHS} from "../../routes/paths";
import type {AccountsHalResponse, AccountDTO} from "../../types/user.types";
import ChangePasswordModal from "./ChangeOtherPasswordModal";

export default function UserListPage() {
    const {t} = useTranslation();
    const navigate = useNavigate();

    const [data, setData] = useState<AccountsHalResponse | null>(null);

    const [size, setSize] = useState<number | undefined>(undefined);
    const [sortBy, setSortBy] = useState<string | undefined>(undefined);
    const [sortDesc, setSortDesc] = useState<boolean | undefined>(undefined);

    const [phrase, setPhrase] = useState("");
    const [searchPhrase, setSearchPhrase] = useState("");
    const [isLoading, setIsLoading] = useState(false);
    const [selectedUser, setSelectedUser] = useState<AccountDTO | null>(null);

    const [isProfileLoaded, setIsProfileLoaded] = useState(false);

    const getLoginFromToken = () => {
        try {
            const token = localStorage.getItem('token') || sessionStorage.getItem('token');
            if (!token) return null;
            const base64Url = token.split('.')[1];
            const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
            const jsonPayload = decodeURIComponent(atob(base64).split('').map(function (c) {
                return '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2);
            }).join(''));
            return JSON.parse(jsonPayload).sub;
        } catch {
            return null;
        }
    };

    useEffect(() => {
        const loadProfile = async () => {
            try {
                const currentLogin = getLoginFromToken();
                if (currentLogin) {
                    const profile = await userService.getAccountByLogin(currentLogin);
                    if (profile?.account) {
                        if (profile.account.listSortBy) {
                            setSortBy(profile.account.listSortBy);
                        }
                        if (profile.account.listSortDesc !== undefined && profile.account.listSortDesc !== null) {
                            setSortDesc(profile.account.listSortDesc);
                        }
                        if (profile.account.listPageSize) {
                            setSize(profile.account.listPageSize);
                        }
                    }
                }
            } catch { /* empty */
            } finally {
                setIsProfileLoaded(true);
            }
        };
        loadProfile();
    }, []);

    useEffect(() => {
        if (!isProfileLoaded) return;
        setIsLoading(true);
        userService.getUsers({page: 0, size, phrase: searchPhrase, sortBy, sortDesc})
            .then(setData)
            .catch(() => {
            })
            .finally(() => setIsLoading(false));
    }, [size, searchPhrase, sortBy, sortDesc, isProfileLoaded]);

    const handleRefresh = () => {
        setSearchPhrase(phrase);
        setIsLoading(true);
        userService.getUsers({page: 0, size, phrase, sortBy, sortDesc})
            .then(setData)
            .catch(() => {
            })
            .finally(() => setIsLoading(false));
    };

    const handleKeyDown = (e: KeyboardEvent<HTMLInputElement>) => {
        if (e.key === 'Enter') handleRefresh();
    };

    const handleFirstPage = () => {
        if (data?._links?.first?.href) {
            setIsLoading(true);
            userService.getUsers(data._links.first.href)
                .then(setData)
                .catch(() => {
                })
                .finally(() => setIsLoading(false));
        }
    };

    const handleLastPage = () => {
        if (data?._links?.last?.href) {
            setIsLoading(true);
            userService.getUsers(data._links.last.href)
                .then(setData)
                .catch(() => {
                })
                .finally(() => setIsLoading(false));
        }
    };

    const handleNextPage = () => {
        if (data?._links?.next?.href) {
            setIsLoading(true);
            userService.getUsers(data._links.next.href)
                .then(setData)
                .catch(() => {
                })
                .finally(() => setIsLoading(false));
        }
    };

    const handlePrevPage = () => {
        if (data?._links?.prev?.href) {
            setIsLoading(true);
            userService.getUsers(data._links.prev.href)
                .then(setData)
                .catch(() => {
                })
                .finally(() => setIsLoading(false));
        }
    };

    const handleSort = (column: string) => {
        if (sortBy === column) {
            setSortDesc(prev => !prev);
        } else {
            setSortBy(column);
            setSortDesc(false);
        }
    };

    const handleSizeChange = (newSize: number) => {
        setSize(newSize);
    };

    const formatDate = (dateString?: string | null) => {
        if (!dateString) return "-";
        const date = new Date(dateString);
        return date.toISOString().replace('T', ' ').substring(0, 16);
    };

    const renderRoleBadge = (roleName: string) => {
        const isAdmin = roleName === "ROLE_ADMIN" || roleName === "ADMINISTRATOR";
        const colors = isAdmin ? "bg-red-100 text-[#7A1014]" : "bg-teal-100 text-teal-800";
        const getLabel = () => {
            if (isAdmin) return t('userEdit.roles.adminBadge');
            if (roleName === "ROLE_TEACHER" || roleName === "TEACHER") return t('userEdit.roles.teacher').toUpperCase();
            if (roleName === "ROLE_STUDENT" || roleName === "STUDENT") return t('userEdit.roles.student').toUpperCase();
            return roleName;
        };
        return (
            <span
                key={roleName}
                className={`inline-block px-3 py-1 ${colors} text-[10px] font-bold rounded-full tracking-wider mb-1`}
            >
                {getLabel()}
            </span>
        );
    };

    const availableSizes = [5, 10, 20, 50];
    const currentSize = size !== undefined ? size : (data?.page?.size || 10);

    if (!availableSizes.includes(currentSize)) {
        availableSizes.push(currentSize);
        availableSizes.sort((a, b) => a - b);
    }

    if (!isProfileLoaded) {
        return (
            <div className="min-h-screen bg-gray-50 flex items-center justify-center">
                <RefreshCw className="animate-spin text-[#7A1014]" size={40}/>
            </div>
        );
    }

    return (
        <div className="min-h-screen bg-base p-8">
            <div className="max-w-7xl mx-auto">
                <div className="flex flex-col md:flex-row justify-between items-start md:items-center mb-8 gap-4">
                    <div>
                        <h1 className="text-3xl font-bold text-primary mb-2">{t('userList.title')}</h1>
                        <p className="text-secondary text-sm max-w-2xl">{t('userList.subtitle')}</p>
                    </div>
                    <div className="flex items-center gap-3">
                        <button
                            onClick={handleRefresh}
                            disabled={isLoading}
                            className="flex items-center gap-2 px-4 py-2 bg-surface border border-border hover:bg-active disabled:opacity-50 rounded-md text-sm font-bold text-primary transition-colors"
                        >
                            <RefreshCw size={16} className={isLoading ? "animate-spin" : ""}/>
                            {t('userList.refresh')}
                        </button>
                        <div className="relative">
                            <div
                                className="absolute inset-y-0 left-3 flex items-center pointer-events-none text-secondary">
                                <Filter size={16}/>
                            </div>
                            <input
                                type="text"
                                placeholder={t('userList.filterPlaceholder')}
                                value={phrase}
                                onChange={(e) => setPhrase(e.target.value)}
                                onKeyDown={handleKeyDown}
                                className="pl-10 pr-4 py-2 bg-surface border border-border rounded-md text-sm font-semibold text-primary focus:ring-2 focus:ring-brand outline-none w-64 transition-all"
                            />
                        </div>
                    </div>
                </div>
                <div className="bg-surface rounded-2xl shadow-sm border border-border overflow-hidden flex flex-col">
                    <div className="overflow-x-auto relative">
                        {isLoading && (
                            <div
                                className="absolute inset-0 bg-surface/50 backdrop-blur-[1px] flex items-center justify-center z-10">
                                <RefreshCw className="animate-spin text-brand" size={32}/>
                            </div>
                        )}
                        <table className="w-full text-left border-collapse relative">
                            <thead>
                            <tr className="border-b border-border  cursor-pointer select-none">
                                <th className="py-6 px-8 text-xs font-bold text-secondary uppercase tracking-widest"
                                    onClick={() => handleSort('accessLevel')}>
                                    <div className="flex items-center gap-2">
                                        {t('userList.table.role')}
                                        <ChevronsUpDownIcon className="h-4 w-4"/>
                                    </div>
                                </th>

                                <th className="py-6 px-8 text-xs font-bold text-secondary uppercase tracking-widest"
                                    onClick={() => handleSort('name')}>
                                    <div className="flex items-center gap-2">
                                        {t('userList.table.firstName')}
                                        <ChevronsUpDownIcon className="h-4 w-4"/>
                                    </div>
                                </th>

                                <th className="py-6 px-8 text-xs font-bold text-secondary uppercase tracking-widest"
                                    onClick={() => handleSort('surname')}>
                                    <div className="flex items-center gap-2">
                                        {t('userList.table.surname')}
                                        <ChevronsUpDownIcon className="h-4 w-4"/>
                                    </div>
                                </th>

                                <th className="py-6 px-8 text-xs font-bold text-secondary uppercase tracking-widest"
                                    onClick={() => handleSort('login')}>
                                    <div className="flex items-center gap-2">
                                        {t('userList.table.login')}
                                        <ChevronsUpDownIcon className="h-4 w-4"/>
                                    </div>
                                </th>

                                <th className="py-6 px-8 text-xs font-bold text-secondary uppercase tracking-widest"
                                    onClick={() => handleSort('email')}>
                                    <div className="flex items-center gap-2">
                                        {t('userList.table.email')}
                                        <ChevronsUpDownIcon className="h-4 w-4"/>
                                    </div>
                                </th>

                                <th className="py-6 px-8 text-xs font-bold text-secondary uppercase tracking-widest"
                                    onClick={() => handleSort('lastLoginSuccessDateTime')}>
                                    <div className="flex items-center gap-2">
                                        {t('userList.table.lastLogin')}
                                        <ChevronsUpDownIcon className="h-4 w-4"/>
                                    </div>
                                </th>

                                <th className="py-6 px-8 text-xs font-bold text-secondary uppercase tracking-widest">
                                    {t('userList.table.actions')}
                                </th>
                            </tr>
                            </thead>
                            <tbody className="divide-y divide-border">
                            {(!data?._embedded?.accountWithAccessLevelsDTOList || data._embedded.accountWithAccessLevelsDTOList.length === 0) ? (
                                <tr>
                                    <td colSpan={7} className="py-8 text-center text-secondary font-medium">
                                        {t('userList.noResults', 'Brak wyników')}
                                    </td>
                                </tr>
                            ) : (
                                data._embedded.accountWithAccessLevelsDTOList.map((row) => (
                                    <tr key={row.account.id} className="hover:bg-base transition-colors">
                                        <td className="py-4 px-8 align-middle">
                                            <div className="flex flex-col items-start">
                                                {row.accessLevels.filter(al => al.active).sort((a, b) => a.accessLevelName.localeCompare(b.accessLevelName)).map(al => renderRoleBadge(al.accessLevelName))}
                                            </div>
                                        </td>
                                        <td className="py-4 px-6 text-sm text-primary font-medium">{row.account.name}</td>
                                        <td className="py-4 px-6 text-sm text-primary font-medium">{row.account.surname}</td>
                                        <td className="py-4 px-6 text-sm text-secondary">{row.account.login}</td>
                                        <td className="py-4 px-6 text-sm text-secondary">{row.account.email}</td>
                                        <td className="py-4 px-6 text-sm text-secondary">{formatDate(row.account.lastLoginSuccessDateTime)}</td>
                                        <td className="py-4 px-8 gap-4">
                                            <button
                                                className="text-sm font-bold text-brand hover:text-brand-hover cursor-pointer transition-colors"
                                                onClick={() => navigate(PATHS.USER_EDIT.replace(':id', row.account.id))}
                                            >
                                                {t('userList.table.edit')}
                                            </button>
                                        </td>
                                    </tr>
                                ))
                            )}
                            </tbody>
                        </table>
                    </div>
                    <div
                        className="flex flex-col md:flex-row items-center justify-between px-8 py-4 border-t border-border bg-surface gap-4">
                        <div className="flex items-center gap-4">
                            <span className="text-sm text-primary font-medium">
                                {t('userList.pagination.page')} {data?.page ? (data.page.number + 1) : 0} {t('userList.pagination.of')} {data?.page ? data.page.totalPages : 0}
                            </span>
                            <div className="flex items-center gap-2 text-sm text-primary font-medium">
                                <label htmlFor="pageSize">{t('userList.pagination.showing')}</label>
                                <select
                                    id="pageSize"
                                    value={currentSize}
                                    onChange={(e) => handleSizeChange(Number(e.target.value))}
                                    className="border border-border rounded-md px-2 py-1 text-sm bg-surface focus:outline-none focus:ring-2 focus:ring-[#7A1014]"
                                >
                                    {availableSizes.map(s => (
                                        <option key={s} value={s}>{s}</option>
                                    ))}
                                </select>
                            </div>
                        </div>
                        <div className="flex gap-2">
                            <button
                                onClick={handleFirstPage}
                                disabled={!data?.page || data.page.number === 0 || isLoading}
                                className="flex items-center gap-1 px-3 py-1.5 bg-surface border border-border rounded-md text-sm font-bold text-primary hover:bg-base disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
                            >
                                <ChevronsLeft size={16}/>
                                {t('userList.pagination.first', 'Pierwsza')}
                            </button>
                            <button
                                onClick={handlePrevPage}
                                disabled={!data?._links?.prev || isLoading}
                                className="flex items-center gap-1 px-3 py-1.5 bg-surface border border-border rounded-md text-sm font-bold text-primary hover:bg-base disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
                            >
                                <ChevronLeft size={16}/>
                                {t('userList.pagination.prev', 'Poprzednia')}
                            </button>
                            <button
                                onClick={handleNextPage}
                                disabled={!data?._links?.next || isLoading}
                                className="flex items-center gap-1 px-3 py-1.5 bg-surface border border-border rounded-md text-sm font-bold text-primary hover:bg-base disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
                            >
                                {t('userList.pagination.next', 'Kolejna')}
                                <ChevronRight size={16}/>
                            </button>
                            <button
                                onClick={handleLastPage}
                                disabled={!data?.page || data.page.number === data.page.totalPages - 1 || data.page.totalPages === 0 || isLoading}
                                className="flex items-center gap-1 px-3 py-1.5 bg-surface border border-border rounded-md text-sm font-bold text-primary hover:bg-base disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
                            >
                                {t('userList.pagination.last', 'Ostatnia')}
                                <ChevronsRight size={16}/>
                            </button>
                        </div>
                    </div>
                </div>
            </div>

            {selectedUser && (
                <ChangePasswordModal
                    isOpen={true}
                    user={selectedUser}
                    onClose={() => setSelectedUser(null)}
                    onSuccess={() => {
                        setSelectedUser(null);
                        handleRefresh();
                    }}
                />
            )}
        </div>
    );
}