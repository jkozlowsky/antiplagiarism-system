import {
    RefreshCw,
    FileText,
    MoreVertical,
    ChevronLeft,
    ChevronRight,
    ArrowUp,
    ArrowDown,
    ArrowUpDown,
    Trash2
} from "lucide-react";
import type {ReportDTO} from "../../types/report.types.ts";
import {useEffect, useState, useCallback} from "react";
import {reportService} from "../../services/reportService.ts";
import {useTranslation} from "react-i18next";
import ConfirmationModal from "../../shared/components/modals/ConfirmationPopup";
import { useNavigate } from "react-router-dom";

export default function ReportsPage() {
    const navigate = useNavigate();
    const [reports, setReports] = useState<ReportDTO[]>([]);
    const [isLoading, setIsLoading] = useState(false);

    const [page, setPage] = useState(0);
    const [size, setSize] = useState(10);
    const [totalPages, setTotalPages] = useState(0);
    const [totalElements, setTotalElements] = useState(0);

    const [sortBy, setSortBy] = useState<string | undefined>(undefined);
    const [sortDesc, setSortDesc] = useState<boolean | undefined>(undefined);

    const [openMenuId, setOpenMenuId] = useState<string | null>(null);

    const [isDeleteModalOpen, setIsDeleteModalOpen] = useState(false);
    const [reportToDelete, setReportToDelete] = useState<string | null>(null);
    const [isDeleting, setIsDeleting] = useState(false);

    const { t, i18n } = useTranslation();

    const fetchReports = useCallback(async () => {
        setIsLoading(true);
        try {
            const pageData = await reportService.getAllReportsForTeacher(page, size, sortBy, sortDesc);
            setReports(pageData.content);
            setTotalPages(pageData.totalPages);
            setTotalElements(pageData.totalElements);
        } catch (error) {
            console.error("Failed to fetch reports", error);
        } finally {
            setIsLoading(false);
        }
    }, [page, size, sortBy, sortDesc]);

    useEffect(() => {
        fetchReports();
    }, [fetchReports]);

    useEffect(() => {
        const closeMenu = () => setOpenMenuId(null);
        document.addEventListener("click", closeMenu);
        return () => document.removeEventListener("click", closeMenu);
    }, []);

    const handleSort = (column: string) => {
        if (sortBy === column) {
            if (sortDesc === false) {
                setSortDesc(true);
            } else if (sortDesc === true) {
                setSortBy(undefined);
                setSortDesc(undefined);
            }
        } else {
            setSortBy(column);
            setSortDesc(false);
        }
        setPage(0);
    };

    const handlePageChange = (newPage: number) => {
        if (newPage >= 0 && newPage < totalPages) {
            setPage(newPage);
        }
    };

    const handleSizeChange = (event: React.ChangeEvent<HTMLSelectElement>) => {
        setSize(Number(event.target.value));
        setPage(0);
    };

    const toggleMenu = (id: string, event: React.MouseEvent) => {
        event.stopPropagation();
        setOpenMenuId(openMenuId === id ? null : id);
    };

    const openDeleteConfirmation = (id: string) => {
        setReportToDelete(id);
        setIsDeleteModalOpen(true);
        setOpenMenuId(null);
    };

    const handleConfirmDelete = async () => {
        if (!reportToDelete) return;

        setIsDeleting(true);
        try {
            await reportService.deleteReport(reportToDelete);
            await new Promise(resolve => setTimeout(resolve, 800));

            await fetchReports();
        } catch (error) {
            console.error("Failed to delete report", error);
        } finally {
            setIsDeleting(false);
            setIsDeleteModalOpen(false);
            setReportToDelete(null);
        }
    };

    const handleCancelDelete = () => {
        setIsDeleteModalOpen(false);
        setReportToDelete(null);
    };

    const formatDate = (isoString: string) => {
        const date = new Date(isoString);
        const optionsDate: Intl.DateTimeFormatOptions = {month: 'short', day: 'numeric', year: 'numeric'};
        const optionsTime: Intl.DateTimeFormatOptions = {hour: '2-digit', minute: '2-digit', hour12: false};

        const lang = i18n.resolvedLanguage || i18n.language || 'en';

        return `${date.toLocaleDateString(lang, optionsDate)} • ${date.toLocaleTimeString(lang, optionsTime)}`;
    };

    const getSimilarityBadge = (similarity: number) => {
        const roundedSimilarity = Math.round(similarity);

        let badgeClass = "px-3 py-1 text-xs font-bold rounded-full whitespace-nowrap ";
        if (roundedSimilarity >= 40) {
            badgeClass += "bg-brand text-white";
        } else if (roundedSimilarity >= 10) {
            badgeClass += "bg-cyan-600 text-white";
        } else {
            badgeClass += "bg-gray-200 text-secondary";
        }

        return (
            <span className={badgeClass}>
            {roundedSimilarity}%
        </span>
        );
    };

    const renderSortIcon = (column: string) => {
        if (sortBy !== column) {
            return <ArrowUpDown size={14} className="text-secondary opacity-50 group-hover:opacity-100 transition-opacity"/>;
        }
        return sortDesc ? (
            <ArrowDown size={14} className="text-primary"/>
        ) : (
            <ArrowUp size={14} className="text-primary"/>
        );
    };

    return (
        <div className="min-h-screen bg-base p-8">
            <div className="max-w-7xl mx-auto">
                <div className="flex flex-col md:flex-row justify-between items-start md:items-center mb-8 gap-4">
                    <div>
                        <h1 className="text-3xl font-bold text-primary mb-2">{t("reportList.title")}</h1>
                        <p className="text-secondary text-sm max-w-2xl leading-relaxed">
                            {t("reportList.sub")}
                        </p>
                    </div>

                    <div className="flex items-center gap-3">
                        <button
                            onClick={fetchReports}
                            disabled={isLoading}
                            className="flex items-center gap-2 px-4 py-2 bg-surface cursor-pointer border border-border hover:bg-active rounded-md text-sm font-bold text-primary transition-colors disabled:opacity-50"
                        >
                            <RefreshCw size={16} className={isLoading ? "animate-spin" : ""}/>
                            {t("reportList.refresh")}
                        </button>
                    </div>
                </div>

                <div className="bg-surface rounded-2xl shadow-sm border border-border overflow-visible">
                    <div className="overflow-x-visible">
                        <table className="w-full text-left border-collapse">
                            <thead>
                            <tr className="border-b border-border bg-surface select-none">
                                <th
                                    onClick={() => handleSort('tag')}
                                    className="py-6 px-8 text-xs font-bold text-secondary uppercase tracking-widest w-2/5 cursor-pointer group hover:bg-active transition-colors"
                                >
                                    <div className="flex items-center gap-2">
                                        {t("reportList.list.subject")}
                                        {renderSortIcon('tag')}
                                    </div>
                                </th>
                                <th
                                    onClick={() => handleSort('created_at')}
                                    className="py-6 px-8 text-xs font-bold text-secondary uppercase tracking-widest w-1/4 cursor-pointer group hover:bg-active transition-colors"
                                >
                                    <div className="flex items-center gap-2">
                                        {t("reportList.list.creation_date")}
                                        {renderSortIcon('created_at')}
                                    </div>
                                </th>
                                <th
                                    onClick={() => handleSort('average_similarity')}
                                    className="py-6 px-8 text-xs font-bold text-secondary uppercase tracking-widest w-1/4 cursor-pointer group hover:bg-active transition-colors"
                                >
                                    <div className="flex items-center gap-2">
                                        {t("reportList.list.avg")}
                                        {renderSortIcon('average_similarity')}
                                    </div>
                                </th>
                                <th className="py-6 px-8 text-xs font-bold text-secondary uppercase tracking-widest text-right">
                                    {t("reportList.list.action")}
                                </th>
                            </tr>
                            </thead>
                            <tbody
                                className={`divide-y divide-border ${isLoading ? 'opacity-60 pointer-events-none' : ''} transition-opacity duration-200`}>
                            {reports.length === 0 && !isLoading ? (
                                <tr>
                                    <td colSpan={4} className="py-8 text-center text-secondary">
                                        {t("reportList.list.notFound")}
                                    </td>
                                </tr>
                            ) : (
                                reports.map((report) => (
                                    <tr key={report.id} className="hover:bg-base transition-colors group cursor-pointer" onClick={() => navigate(`/reports/${report.id}`)}>
                                        <td className="py-5 px-8">
                                            <div className="flex items-center gap-3">
                                                <div
                                                    className="w-10 h-10 rounded-lg bg-base flex items-center justify-center text-secondary border border-border group-hover:bg-surface transition-colors">
                                                    <FileText size={20}/>
                                                </div>
                                                <div className="flex flex-col">
                                                        <span className="text-sm font-bold text-primary">
                                                            {report.tag}
                                                        </span>
                                                    <span className="text-xs text-secondary mt-0.5">
                                                            {report.subject_name}
                                                        </span>
                                                </div>
                                            </div>
                                        </td>
                                        <td className="py-5 px-8 text-sm text-secondary font-medium">
                                            {formatDate(report.created_at)}
                                        </td>
                                        <td className="py-5 px-8">
                                            {getSimilarityBadge(report.average_similarity * 100)}
                                        </td>
                                        <td className="py-5 px-8 relative">
                                            <div className="flex items-center justify-end gap-2">
                                                <button
                                                    onClick={(e) => toggleMenu(report.id, e)}
                                                    className="text-secondary hover:text-primary transition-colors p-2 rounded-full hover:bg-border outline-none">
                                                    <MoreVertical size={20}/>
                                                </button>
                                            </div>

                                            {openMenuId === report.id && (
                                                <div className="absolute right-8 top-12 w-40 bg-surface border border-border rounded-lg shadow-lg z-50 py-1 overflow-hidden">
                                                    <button
                                                        onClick={(e) => {
                                                            e.stopPropagation();
                                                            openDeleteConfirmation(report.id);
                                                        }}
                                                        className="w-full text-left px-4 py-2 text-sm text-danger hover:bg-danger-subtle flex items-center gap-2 transition-colors"
                                                    >
                                                        <Trash2 size={16} />
                                                        {t("reportList.list.delete", "Delete")}
                                                    </button>
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
                        className="flex flex-col sm:flex-row items-center justify-between px-8 py-4 border-t border-border bg-surface gap-4">
                        <div className="flex items-center gap-3">
                            <span className="text-sm text-secondary">{t("reportList.pagination.showing")}</span>
                            <select
                                value={size}
                                onChange={handleSizeChange}
                                className="bg-base border border-border text-primary text-sm rounded-md focus:ring-brand focus:border-brand block p-1.5 outline-none cursor-pointer"
                            >
                                <option value={5}>5</option>
                                <option value={10}>10</option>
                                <option value={25}>25</option>
                                <option value={50}>50</option>
                            </select>
                        </div>

                        <div className="flex items-center gap-6">
                            <span className="text-sm text-secondary">
                                {totalElements === 0 ? "0" : (page * size) + 1}-
                                {Math.min((page + 1) * size, totalElements)} {t("reportList.pagination.of")} {totalElements}
                            </span>

                            <div className="flex items-center gap-2">
                                <button
                                    onClick={() => handlePageChange(page - 1)}
                                    disabled={page === 0 || isLoading}
                                    className="p-1 rounded-md text-secondary hover:text-primary hover:bg-base disabled:opacity-50 disabled:hover:bg-transparent transition-colors"
                                >
                                    <ChevronLeft size={20}/>
                                </button>
                                <button
                                    onClick={() => handlePageChange(page + 1)}
                                    disabled={page >= totalPages - 1 || isLoading}
                                    className="p-1 rounded-md text-secondary hover:text-primary hover:bg-base disabled:opacity-50 disabled:hover:bg-transparent transition-colors"
                                >
                                    <ChevronRight size={20}/>
                                </button>
                            </div>
                        </div>
                    </div>
                </div>

            </div>

            <ConfirmationModal
                isOpen={isDeleteModalOpen}
                title={t("reportList.delete.title", "Delete Report")}
                description={t("reportList.delete.description", "Are you sure you want to delete this report? This action cannot be undone.")}
                confirmText={t("reportList.delete.confirm", "Delete")}
                onConfirm={handleConfirmDelete}
                onCancel={handleCancelDelete}
                isLoading={isDeleting}
            />
        </div>
    );
}