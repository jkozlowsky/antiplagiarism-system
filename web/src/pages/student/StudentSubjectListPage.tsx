import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { getStudentSubjects } from '../../services/subjectService';
import type { SubjectDTO } from '../../types/SubjectDTO';
import { useTranslation } from 'react-i18next';
import { PATHS } from '../../routes/paths';
import { useBreadcrumb } from '../../contexts/BreadcrumbContext';

export const StudentSubjectListPage: React.FC = () => {
    const navigate = useNavigate();
    const { t, i18n } = useTranslation();
    const { setDynamicBreadcrumb } = useBreadcrumb();

    useEffect(() => {
        setDynamicBreadcrumb('sidebar.subjectList');
        return () => setDynamicBreadcrumb(null);
    }, [setDynamicBreadcrumb]);

    const [subjects, setSubjects] = useState<SubjectDTO[]>([]);
    const [loading, setLoading] = useState<boolean>(true);
    const [error, setError] = useState<string | null>(null);
    const [activeTab, setActiveTab] = useState<'active' | 'archived'>('active');

    useEffect(() => {
        getStudentSubjects()
            .then(data => {
                setSubjects(data);
                setLoading(false);
            })
            .catch(() => {
                setError(t('subject.list.fetchError'));
                setLoading(false);
            });
    }, [t]);

    const activeSubjects = subjects.filter(sub => !sub.archived);
    const archivedSubjects = subjects.filter(sub => sub.archived);
    const displayedSubjects = activeTab === 'active' ? activeSubjects : archivedSubjects;

    const handleSubjectClick = (subjectId?: string | null) => {
        if (!subjectId) return;
        navigate(PATHS.STUDENT_SUBJECT_DETAILS.replace(':id', subjectId));
    };

    if (loading) return <div className="p-8 text-center text-secondary">{t('common.loading')}</div>;
    if (error) return <div className="p-8 text-center text-danger font-medium">{error}</div>;

    return (
        <div className="p-6 md:p-10 max-w-[1400px] mx-auto min-h-screen bg-base">

            <div className="mb-8">
                <h1 className="text-3xl font-bold text-primary mb-3">
                    {t('subject.student.title')}
                </h1>
                <p className="text-secondary text-sm max-w-2xl">
                    {t('subject.student.description')}
                </p>
            </div>

            <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center border-b border-border mb-8 gap-4">
                <div className="flex space-x-8">
                    <button
                        onClick={() => setActiveTab('active')}
                        className={`text-sm font-bold pb-4 -mb-[1px] transition-colors ${activeTab === 'active' ? 'text-brand border-b-2 border-brand' : 'text-secondary hover:text-primary'}`}
                    >
                        {t('subject.list.tabs.active')} ({activeSubjects.length})
                    </button>
                    <button
                        onClick={() => setActiveTab('archived')}
                        className={`text-sm font-bold pb-4 -mb-[1px] transition-colors ${activeTab === 'archived' ? 'text-brand border-b-2 border-brand' : 'text-secondary hover:text-primary'}`}
                    >
                        {t('subject.list.tabs.archived')} ({archivedSubjects.length})
                    </button>
                </div>
            </div>

            {displayedSubjects.length === 0 ? (
                <div className="text-center bg-surface border border-border rounded-xl p-12 shadow-sm">
                    <p className="text-secondary text-lg">{t('subject.list.empty')}</p>
                </div>
            ) : (
                <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-8">
                    {displayedSubjects.map((sub) => (
                        <div
                            onClick={() => handleSubjectClick(sub.id)}
                            key={sub.id}
                            className="bg-surface border border-border rounded-2xl shadow-sm hover:shadow-xl transition-all duration-300 flex flex-col h-full overflow-hidden group cursor-pointer"
                        >
                            <div className="h-44 bg-active relative flex items-center justify-center overflow-hidden shrink-0 border-b border-border">
                                <img
                                    src={`https://api.dicebear.com/7.x/shapes/svg?seed=${sub.id || sub.name}`}
                                    alt={`Okładka przedmiotu ${sub.name}`}
                                    className="w-full h-full object-cover group-hover:scale-105 transition-transform duration-300"
                                />
                                <div className="absolute top-4 left-4 bg-surface shadow border border-border text-primary text-xs font-bold px-2.5 py-1 rounded">
                                    {sub.edition}
                                </div>
                            </div>

                            <div className="p-6 flex-grow flex flex-col">
                                <h3
                                    className="text-xl font-bold text-primary mb-2 line-clamp-2 leading-snug group-hover:text-brand transition-colors cursor-pointer"
                                    onClick={(e) => { e.stopPropagation(); handleSubjectClick(sub.id); }}
                                >
                                    {sub.name}
                                </h3>

                                <p className="text-secondary text-sm mb-5 line-clamp-3">
                                    {sub.subjectDescription || t('subject.list.noDescription')}
                                </p>

                                <div className="mt-auto flex justify-between items-end mb-4">
                                    <div className="flex flex-col">
                                        <span className="text-xs text-secondary font-semibold mb-1">
                                            {t('subject.list.card.nextScan')}
                                        </span>
                                        <span className={`text-sm font-medium ${sub.archived ? 'text-danger' : sub.nextScan ? 'text-blue-600 dark:text-blue-400' : 'text-secondary'}`}>
                                            {sub.archived
                                                ? t('subject.list.card.status.archived')
                                                : sub.nextScan
                                                    ? new Date(sub.nextScan).toLocaleString(i18n.language, {
                                                        dateStyle: 'medium',
                                                        timeStyle: 'short'
                                                    })
                                                    : t('subject.list.card.status.noScanPlanned')}
                                        </span>
                                    </div>
                                </div>

                                <div className="flex gap-3">
                                    <button
                                        onClick={(e) => { e.stopPropagation(); handleSubjectClick(sub.id); }}
                                        className="flex-grow bg-brand text-white text-sm font-bold py-2.5 rounded-lg hover:bg-brand-hover transition-colors shadow-sm"
                                    >
                                        {t('subject.list.card.btn.view')}
                                    </button>
                                </div>
                            </div>
                        </div>
                    ))}
                </div>
            )}
        </div>
    );
};