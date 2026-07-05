import React, { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import {
    getSubjectDetails,
    deleteSubject,
    syncSubjectWithGitea,
    getTranslatedDescription
} from '../../services/subjectService';
import type { SubjectDTO } from '../../types/SubjectDTO';
import { formatDate, reportService } from "../../services/reportService";
import type { Page } from "../../types/user.types";
import type { ReportDTO } from "../../types/report.types";
import { SimilarityBadge } from "../../shared/components/similarity_badge/SimilarityBadge";
import { Calendar, SquarePen, CirclePlay, BarChartBigIcon, Trash2, Loader2, RefreshCw } from "lucide-react";
import { PATHS } from "../../routes/paths";
import { EditSubjectModal } from './EditSubjectModal';
import { repositoryService } from "../../services/repositoryService.ts";
import type { RepositoryWithStudentDTO } from "../../types/subject.types.ts";

export const SubjectDetailsView: React.FC = () => {
    const { id } = useParams<{ id: string }>();
    const navigate = useNavigate();
    const { t } = useTranslation();

    const [subject, setSubject] = useState<SubjectDTO | null>(null);
    const [loading, setLoading] = useState<boolean>(true);
    const [error, setError] = useState<string | null>(null);
    const [tagError, setTagError] = useState<boolean>(false);

    const [reports, setReports] = useState<Page<ReportDTO> | null>(null);
    const [isReportsModalOpen, setIsReportsModalOpen] = useState<boolean>(false);
    const [isStartAnalysisModalOpen, setIsStartAnalysisModalOpen] = useState<boolean>(false);
    const [analysisTag, setAnalysisTag] = useState('');

    const [isEditModalOpen, setIsEditModalOpen] = useState<boolean>(false);
    const [isDeleteModalOpen, setIsDeleteModalOpen] = useState<boolean>(false);
    const [deleteGiteaOrg, setDeleteGiteaOrg] = useState<boolean>(false);
    const [isDeleting, setIsDeleting] = useState<boolean>(false);

    const [repositories, setRepositories] = useState<RepositoryWithStudentDTO[] | null>(null);

    const [isSyncing, setIsSyncing] = useState<boolean>(false);

    const fetchSubjectData = () => {
        if (!id) return;
        setLoading(true);
        getSubjectDetails(id)
            .then(data => {
                setSubject(data);
                setLoading(false);
            })
            .catch(() => {
                setError(t('subject.details.fetchError'));
                setLoading(false);
            });
    };

    const fetchReports = (id: string) => {
        reportService.getAllReportsForSubject(id, 0, 1000, "created_at", true)
            .then(data => setReports(data))
            .catch(console.error)
    }

    const fetchRepositories = (id: string) => {
        repositoryService.getRepositoriesForSubject(id)
            .then(data => setRepositories(data))
            .catch();
    }

    useEffect(() => {
        fetchSubjectData();
        if (id) {
            fetchReports(id);
            fetchRepositories(id);
        }
    }, [id, t]);


    const handleStartAnalysis = () => {
        if (!id) return;
        reportService.postStartSubjectAnalysis(id, analysisTag)
            .then(() => {
                setIsStartAnalysisModalOpen(false);
                setAnalysisTag('');
                fetchSubjectData();
                fetchReports(id);
            });
    };

    const handleDeleteSubject = async () => {
        if (!id || !subject) return;
        const currentHash = subject.versionHash;

        if (!currentHash) {
            setError(t('subject.deleteVersionError'));
            return;
        }

        setIsDeleting(true);
        setError(null);

        try {
            await deleteSubject(id, currentHash, deleteGiteaOrg);
            setIsDeleteModalOpen(false);
            navigate('/subjects');
        } catch (err: any) {
            if (err.response?.status === 409) {
                setError(t('subject.deleteConflict'));
            } else if (err.response?.status === 403) {
                setError(t('subject.deleteForbidden'));
            } else {
                setError(t('subject.deleteError'));
            }
            setIsDeleteModalOpen(false);
        } finally {
            setIsDeleting(false);
        }
    };

    const handleSyncGitea = async () => {
        if (!id) return;
        setIsSyncing(true);
        setError(null);
        try {
            await syncSubjectWithGitea(id);
            fetchSubjectData();
        } catch (err: any) {
            setError(t('subject.details.syncError'));
        } finally {
            setIsSyncing(false);
        }
        repositoryService.getRepositoriesForSubject(id)
            .then(data => setRepositories(data))
            .catch();
    };

    const uniqueStudentsCount = React.useMemo(() => {
        if (!repositories) return '0';
        const uniqueStudentIdentifiers = new Set<string>();
        repositories.forEach(repo => {
            if (repo.students) {
                repo.students.forEach(student => {
                    uniqueStudentIdentifiers.add(`${student.name} ${student.surname}`);
                });
            }
        });
        return uniqueStudentIdentifiers.size.toString();
    }, [repositories]);

    const displaySimilarity = React.useMemo(() => {
        if (subject?.aggregatedAverageSimilarity != null) {
            return `${(subject.aggregatedAverageSimilarity * 100).toFixed(1)}%`;
        }
        return '0.0%';
    }, [subject?.aggregatedAverageSimilarity]);

    if (loading) return <div className="p-8 flex justify-center items-center h-64"><Loader2 className="w-8 h-8 animate-spin text-brand" /></div>;
    if (error || !subject) return <div className="p-8 text-center text-danger font-medium">{error || t('subject.details.notFound')}</div>;


    function handleShowTranslation() {
        if (!id) return;
        getTranslatedDescription(id).then((response) => {
            setSubject((prevSubject) => {
                if (!prevSubject) return null;
                return {
                    ...prevSubject,
                    subjectDescription: response.translatedSubjectDescription
                };
            });
        });
    }

    return (
        <div className="p-6 md:p-10 max-w-[1400px] mx-auto min-h-screen bg-base relative">

            <div className="mb-8 border-b border-border pb-6">
                {subject.archived && (
                    <div className="mb-6 bg-amber-500/10 border border-amber-500/20 text-amber-700 dark:text-amber-500 p-4 rounded-xl flex items-center gap-3 shadow-sm animate-fade-in">
                        <svg className="w-6 h-6 flex-shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" /></svg>
                        <span className="text-sm font-bold">
                            {t('subject.details.archivedWarning')}
                        </span>
                    </div>
                )}

                <div className="flex flex-col lg:flex-row lg:justify-between lg:items-start gap-6">
                    <div className="flex-1 min-w-0">
                        <h1 className="text-3xl md:text-4xl font-bold text-primary mb-3 truncate">{subject.name}</h1>
                        <p className="text-secondary text-sm mb-6 leading-relaxed flex flex-wrap items-center gap-2">
                            <span>{t('subject.details.edition')}: <span className="font-medium text-primary">{subject.edition}</span></span>
                            <span className="text-border">|</span>
                            <span>{t('subject.details.organization')}: <a href={subject.giteaURL || `https://gitea.com/${subject.organizationName}`} target="_blank" rel="noreferrer" className="font-medium text-brand hover:underline">{subject.organizationName}</a></span>
                        </p>
                        <p className="text-primary max-w-4xl text-sm mb-4 leading-relaxed">
                            {subject.subjectDescription ? subject.subjectDescription : <span className="italic text-secondary">{t('subject.details.noDescription')}</span>}
                        </p>
                        {subject.subjectDescription && (
                            <button
                                onClick={() => handleShowTranslation()}
                                className="cursor-pointer text-sm font-medium text-brand hover:text-primary underline underline-offset-4 decoration-brand/30 hover:decoration-primary transition-all duration-200 focus:outline-none"
                            >
                                {t("subject.translation")}
                            </button>
                        )}
                    </div>

                    <div className="flex flex-wrap items-center justify-start lg:justify-end gap-3 lg:gap-4 shrink-0 mt-4 lg:mt-0">
                        {!subject.archived && (
                            <>
                                <button onClick={() => navigate(PATHS.SUBJECT_SCHEDULE_LIST.replace(':id', subject.id!.toString()))} className="flex items-center gap-2 text-sm font-semibold text-secondary hover:text-brand transition-colors px-2 py-1">
                                    <Calendar className="w-4 h-4"/>
                                    {t('subject.details.actions.schedule')}
                                </button>
                                <button className="flex items-center gap-2 text-sm font-semibold text-secondary hover:text-brand-hover transition-colors px-2 py-1" onClick={() => setIsStartAnalysisModalOpen(true)}>
                                    <CirclePlay className="w-4 h-4"/>
                                    {t('subject.details.actions.analyze')}
                                </button>
                            </>
                        )}
                        {subject.canEdit && (
                            <button onClick={() => setIsEditModalOpen(true)} className="flex items-center gap-2 text-sm font-semibold text-secondary hover:text-brand transition-colors px-2 py-1">
                                <SquarePen className="w-4 h-4"/>
                                {subject.archived ? t('subject.details.actions.settings') : t('subject.details.actions.edit')}
                            </button>
                        )}
                        {!subject.archived && subject.canManageTeachers && (
                            <button onClick={() => setIsDeleteModalOpen(true)} className="flex items-center gap-2 text-sm font-semibold text-danger hover:text-red-700 dark:hover:text-red-400 transition-colors px-2 py-1">
                                <Trash2 className="w-4 h-4"/>
                                {t('subject.delete')}
                            </button>
                        )}
                    </div>
                </div>
            </div>

            {subject.canViewStats ? (
                <div className="flex flex-col gap-10">
                    <div className="grid grid-cols-2 md:grid-cols-4 gap-100 md:gap-20 mb-4">
                        {[
                            { label: t('subject.details.stats.students'), value: uniqueStudentsCount},
                            { label: t('subject.details.stats.repos'), value: repositories?.length?.toString() || '0' },
                            { label: t('subject.details.stats.pending'), value: reports?.totalElements?.toString() || '0' },
                            { label: t('subject.details.stats.similarity'), value: displaySimilarity }
                        ].map((stat, idx) => (
                            <div key={idx} className={`flex flex-col rounded-md px-8 py-5 text-green-700 bg-green-100 border-green-200 dark:bg-green-500/10 dark:border-green-500/20 dark:text-green-400 drop-shadow-lg inset-shadow-sm`}>
                                <span className="text-[11px] sm:text-xs font-bold text-center uppercase tracking-widest mb-1">{stat.label}</span>
                                <span className="text-4xl sm:text-5xl font-extrabold text-center">{stat.value}</span>
                            </div>
                        ))}
                    </div>

                    <div>
                        <div className="flex justify-between items-center mb-4 mt-4">
                            <h3 className="text-xl font-bold text-primary flex items-center gap-2">
                                <BarChartBigIcon className="w-5 h-5 text-brand" />
                                {t('subject.details.reports.title')}
                            </h3>
                            <button type="button" onClick={() => setIsReportsModalOpen(true)} className="text-sm font-bold text-brand hover:text-brand-hover transition-colors">
                                {t('subject.details.reports.viewAll')}
                            </button>
                        </div>

                        <div className="flex overflow-x-auto gap-5 pb-4 snap-x [&::-webkit-scrollbar]:h-2 [&::-webkit-scrollbar-track]:bg-transparent [&::-webkit-scrollbar-thumb]:bg-[#cbd5e1] hover:[&::-webkit-scrollbar-thumb]:bg-[#94a3b8] dark:[&::-webkit-scrollbar-thumb]:bg-[#475569] dark:hover:[&::-webkit-scrollbar-thumb]:bg-[#334155] [&::-webkit-scrollbar-thumb]:rounded-full transition-colors">
                            {reports?.content.length === 0 ? (
                                <p className="text-sm text-secondary italic">{t('subject.details.reports.empty')}</p>
                            ) : (
                                reports?.content.map((report) => (
                                    <div key={report.id}
                                         className="min-w-[280px] p-5 rounded-xl border border-border bg-surface shadow-sm snap-start shrink-0 cursor-pointer hover:border-brand transition-colors"
                                         onClick={() => {
                                             navigate(`/reports/${report.id}`)
                                         }}
                                    >
                                        <div className="flex justify-between items-start mb-3">
                                            <h4 className="font-bold text-primary">{t('subject.details.reports.report')} • {formatDate(report.created_at)}</h4>
                                            <span className={"text-[13px] font-bold px-2 py-0.5 rounded-sm"}>
                                                <SimilarityBadge similarity={report.average_similarity * 100} />
                                            </span>
                                        </div>
                                        <p className="text-sm text-primary font-mono py-1 rounded inline-block">{t("subject.details.reports.repositories")}</p>
                                        <b> {report.scanned_repositories}</b>
                                        <p className="text-sm text-primary font-mono bg-base ml-8 px-2 py-1 rounded inline-block"> {report.tag} </p>
                                    </div>
                                ))
                            )}
                        </div>
                    </div>

                    <div>
                        <div className="flex justify-between items-center mb-6">
                            <h3 className="text-xl font-bold text-primary">{t('subject.details.repos.title')}</h3>
                            <div className="flex gap-4 text-sm font-semibold text-secondary">
                                {subject.canEdit && !subject.archived && (
                                    <button
                                        type="button"
                                        onClick={handleSyncGitea}
                                        disabled={isSyncing}
                                        className="hover:text-brand transition-colors flex items-center gap-1 disabled:opacity-50"
                                    >
                                        {isSyncing ? (
                                            <Loader2 className="w-4 h-4 animate-spin" />
                                        ) : (
                                            <RefreshCw className="w-4 h-4" />
                                        )}
                                        <span className="text-sm font-semibold">{t('subject.details.syncGitea')}</span>
                                    </button>
                                )}
                            </div>
                        </div>

                        <div className="flex flex-col gap-4">
                            {repositories?.length === 0 ? (
                                <div className="p-8 text-center text-secondary border border-dashed border-border rounded-xl">
                                    {t('subject.details.repos.empty')}
                                </div>
                            ) : (
                                repositories?.map((repo: RepositoryWithStudentDTO) => {
                                    const repoName = repo.repositoryName;
                                    const users = repo.students && repo.students.length > 0
                                        ? repo.students.map((s) => `${s.name} ${s.surname}`).join(', ')
                                        : t('subject.details.repos.noUsers');

                                    return (
                                        <div key={repoName} className="border border-border bg-surface rounded-xl p-5 shadow-sm flex flex-col sm:flex-row sm:justify-between sm:items-center gap-4 hover:border-brand/50 transition-colors">
                                            <div>
                                                <h4 className="font-bold text-primary text-base mb-1">{repoName}</h4>
                                                <p className="text-sm text-secondary mb-1">
                                                    {t('subject.details.repos.tickets')}: <span className="font-bold text-primary">{repo.ticketCount || 0}</span>
                                                </p>
                                                <p className="text-xs text-secondary font-medium">
                                                    {t('subject.details.repos.contributors')}: <span className="text-primary">{users}</span>
                                                </p>
                                            </div>
                                        </div>
                                    );
                                })
                            )}
                        </div>
                    </div>

                </div>
            ) : (
                <div className="bg-surface border border-border rounded-xl p-12 text-center shadow-sm mt-8">
                    <p className="text-secondary text-lg">{t('subject.details.noAccess')}</p>
                </div>
            )}


            {isReportsModalOpen && (
                <div className="fixed inset-0 z-50 bg-base flex flex-col p-6 md:p-10 overflow-y-auto animate-fade-in">
                    <div className="max-w-[1400px] w-full mx-auto">
                        <div className="flex justify-between items-center mb-8 border-b border-border pb-4 mt-4">
                            <h2 className="text-3xl font-bold text-primary">
                                {t('subject.details.reports.modalTitle')}
                            </h2>
                            <button
                                type="button"
                                onClick={() => setIsReportsModalOpen(false)}
                                className="text-secondary hover:text-brand font-bold text-3xl p-2 transition-colors leading-none"
                            >
                                &times;
                            </button>
                        </div>

                        <div
                            className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4 xl:grid-cols-5 gap-6">
                            {reports?.content.map((report) => (
                                <div
                                    key={report.id}
                                    className="p-5 rounded-xl border border-border bg-surface shadow-sm cursor-pointer hover:border-brand transition-colors flex flex-col min-h-[160px]"
                                    onClick={() => {
                                        navigate(`/reports/${report.id}`)
                                    }}
                                >
                                    <div className="flex justify-between items-start gap-3 mb-4">
                                        <h4 className="font-bold text-primary leading-relaxed">
                                            {t('subject.details.reports.report')} • {formatDate(report.created_at)}
                                        </h4>
                                        <div className="shrink-0 mt-0.5">
                                            <SimilarityBadge similarity={report.average_similarity * 100} />
                                        </div>
                                    </div>
                                    <div className="mt-auto flex flex-col items-start gap-2">
                                        <p className="text-sm text-primary font-mono m-0">
                                            {t("subject.details.reports.repositories")}
                                            <span className="font-bold text-lg">{report.scanned_repositories} </span>
                                        </p>
                                        <span
                                            className="inline-flex text-xs text-secondary font-mono bg-base px-2 py-1 rounded">
                                            {report.tag}
                                        </span>
                                    </div>
                                </div>
                            ))}
                        </div>
                    </div>
                </div>
            )}

            {isStartAnalysisModalOpen && (
                <div className="fixed inset-0 z-[60] flex items-center justify-center p-4 bg-black/60 backdrop-blur-sm animate-fade-in">
                    <div className="bg-surface w-full max-w-[480px] rounded-2xl shadow-xl overflow-hidden border border-border">
                        <div className="flex justify-between items-center p-6 pb-4">
                            <h2 className="text-[20px] font-bold text-primary">
                                {t('subject.analysis.modal.title')}
                            </h2>
                            <button
                                onClick={() => {
                                    setIsStartAnalysisModalOpen(false);
                                    setTagError(false);
                                }}
                                className="text-secondary hover:text-primary transition-colors"
                            >
                                <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" /></svg>
                            </button>
                        </div>
                        <div className="px-6 pb-6">
                            <p className="text-[13px] text-secondary leading-relaxed mb-6">
                                {t('subject.analysis.modal.description')}
                            </p>
                            <div>
                                <label className="block text-[11px] font-bold text-primary uppercase tracking-wider mb-2">
                                    {t('subject.analysis.modal.tagLabel')}
                                </label>
                                <input
                                    type="text"
                                    value={analysisTag}
                                    onChange={(e) => {
                                        setAnalysisTag(e.target.value);
                                        if (tagError) setTagError(false);
                                    }}
                                    placeholder={t('subject.analysis.modal.tagPlaceholder')}
                                    className={`w-full px-4 py-2.5 bg-base border rounded-lg text-primary placeholder:text-gray-400 focus:outline-none focus:ring-1 transition-all ${
                                        tagError
                                            ? 'border-danger focus:border-danger focus:ring-danger'
                                            : 'border-border focus:border-brand focus:ring-brand'
                                    }`}
                                />
                                {tagError && (
                                    <p className="text-[11px] text-danger font-semibold mt-1.5">
                                        {t('validation.required', 'This field is required')}
                                    </p>
                                )}
                            </div>
                        </div>
                        <div className="px-6 pb-6 flex justify-end items-center gap-4">
                            <button
                                onClick={() => {
                                    setIsStartAnalysisModalOpen(false);
                                    setTagError(false);
                                }}
                                className="text-[14px] font-bold text-secondary hover:text-primary transition-colors"
                            >
                                {t('common.cancel')}
                            </button>
                            <button
                                onClick={() => {
                                    if (!analysisTag || !analysisTag.trim()) {
                                        setTagError(true);
                                        return;
                                    }
                                    handleStartAnalysis();
                                }}
                                disabled={!analysisTag || !analysisTag.trim()} // Optional: visually disable button
                                className="px-6 py-2.5 bg-brand hover:bg-brand-hover text-white text-[14px] font-bold rounded-lg shadow-sm transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
                            >
                                {t('subject.analysis.modal.submit')}
                            </button>
                        </div>
                    </div>
                </div>
            )}

            {isDeleteModalOpen && (
                <div className="fixed inset-0 z-[60] flex items-center justify-center p-4 bg-black/60 backdrop-blur-sm animate-fade-in">
                    <div className="bg-surface w-full max-w-[480px] rounded-2xl shadow-xl overflow-hidden border border-border">
                        <div className="flex justify-between items-center p-6 pb-4 border-b border-border">
                            <div className="flex items-center gap-3">
                                <div className="p-2 bg-danger-subtle rounded-full text-danger">
                                    <Trash2 className="w-5 h-5" />
                                </div>
                                <h2 className="text-xl font-bold text-primary">{t('subject.deleteConfirmTitle')}</h2>
                            </div>
                        </div>
                        <div className="p-6">
                            <p className="text-sm text-secondary leading-relaxed mb-6">
                                {t('subject.deleteConfirmMessage')}
                            </p>
                            <div className="flex items-center gap-3 mt-5">
                                <input
                                    id="gitea-delete-checkbox"
                                    type="checkbox"
                                    checked={deleteGiteaOrg}
                                    onChange={(e) => setDeleteGiteaOrg(e.target.checked)}
                                    className="w-4 h-4 rounded border-border accent-danger text-danger focus:ring-danger cursor-pointer"
                                />
                                <label htmlFor="gitea-delete-checkbox" className="text-sm font-bold text-primary cursor-pointer select-none">
                                    {t('subject.deleteGiteaOption')}
                                </label>
                            </div>
                        </div>
                        <div className="px-6 pb-6 pt-2 flex justify-end items-center gap-4">
                            <button onClick={() => setIsDeleteModalOpen(false)} disabled={isDeleting} className="text-sm font-bold text-secondary hover:text-primary transition-colors disabled:opacity-50">
                                {t('common.cancel')}
                            </button>
                            <button onClick={handleDeleteSubject} disabled={isDeleting} className="px-6 py-2.5 bg-danger hover:bg-red-700 text-white text-sm font-bold rounded-lg shadow-sm transition-colors flex items-center justify-center disabled:opacity-50">
                                {isDeleting ? <Loader2 className="w-4 h-4 mr-2 animate-spin" /> : null}
                                {t('common.delete')}
                            </button>
                        </div>
                    </div>
                </div>
            )}

            {isEditModalOpen && subject && (
                <EditSubjectModal
                    subject={subject}
                    onClose={() => setIsEditModalOpen(false)}
                    onSuccess={() => {
                        setIsEditModalOpen(false);
                        fetchSubjectData();
                    }}
                />
            )}

        </div>
    );
};