import { useEffect, useState } from "react";
import { useParams, useNavigate } from "react-router-dom";
import { useTranslation } from "react-i18next";
import {
    ArrowLeft, ShieldCheck, FileText, Activity, Users, Hash, Lock
} from "lucide-react";
import { reportService } from "../../services/reportService";
import type { StudentOwnReportDetailsDTO } from "../../types/report.types";
import {PATHS} from "../../routes/paths.ts";

export default function StudentReportDetailsPage() {
    const { id } = useParams<{ id: string }>();
    const [reportDetails, setReportDetails] = useState<StudentOwnReportDetailsDTO | null>(null);
    const [isLoading, setIsLoading] = useState(true);

    const { t, i18n } = useTranslation();
    const navigate = useNavigate();

    useEffect(() => {
        if (!id) return;
        const fetchDetails = async () => {
            setIsLoading(true);
            try {
                const data = await reportService.getMyReportDetails(id);
                setReportDetails(data);
            } catch (err: any) {
                navigate('/student-reports');
            } finally {
                setIsLoading(false);
            }
        };
        fetchDetails();
    }, [id, navigate]);

    const formatDate = (isoString: string) => {
        const date = new Date(isoString);
        const optionsDate: Intl.DateTimeFormatOptions = { month: 'short', day: 'numeric', year: 'numeric' };
        const optionsTime: Intl.DateTimeFormatOptions = { hour: '2-digit', minute: '2-digit', hour12: false };

        return `${date.toLocaleDateString(i18n.language, optionsDate)} • ${date.toLocaleTimeString(i18n.language, optionsTime)}`;
    };

    const getPercentageColor = (value: number) => {
        if (value >= 0.6) {
            return "text-red-700 bg-red-100 border-red-200 dark:bg-red-500/10 dark:border-red-500/20 dark:text-red-400";
        }
        if (value >= 0.3) {
            return "text-orange-700 bg-orange-100 border-orange-200 dark:bg-orange-500/10 dark:border-orange-500/20 dark:text-orange-400";
        }
        return "text-green-700 bg-green-100 border-green-200 dark:bg-green-500/10 dark:border-green-500/20 dark:text-green-400";
    };

    if (isLoading || !reportDetails) {
        return (
            <div className="min-h-screen bg-base p-8 flex justify-center items-center">
                <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-brand"></div>
            </div>
        );
    }

    return (
        <div className="min-h-screen bg-base p-4 md:p-8">
            <div className="max-w-4xl mx-auto">
                <button
                    onClick={() => navigate(PATHS.STUDENT_REPORTS)}
                    className="flex items-center gap-2 text-secondary hover:text-primary transition-colors font-semibold mb-6"
                >
                    <ArrowLeft size={18} />
                    {t("studentReportDetails.back")}
                </button>

                <div className="bg-surface rounded-2xl shadow-sm border border-border p-6 md:p-8 mb-8">
                    <div className="flex flex-col md:flex-row gap-6 justify-between items-start">
                        <div>
                            <div className="flex items-center gap-3 mb-2">
                                <div className="p-2 bg-brand-subtle text-brand rounded-lg">
                                    <FileText size={24} />
                                </div>
                                <h1 className="text-2xl font-bold text-primary">
                                    {reportDetails.subjectName}
                                </h1>
                            </div>
                            <p className="text-secondary text-sm flex items-center gap-2">
                                <span className="font-semibold text-primary px-2 py-0.5 bg-base border border-border rounded-md">
                                    Tag: {reportDetails.tag}
                                </span>
                                • {formatDate(reportDetails.createdAt)}
                            </p>
                        </div>

                        <div className="bg-brand/5 text-secondary px-4 py-3 rounded-lg border border-brand/20 dark:border-brand/10 flex items-center gap-3 max-w-md">
                            <ShieldCheck size={28} className="shrink-0 text-brand" />
                            <p className="text-xs font-medium leading-relaxed">
                                {t("studentReportDetails.privacyNotice")}
                            </p>
                        </div>
                    </div>
                </div>

                {reportDetails.raportLevel === "NOTHING" ? (
                    <div className="bg-surface rounded-2xl border border-border p-12 text-center shadow-sm">
                        <Lock size={48} className="mx-auto text-secondary mb-4 opacity-50" />
                        <h2 className="text-xl font-bold text-primary mb-2">
                            {t("studentReportDetails.hiddenTitle")}
                        </h2>
                        <p className="text-secondary max-w-md mx-auto">
                            {t("studentReportDetails.hiddenDesc")}
                        </p>
                    </div>
                ) : (
                    <>
                        <h2 className="text-xl font-bold text-primary mb-4">
                            {t("studentReportDetails.matchesTitle")}
                        </h2>

                        {reportDetails.matches.length === 0 ? (
                            <div className="bg-surface rounded-2xl border border-border p-12 text-center shadow-sm">
                                <Activity size={40} className="mx-auto text-success mb-3 opacity-80" />
                                <h3 className="text-lg font-bold text-primary mb-1">
                                    {t("studentReportDetails.noMatches")}
                                </h3>
                                <p className="text-secondary text-sm">
                                    {t("studentReportDetails.noMatchesSub")}
                                </p>
                            </div>
                        ) : (
                            <div className="grid grid-cols-1 gap-6">
                                {reportDetails.matches.map((match, index) => {
                                    const max = match.studentSimilarity || 0;

                                    const hasAverage = match.averageSimilarity !== undefined && match.averageSimilarity !== null;
                                    const avg = match.averageSimilarity || 0;

                                    const otherSim = hasAverage && max > 0 && (2 * max - avg) > 0
                                        ? (avg * max) / (2 * max - avg)
                                        : null;

                                    const isHighestPercentage = reportDetails.raportLevel === "HIGHEST_PERCENTAGE";
                                    const highestPercentValue = Math.max(max, avg);

                                    return (
                                        <div key={index} className="bg-surface rounded-2xl border border-border p-6 shadow-sm hover:shadow-md transition-shadow">

                                            <div className="flex items-center justify-between mb-6 border-b border-border pb-4">
                                                <h3 className="font-bold text-lg text-primary flex items-center gap-2">
                                                    {t("studentReportDetails.comparedWith")}
                                                    <span className="bg-base px-3 py-1 rounded-md text-secondary border border-border italic text-sm">
                                                        {t("studentReportDetails.anonymized")}
                                                    </span>
                                                </h3>
                                            </div>

                                            <div className="flex flex-wrap gap-4 mt-2">

                                                {isHighestPercentage ? (
                                                    <div className={`w-full sm:w-64 p-4 rounded-xl border flex flex-col gap-1 ${getPercentageColor(highestPercentValue)}`}>
                                                        <div className="flex items-center gap-2 mb-1 opacity-80">
                                                            <Hash size={18} />
                                                            <span className="text-xs font-bold uppercase tracking-wider">
                                                                {t("studentReportDetails.metrics.average")}
                                                            </span>
                                                        </div>
                                                        <p className="text-3xl font-black">
                                                            {(highestPercentValue * 100).toFixed(1)}%
                                                        </p>
                                                    </div>
                                                ) : (
                                                    <>
                                                        {match.studentSimilarity !== undefined && match.studentSimilarity !== null && (
                                                            <div className={`w-full sm:w-64 p-4 rounded-xl border flex flex-col gap-1 ${getPercentageColor(match.studentSimilarity)}`}>
                                                                <div className="flex items-center gap-2 mb-1 opacity-80">
                                                                    <Activity size={18} />
                                                                    <span className="text-xs font-bold uppercase tracking-wider">
                                                                        {t("studentReportDetails.metrics.studentSimilarity")}
                                                                    </span>
                                                                </div>
                                                                <p className="text-3xl font-black">
                                                                    {(match.studentSimilarity * 100).toFixed(1)}%
                                                                </p>
                                                            </div>
                                                        )}

                                                        {otherSim !== null && (
                                                            <div className={`w-full sm:w-64 p-4 rounded-xl border flex flex-col gap-1 ${getPercentageColor(otherSim)}`}>
                                                                <div className="flex items-center gap-2 mb-1 opacity-80">
                                                                    <Users size={18} />
                                                                    <span className="text-xs font-bold uppercase tracking-wider">
                                                                        {t("studentReportDetails.metrics.otherSimilarity")}
                                                                    </span>
                                                                </div>
                                                                <p className="text-3xl font-black">
                                                                    {(otherSim * 100).toFixed(1)}%
                                                                </p>
                                                            </div>
                                                        )}

                                                        {hasAverage && (
                                                            <div className={`w-full sm:w-64 p-4 rounded-xl border flex flex-col gap-1 ${getPercentageColor(match.averageSimilarity!)}`}>
                                                                <div className="flex items-center gap-2 mb-1 opacity-80">
                                                                    <Hash size={18} />
                                                                    <span className="text-xs font-bold uppercase tracking-wider">
                                                                        {t("studentReportDetails.metrics.average")}
                                                                    </span>
                                                                </div>
                                                                <p className="text-3xl font-black">
                                                                    {(match.averageSimilarity! * 100).toFixed(1)}%
                                                                </p>
                                                            </div>
                                                        )}
                                                    </>
                                                )}

                                            </div>
                                        </div>
                                    );
                                })}
                            </div>
                        )}
                    </>
                )}
            </div>
        </div>
    );
}