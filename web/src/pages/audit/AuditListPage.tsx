import {useEffect, useState} from "react";
import {ChevronLeft, ChevronRight, ChevronsUpDownIcon, RefreshCw} from "lucide-react";
import {useTranslation} from "react-i18next";
import {auditService} from "../../services/auditService";
import type {AuditRecordDTO} from "../../types/audit.types";
import type {Page} from "../../types/user.types";
import {useBreadcrumb} from "../../contexts/BreadcrumbContext.tsx";

export default function AuditListPage() {
    const {t} = useTranslation();

    const [data, setData] = useState<Page<AuditRecordDTO> | null>(null);
    const [page, setPage] = useState(0);
    const [size, setSize] = useState<number>(10);
    const [sortBy, setSortBy] = useState<string>('commitDate');
    const [sortDesc, setSortDesc] = useState<boolean>(true);
    const [entityFilter, setEntityFilter] = useState<string>("ALL");
    const {setDynamicBreadcrumb} = useBreadcrumb();

    const [isLoading, setIsLoading] = useState(false);

    const fetchLogs = async () => {
        setIsLoading(true);
        try {
            const result = await auditService.getAuditLogs(page, size, sortBy, sortDesc, entityFilter);
            setData(result);
        } catch {
        } finally {
            setIsLoading(false);
        }
    };

    useEffect(() => {
        setDynamicBreadcrumb('sidebar.auditLogs');
        return () => setDynamicBreadcrumb(null);
    }, [setDynamicBreadcrumb]);

    useEffect(() => {
        fetchLogs();
    }, [page, size, sortBy, sortDesc, entityFilter]);

    const handleRefresh = () => {
        if (page !== 0) {
            setPage(0);
        } else {
            fetchLogs();
        }
    };

    const handleNextPage = () => {
        if (data && !data.last) setPage(prev => prev + 1);
    };

    const handlePrevPage = () => {
        if (page > 0) setPage(prev => prev - 1);
    };

    const handleSort = (column: string) => {
        if (sortBy === column) {
            setSortDesc(prev => !prev);
        } else {
            setSortBy(column);
            setSortDesc(true);
        }
        setPage(0);
    };

    const handleSizeChange = (newSize: number) => {
        setSize(newSize);
        setPage(0);
    };

    const formatDate = (dateString?: string | null) => {
        if (!dateString) return "-";
        return dateString.replace('T', ' ').substring(0, 16);
    };

    const renderActionBadge = (changeType: string) => {
        let colors = "bg-gray-100 text-gray-800";
        if (changeType === 'ADD') colors = "bg-teal-100 text-teal-800";
        if (changeType === 'UPDATE') colors = "bg-amber-100 text-amber-800";
        if (changeType === 'REMOVE') colors = "bg-red-100 text-[#7A1014]";

        return (
            <span className={`inline-block px-3 py-1 ${colors} text-[10px] font-bold rounded-full tracking-wider`}>
                {changeType}
            </span>
        );
    };

    const availableSizes = [5, 10, 20, 50];
    if (!availableSizes.includes(size)) {
        availableSizes.push(size);
        availableSizes.sort((a, b) => a - b);
    }

    return (
        <div className="min-h-screen bg-base p-8">
            <div className="max-w-7xl mx-auto">
                <div className="flex flex-col md:flex-row justify-between items-start md:items-center mb-8 gap-4">
                    <div>
                        <h1 className="text-3xl font-bold text-primary mb-2">
                            {t('audit.title', 'Historia zmian w systemie')}
                        </h1>
                        <p className="text-secondary text-sm max-w-2xl">
                            {t('audit.subtitle', 'Zarządzanie logami audytowymi i historią edycji encji w bazie danych.')}
                        </p>
                    </div>
                    <div className="flex items-center gap-3">
                        <select
                            value={entityFilter}
                            onChange={(e) => {
                                setEntityFilter(e.target.value);
                                setPage(0);
                            }}
                            className="px-4 py-2 bg-surface border border-border rounded-md text-sm font-semibold text-primary focus:ring-2 focus:ring-brand outline-none transition-all"
                        >
                            <option value="ALL">{t('audit.filter.all', 'All entities')}</option>
                            <option value="Account">Account</option>
                            <option value="Subject">Subject</option>
                            <option value="RulePreset">RulePreset</option>
                            <option value="Schedule">Schedule</option>
                            <option value="SubjectAssignment">SubjectAssignment</option>
                        </select>
                        <button
                            onClick={handleRefresh}
                            disabled={isLoading}
                            className="flex items-center gap-2 px-4 py-2 bg-surface border border-border hover:bg-active disabled:opacity-50 rounded-md text-sm font-bold text-primary transition-colors"
                        >
                            <RefreshCw size={16} className={isLoading ? "animate-spin" : ""}/>
                            {t('common.refresh', 'Odśwież')}
                        </button>
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
                            <tr className="border-b border-border cursor-pointer select-none">
                                <th className="py-6 px-8 text-xs font-bold text-secondary uppercase tracking-widest"
                                    onClick={() => handleSort('commitDate')}>
                                    <div className="flex items-center gap-2">
                                        {t('audit.table.date', 'Data')}
                                        <ChevronsUpDownIcon className="h-4 w-4"/>
                                    </div>
                                </th>
                                <th className="py-6 px-8 text-xs font-bold text-secondary uppercase tracking-widest">
                                    {t('audit.table.author', 'Autor')}
                                </th>
                                <th className="py-6 px-8 text-xs font-bold text-secondary uppercase tracking-widest"
                                    onClick={() => handleSort('entityType')}>
                                    <div className="flex items-center gap-2">
                                        {t('audit.table.entity', 'Typ Encji')}
                                        <ChevronsUpDownIcon className="h-4 w-4"/>
                                    </div>
                                </th>
                                <th className="py-6 px-8 text-xs font-bold text-secondary uppercase tracking-widest">
                                    {t('audit.table.entityId', 'ID Obiektu')}
                                </th>
                                <th className="py-6 px-8 text-xs font-bold text-secondary uppercase tracking-widest">
                                    {t('audit.table.action', 'Akcja')}
                                </th>
                                <th className="py-6 px-8 text-xs font-bold text-secondary uppercase tracking-widest">
                                    {t('audit.table.modifications', 'Modyfikacje pól')}
                                </th>
                            </tr>
                            </thead>
                            <tbody className="divide-y divide-border">
                            {data?.content?.length === 0 ? (
                                <tr>
                                    <td colSpan={6} className="py-8 text-center text-secondary font-medium">
                                        {t('userList.noResults', 'Brak wyników')}
                                    </td>
                                </tr>
                            ) : (
                                data?.content?.map((row, idx) => (
                                    <tr key={`${row.commitId}-${idx}`}
                                        className="hover:bg-base transition-colors align-top">
                                        <td className="py-4 px-8 text-sm text-secondary whitespace-nowrap">{formatDate(row.commitDate)}</td>
                                        <td className="py-4 px-8 text-sm text-primary font-medium whitespace-nowrap">{row.author}</td>
                                        <td className="py-4 px-8 text-sm text-primary font-bold whitespace-nowrap">{row.entityType}</td>

                                        <td className="py-4 px-8 text-sm text-secondary font-mono whitespace-nowrap"
                                            title={row.entityId}>
                                            {row.entityId && row.entityId.length > 8 ? `${row.entityId.substring(0, 8)}...` : row.entityId}
                                        </td>

                                        <td className="py-4 px-8 align-middle whitespace-nowrap">
                                            {renderActionBadge(row.changeType)}
                                        </td>

                                        <td className="py-4 px-8 text-sm text-gray-700">
                                            {row.changeType === 'ADD' && (
                                                <span
                                                    className="text-teal-600 font-semibold italic">{t('audit.details.created', 'Inicjalizacja / Utworzenie obiektu')}</span>
                                            )}
                                            {row.changeType === 'REMOVE' && (
                                                <span
                                                    className="text-red-600 font-semibold italic">{t('audit.details.deleted', 'Usunięcie obiektu z systemu')}</span>
                                            )}
                                            {row.changeType === 'UPDATE' && (
                                                <div
                                                    className="flex flex-col gap-2 max-w-xl bg-base/40 p-3 rounded-lg border border-border/50">
                                                    {row.changes?.map((c, cIdx) => (
                                                        <div key={cIdx}
                                                             className="text-xs flex flex-wrap items-center gap-1 border-b border-border/30 pb-1 last:border-0 last:pb-0">
                                                            <span
                                                                className="font-mono font-bold text-secondary bg-surface px-1.5 py-0.5 rounded border border-border/60">{c.property}:</span>
                                                            <span
                                                                className="text-red-500 line-through bg-red-50 px-1 rounded truncate max-w-[120px]"
                                                                title={c.oldValue}>{c.oldValue || 'null'}</span>
                                                            <span className="text-secondary font-bold">→</span>
                                                            <span
                                                                className="text-teal-600 font-semibold bg-teal-50 px-1 rounded truncate max-w-[120px]"
                                                                title={c.newValue}>{c.newValue}</span>
                                                        </div>
                                                    ))}
                                                </div>
                                            )}
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
                                {t('userList.pagination.page', 'Strona')} {data ? (data.number + 1) : 0} {t('userList.pagination.of', 'z')} {data ? data.totalPages : 0}
                            </span>
                            <div className="flex items-center gap-2 text-sm text-primary font-medium">
                                <label htmlFor="pageSize">{t('userList.pagination.showing', 'Pokaż:')}</label>
                                <select
                                    id="pageSize"
                                    value={size}
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
                                onClick={handlePrevPage}
                                disabled={page === 0 || isLoading}
                                className="flex items-center gap-1 px-3 py-1.5 bg-surface border border-border rounded-md text-sm font-bold text-primary hover:bg-base disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
                            >
                                <ChevronLeft size={16}/>
                                {t('userList.pagination.prev', 'Poprzednia')}
                            </button>
                            <button
                                onClick={handleNextPage}
                                disabled={!data || data.last || isLoading}
                                className="flex items-center gap-1 px-3 py-1.5 bg-surface border border-border rounded-md text-sm font-bold text-primary hover:bg-base disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
                            >
                                {t('userList.pagination.next', 'Następna')}
                                <ChevronRight size={16}/>
                            </button>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    );
}