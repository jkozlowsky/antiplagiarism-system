import {
    RefreshCw,
    FileText,
    ChevronLeft,
    ChevronRight,
    ArrowUp,
    ArrowDown,
    ArrowUpDown
} from "lucide-react";
import { useEffect, useState, useCallback, useMemo } from "react";
import { useTranslation } from "react-i18next";
import { useNavigate } from "react-router-dom";
import { reportService } from "../../services/reportService";
import type { ReportDTO } from "../../types/report.types";

export default function StudentReportListPage() {
    const [reports, setReports] = useState<ReportDTO[]>([]);
    const [isLoading, setIsLoading] = useState(false);

    const [page, setPage] = useState(0);
    const [size, setSize] = useState(10);

    const [sortBy, setSortBy] = useState<keyof ReportDTO | undefined>(undefined);
    const [sortDesc, setSortDesc] = useState<boolean | undefined>(undefined);

    const { t, i18n } = useTranslation();
    const navigate = useNavigate();

    const fetchReports = useCallback(async () => {
        setIsLoading(true);
        try {
            const data = await reportService.getMyReports();
            setReports(data || []);
            setPage(0);
        } catch (error) {
            console.error("Failed to fetch student reports", error);
        } finally {
            setIsLoading(false);
        }
    }, []);

    useEffect(() => {
        fetchReports();
    }, [fetchReports]);

    const sortedReports = useMemo(() => {
        if (!sortBy) return [...reports];

        return [...reports].sort((a, b) => {
            const aVal = a[sortBy];
            const bVal = b[sortBy];

            if (aVal === undefined || aVal === null) return sortDesc ? 1 : -1;
            if (bVal === undefined || bVal === null) return sortDesc ? -1 : 1;

            if (aVal < bVal) return sortDesc ? 1 : -1;
            if (aVal > bVal) return sortDesc ? -1 : 1;
            return 0;
        });
    }, [reports, sortBy, sortDesc]);

    const totalElements = sortedReports.length;
    const totalPages = Math.ceil(totalElements / size);
    const paginatedReports = sortedReports.slice(page * size, (page + 1) * size);

    const handleSort = (column: keyof ReportDTO) => {
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

    const handleViewReport = (reportId: string) => {
        navigate(`/student-reports/${reportId}`);
    };

    const formatDate = (isoString: string) => {
        if (!isoString) return "";
        const date = new Date(isoString);
        const optionsDate: Intl.DateTimeFormatOptions = { month: 'short', day: 'numeric', year: 'numeric' };
        const optionsTime: Intl.DateTimeFormatOptions = { hour: '2-digit', minute: '2-digit', hour12: false };

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

    const renderSortIcon = (column: keyof ReportDTO) => {
        if (sortBy !== column) {
            return <ArrowUpDown size={14} className="text-secondary opacity-50 group-hover:opacity-100 transition-opacity" />;
        }
        return sortDesc ? (
            <ArrowDown size={14} className="text-primary" />
        ) : (
            <ArrowUp size={14} className="text-primary" />
        );
    };

    return (
        <div className="min-h-screen bg-base p-8">
            <div className="max-w-7xl mx-auto">
                <div className="flex flex-col md:flex-row justify-between items-start md:items-center mb-8 gap-4">
                    <div>
                        <h1 className="text-3xl font-bold text-primary mb-2">{t("studentReportList.title")}</h1>
                        <p className="text-secondary text-sm max-w-2xl leading-relaxed">
                            {t("studentReportList.sub")}
                        </p>
                    </div>

                    <div className="flex items-center gap-3">
                        <button
                            onClick={fetchReports}
                            disabled={isLoading}
                            className="flex items-center gap-2 px-4 py-2 bg-surface border border-border hover:bg-active rounded-md text-sm font-bold text-primary transition-colors disabled:opacity-50"
                        >
                            <RefreshCw size={16} className={isLoading ? "animate-spin" : ""} />
                            {t("studentReportList.refresh")}
                        </button>
                    </div>
                </div>

                <div className="bg-surface rounded-2xl shadow-sm border border-border overflow-visible">
                    <div className="overflow-x-auto">
                        <table className="w-full text-left border-collapse">
                            <thead>
                            <tr className="border-b border-border bg-surface select-none">
                                <th
                                    onClick={() => handleSort('tag')}
                                    className="py-6 px-8 text-xs font-bold text-secondary uppercase tracking-widest w-2/5 cursor-pointer group hover:bg-active transition-colors"
                                >
                                    <div className="flex items-center gap-2">
                                        {t("studentReportList.list.subject")}
                                        {renderSortIcon('tag')}
                                    </div>
                                </th>
                                <th
                                    onClick={() => handleSort('created_at')}
                                    className="py-6 px-8 text-xs font-bold text-secondary uppercase tracking-widest w-1/4 cursor-pointer group hover:bg-active transition-colors"
                                >
                                    <div className="flex items-center gap-2">
                                        {t("studentReportList.list.creation_date")}
                                        {renderSortIcon('created_at')}
                                    </div>
                                </th>
                                <th
                                    onClick={() => handleSort('average_similarity')}
                                    className="py-6 px-8 text-xs font-bold text-secondary uppercase tracking-widest w-1/4 cursor-pointer group hover:bg-active transition-colors"
                                >
                                    <div className="flex items-center gap-2">
                                        {t("studentReportList.list.avg")}
                                        {renderSortIcon('average_similarity')}
                                    </div>
                                </th>
                            </tr>
                            </thead>
                            <tbody
                                className={`divide-y divide-border ${isLoading ? 'opacity-60 pointer-events-none' : ''} transition-opacity duration-200`}
                            >
                            {paginatedReports.length === 0 && !isLoading ? (
                                <tr>
                                    <td colSpan={3} className="py-12 text-center text-secondary font-medium">
                                        <div className="flex flex-col items-center justify-center gap-3">
                                            <FileText size={32} className="opacity-40" />
                                            <p>{t("studentReportList.list.notFound")}</p>
                                        </div>
                                    </td>
                                </tr>
                            ) : (
                                paginatedReports.map((report) => (
                                    <tr
                                        key={report.id}
                                        onClick={() => handleViewReport(report.id)}
                                        className="hover:bg-base transition-colors group cursor-pointer"
                                    >
                                        <td className="py-5 px-8">
                                            <div className="flex items-center gap-3">
                                                <div className="w-10 h-10 rounded-lg bg-base flex items-center justify-center text-secondary border border-border group-hover:bg-surface transition-colors">
                                                    <FileText size={20} />
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
                                    </tr>
                                ))
                            )}
                            </tbody>
                        </table>
                    </div>

                    <div className="flex flex-col sm:flex-row items-center justify-between px-8 py-4 border-t border-border bg-surface gap-4">
                        <div className="flex items-center gap-3">
                            <span className="text-sm text-secondary">{t("studentReportList.pagination.showing")}</span>
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
                                {Math.min((page + 1) * size, totalElements)} {t("studentReportList.pagination.of")} {totalElements}
                            </span>

                            <div className="flex items-center gap-2">
                                <button
                                    onClick={() => handlePageChange(page - 1)}
                                    disabled={page === 0 || isLoading}
                                    className="p-1 rounded-md text-secondary hover:text-primary hover:bg-base disabled:opacity-50 disabled:hover:bg-transparent transition-colors"
                                >
                                    <ChevronLeft size={20} />
                                </button>
                                <button
                                    onClick={() => handlePageChange(page + 1)}
                                    disabled={page >= (totalPages === 0 ? 0 : totalPages - 1) || isLoading}
                                    className="p-1 rounded-md text-secondary hover:text-primary hover:bg-base disabled:opacity-50 disabled:hover:bg-transparent transition-colors"
                                >
                                    <ChevronRight size={20} />
                                </button>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    );
}