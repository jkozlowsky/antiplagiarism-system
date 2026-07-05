import {useCallback, useEffect, useState} from 'react';
import {useTranslation} from 'react-i18next';
import {useNavigate} from 'react-router-dom';
import {AlertTriangle, CheckCircle, FileText, RefreshCw, ScanSearch, ExternalLink} from 'lucide-react';
import {reportService} from '../../services/reportService';
import {repositoryService} from '../../services/repositoryService';
import type {StudentReportDetailsDTO, StudentRepositoryDTO} from '../../types/studentScan.types';
import type {ReportDTO} from '../../types/report.types';

export default function StudentScanPage() {
    const {t, i18n} = useTranslation();
    const navigate = useNavigate();

    const [selectedRepo, setSelectedRepo] = useState<StudentRepositoryDTO | null>(null);
    const [studentTag, setStudentTag] = useState('');
    const [taskTag, setTaskTag] = useState('');
    const [isScanning, setIsScanning] = useState(false);
    const [scanResult, setScanResult] = useState<StudentReportDetailsDTO | null>(null);
    const [error, setError] = useState<string | null>(null);

    const [repositories, setRepositories] = useState<StudentRepositoryDTO[]>([]);
    const [isLoadingRepos, setIsLoadingRepos] = useState(false);
    const [usedScans, setUsedScans] = useState<number>(0);

    const [history, setHistory] = useState<ReportDTO[]>([]);
    const [isLoadingHistory, setIsLoadingHistory] = useState(false);

    useEffect(() => {
        const loadRepos = async () => {
            setIsLoadingRepos(true);
            try {
                const data = await repositoryService.getMyRepositories();
                setRepositories(Array.isArray(data) ? data : []);
            } catch {
                setRepositories([]);
            } finally {
                setIsLoadingRepos(false);
            }
        };
        loadRepos();
    }, []);

    const loadHistory = useCallback(async () => {
        setIsLoadingHistory(true);
        try {
            const data = await reportService.getMyReports();
            setHistory(data || []);
        } catch {
            setHistory([]);
        } finally {
            setIsLoadingHistory(false);
        }
    }, []);

    useEffect(() => {
        loadHistory();
    }, [loadHistory]);

    const handleScan = async () => {
        if (!selectedRepo || !studentTag.trim() || !taskTag.trim()) return;
        setIsScanning(true);
        setError(null);
        setScanResult(null);
        try {
            const result = await reportService.generateStudentScan(selectedRepo.repositoryId, studentTag.trim(), taskTag.trim());
            setScanResult(result);
            await loadHistory();
            setRepositories(prev => prev.map(r =>
                r.repositoryId === selectedRepo.repositoryId
                    ? {...r, usedScans: r.usedScans + 1}
                    : r
            ));
            setUsedScans(prev => prev + 1);
        } catch (err: any) {
            const status = err?.response?.status;
            if (status === 403) setError(t('studentScan.errors.noTickets'));
            else if (status === 404) setError(t('studentScan.errors.notFound'));
            else if (status === 409) setError(t('studentScan.errors.noTickets'));
            else setError(t('studentScan.errors.base'));
            setUsedScans(prev => prev + 1);
        } finally {
            setIsScanning(false);
        }
    };

    const formatDate = (iso: string) => {
        if (!iso) return "";
        const d = new Date(iso);
        const lang = i18n.resolvedLanguage || i18n.language || 'en';
        return d.toLocaleDateString(lang, {month: 'short', day: 'numeric', year: 'numeric'})
            + ' • ' + d.toLocaleTimeString(lang, {hour: '2-digit', minute: '2-digit', hour12: false});
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

    const handleViewReport = (reportId: string) => {
        navigate(`/student-reports/${reportId}`);
    };

    const noScansLeft = selectedRepo != null && usedScans >= selectedRepo.maxScans;
    const canSubmit = !isScanning && selectedRepo != null && studentTag.trim().length > 0 && taskTag.trim().length > 0 && !noScansLeft;

    return (
        <div className="min-h-screen bg-base p-8">
            <div className="max-w-4xl mx-auto">

                <div className="mb-8">
                    <h1 className="text-3xl font-bold text-primary mb-2">{t('studentScan.title')}</h1>
                    <p className="text-secondary text-sm leading-relaxed">{t('studentScan.sub')}</p>
                </div>

                <div className="bg-surface rounded-2xl shadow-sm border border-border p-6 mb-6">
                    <div className="flex flex-col gap-4">

                        <div>
                            <label className="block text-xs font-bold text-secondary uppercase tracking-widest mb-1.5">
                                {t('studentScan.form.repository')}
                            </label>
                            {isLoadingRepos ? (
                                <div className="flex items-center gap-2 text-sm text-secondary py-2.5">
                                    <RefreshCw size={14} className="animate-spin"/>
                                    {t('common.loading')}
                                </div>
                            ) : repositories.length === 0 ? (
                                <p className="text-sm text-secondary py-2.5">
                                    {t('studentScan.form.noRepositories')}
                                </p>
                            ) : (
                                <select
                                    value={selectedRepo?.repositoryId ?? ''}
                                    onChange={e => {
                                        const repo = repositories.find(r => r.repositoryId === e.target.value) ?? null;
                                        setSelectedRepo(repo);
                                        setUsedScans(repo?.usedScans ?? 0);
                                        setError(null);
                                        setScanResult(null);
                                    }}
                                    className="w-full bg-base border border-border rounded-md px-4 py-2.5 text-sm text-primary outline-none focus:border-brand transition-colors cursor-pointer"
                                >
                                    <option value="">{t('studentScan.form.repositoryPlaceholder')}</option>
                                    {repositories.map(r => (
                                        <option key={r.repositoryId} value={r.repositoryId}>
                                            {r.subjectName} / {r.repositoryName}
                                        </option>
                                    ))}
                                </select>
                            )}
                        </div>

                        {selectedRepo && (
                            <div className={`flex items-center gap-2 text-sm px-4 py-2.5 rounded-md border ${
                                noScansLeft
                                    ? 'bg-danger-subtle border-danger-border text-danger'
                                    : 'bg-base border-border text-secondary'
                            }`}>
                                <ScanSearch size={14}/>
                                <span>
                                    {t('studentScan.form.scansUsed')}:{' '}
                                    <span className="font-bold text-primary">
                                        {usedScans} / {selectedRepo.maxScans}
                                    </span>
                                </span>
                                {noScansLeft && (
                                    <span className="font-bold ml-1">
                                        — {t('studentScan.errors.noTickets')}
                                    </span>
                                )}
                            </div>
                        )}

                        <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                            <div>
                                <label
                                    className="block text-xs font-bold text-secondary uppercase tracking-widest mb-1.5">
                                    {t('studentScan.form.studentTag')}
                                </label>
                                <input
                                    type="text"
                                    value={studentTag}
                                    onChange={e => setStudentTag(e.target.value)}
                                    placeholder={t('studentScan.form.studentTagPlaceholder')}
                                    className="w-full bg-base border border-border rounded-md px-4 py-2.5 text-sm text-primary outline-none focus:border-brand transition-colors"
                                />
                            </div>
                            <div>
                                <label
                                    className="block text-xs font-bold text-secondary uppercase tracking-widest mb-1.5">
                                    {t('studentScan.form.taskTag')}
                                </label>
                                <input
                                    type="text"
                                    value={taskTag}
                                    onChange={e => setTaskTag(e.target.value)}
                                    placeholder={t('studentScan.form.taskTagPlaceholder')}
                                    className="w-full bg-base border border-border rounded-md px-4 py-2.5 text-sm text-primary outline-none focus:border-brand transition-colors"
                                />
                            </div>
                        </div>

                        {error && (
                            <div
                                className="flex items-center gap-2 text-sm text-danger bg-danger-subtle border border-danger-border rounded-md px-4 py-2.5">
                                <AlertTriangle size={16}/>
                                {error}
                            </div>
                        )}

                        <button
                            onClick={handleScan}
                            disabled={!canSubmit}
                            className="flex items-center justify-center gap-2 px-6 py-2.5 bg-brand hover:bg-brand-hover text-white text-sm font-bold rounded-md transition-colors disabled:opacity-50 disabled:cursor-not-allowed self-start"
                        >
                            {isScanning
                                ? <><RefreshCw size={16} className="animate-spin"/>{t('studentScan.form.scanning')}</>
                                : <><ScanSearch size={16}/>{t('studentScan.form.submit')}</>
                            }
                        </button>
                    </div>
                </div>

                {scanResult && (
                    <div className="bg-surface rounded-2xl shadow-sm border border-border p-6 mb-6">
                        <div className="flex items-center justify-between mb-4">
                            <div className="flex items-center gap-2">
                                <CheckCircle size={20} className="text-brand"/>
                                <h2 className="text-lg font-bold text-primary">{t('studentScan.result.title', 'Result of a code scan')}</h2>
                            </div>

                            <button
                                onClick={() => handleViewReport(scanResult.reportId)}
                                className="flex items-center gap-2 px-4 py-2 bg-base border border-border hover:bg-active text-sm font-bold text-primary rounded-md transition-colors"
                            >
                                <FileText size={16} className="text-secondary"/>
                                {t('studentScan.result.openReport', 'Open Report')}
                                <ExternalLink size={16} className="text-secondary"/>
                            </button>
                        </div>

                        <div className="grid grid-cols-1 sm:grid-cols-3 gap-4 text-sm">
                            <div className="bg-base border border-border rounded-md p-3">
                                <span
                                    className="block text-xs text-secondary mb-1">{t('studentScan.result.tag', 'Tag')}:</span>
                                <span className="font-bold text-primary">{scanResult.tag}</span>
                            </div>
                            <div className="bg-base border border-border rounded-md p-3">
                                <span
                                    className="block text-xs text-secondary mb-1">{t('studentScan.result.subject', 'Subject')}:</span>
                                <span className="font-bold text-primary">{scanResult.subjectName}</span>
                            </div>
                            <div className="bg-base border border-border rounded-md p-3">
                                <span
                                    className="block text-xs text-secondary mb-1">{t('studentScan.result.scannedAt', 'Scanned')}:</span>
                                <span className="font-bold text-primary">{formatDate(scanResult.createdAt)}</span>
                            </div>
                        </div>
                    </div>
                )}

                <div className="bg-surface rounded-2xl shadow-sm border border-border overflow-hidden">
                    <div className="flex items-center justify-between px-6 py-4 border-b border-border">
                        <h2 className="text-base font-bold text-primary">{t('studentScan.history.title')}</h2>
                        <button
                            onClick={loadHistory}
                            disabled={isLoadingHistory}
                            className="flex items-center gap-2 px-3 py-1.5 text-sm font-bold text-primary bg-base border border-border rounded-md hover:bg-active transition-colors disabled:opacity-50"
                        >
                            <RefreshCw size={14} className={isLoadingHistory ? 'animate-spin' : ''}/>
                            {t('common.refresh')}
                        </button>
                    </div>
                    <div
                        className={`divide-y divide-border ${isLoadingHistory ? 'opacity-60' : ''} transition-opacity`}>
                        {(!Array.isArray(history) || history.length === 0) ? (
                            <p className="text-center text-secondary text-sm py-8">
                                {t('studentScan.history.empty')}
                            </p>
                        ) : history.map(r => (
                            <div
                                key={r.id}
                                onClick={() => handleViewReport(r.id)}
                                className="flex items-center justify-between px-6 py-4 hover:bg-base transition-colors group cursor-pointer"
                            >
                                <div className="flex items-center gap-3">
                                    <div
                                        className="w-9 h-9 rounded-lg bg-base border border-border flex items-center justify-center text-secondary group-hover:bg-surface transition-colors">
                                        <FileText size={18}/>
                                    </div>
                                    <div>
                                        <p className="text-sm font-bold text-primary">{r.tag}</p>
                                        <p className="text-xs text-secondary mt-0.5">{r.subject_name}</p>
                                    </div>
                                </div>
                                <div className="flex items-center gap-4">
                                    <span
                                        className="text-sm text-secondary font-medium">{formatDate(r.created_at)}</span>
                                    {getSimilarityBadge(r.average_similarity * 100)}
                                </div>
                            </div>
                        ))}
                    </div>
                </div>

            </div>
        </div>
    );
}