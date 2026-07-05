import React, {useEffect, useState} from 'react';
import {useTranslation} from 'react-i18next';
import type {SubjectDTO, TeacherAssignmentDTO, UpdateSubjectDTO} from '../../types/SubjectDTO';
import {getSubjectDetails, toggleArchiveSubject, updateSubject, getSubjectTeachers} from '../../services/subjectService';
import {TeacherAssignmentManager} from './TeacherAssignmentManager';
import {Loader2} from 'lucide-react';

interface EditSubjectModalProps {
    subject: SubjectDTO;
    onClose: () => void;
    onSuccess: () => void;
}

export const EditSubjectModal: React.FC<EditSubjectModalProps> = ({subject, onClose, onSuccess}) => {
    const {t} = useTranslation();

    const [name, setName] = useState(subject.name || '');
    const [description, setDescription] = useState(subject.subjectDescription || '');

    const [tickets, setTickets] = useState(subject.manualRules?.studentTicketCount || 10);
    const [minTokens, setMinTokens] = useState(subject.manualRules?.minimumTokensMatch || 0);
    const [normalization, setNormalization] = useState(subject.manualRules?.enableNormalization || false);
    const [visibility, setVisibility] = useState(subject.manualRules?.raportLevelName || 'ONLY_PERCENTAGES');

    const [teachers, setTeachers] = useState<TeacherAssignmentDTO[]>([]);
    const [currentVersionHash, setCurrentVersionHash] = useState(subject.versionHash || '');

    const [isSubmitting, setIsSubmitting] = useState(false);
    const [isArchiving, setIsArchiving] = useState(false);
    const [error, setError] = useState<string | null>(null);
    const [showConfirmEdit, setShowConfirmEdit] = useState(false);

    useEffect(() => {
        if (subject.id) {
            Promise.all([
                getSubjectDetails(subject.id),
                getSubjectTeachers(subject.id)
            ])
                .then(([detailsData, teachersData]: [SubjectDTO, TeacherAssignmentDTO[]]) => {
                    setName(detailsData.name || '');
                    setDescription(detailsData.subjectDescription || '');

                    if (detailsData.manualRules) {
                        setTickets(detailsData.manualRules.studentTicketCount || 10);
                        setMinTokens(detailsData.manualRules.minimumTokensMatch || 0);
                        setNormalization(detailsData.manualRules.enableNormalization || false);
                        setVisibility(detailsData.manualRules.raportLevelName || 'ONLY_PERCENTAGES');
                    }

                    if (detailsData.versionHash) {
                        setCurrentVersionHash(detailsData.versionHash);
                    }

                    setTeachers(teachersData || []);
                })
                .catch((err: any) => {
                    console.error("Błąd pobierania danych przedmiotu: ", err);
                });
        }
    }, [subject.id]);

    const handlePreSubmit = () => {
        if (!name.trim()) {
            setError(t('subjectEdit.errorEmptyName'));
            return;
        }
        if (name.trim().length < 3) {
            setError(t('subjectEdit.errorShortName'));
            return;
        }
        if (description.trim() && description.trim().length > 500) {
            setError(t('subjectEdit.errorLongDesc'));
            return;
        }

        setError(null);
        setShowConfirmEdit(true);
    };

    const executeSubmit = async () => {
        setShowConfirmEdit(false);
        setIsSubmitting(true);
        setError(null);

        const dto: UpdateSubjectDTO = {
            name,
            subjectDescription: description,
            teachers,
            rules: {
                raportLevelName: visibility,
                studentTicketCount: tickets,
                minimumTokensMatch: minTokens,
                enableNormalization: normalization
            }
        };

        try {
            await updateSubject(subject.id!, dto, currentVersionHash);
            onSuccess();
        } catch (err: any) {
            let errorMsg = t('subjectEdit.defaultError');
            const data = err.response?.data;

            if (data) {
                if (Array.isArray(data.errors) && data.errors.length > 0) {
                    errorMsg = data.errors
                        .map((e: any) => e.defaultMessage || e.message)
                        .filter(Boolean)
                        .join(' | ');
                } else if (data.errors && typeof data.errors === 'object') {
                    errorMsg = Object.values(data.errors).join(' | ');
                } else if (data.message) {
                    errorMsg = data.message;
                } else if (typeof data === 'string') {
                    errorMsg = data;
                }
            }

            setError(errorMsg);
        } finally {
            setIsSubmitting(false);
        }
    };

    const handleToggleArchive = async () => {
        setIsArchiving(true);
        setError(null);
        try {
            await toggleArchiveSubject(subject.id!, currentVersionHash);
            onSuccess();
        } catch (err: any) {
            let errorMsg = t('subjectEdit.archiveError');
            const data = err.response?.data;
            if (data && data.message) {
                errorMsg = data.message;
            } else if (typeof data === 'string') {
                errorMsg = data;
            }
            setError(errorMsg);
        } finally {
            setIsArchiving(false);
        }
    };

    return (
        <div
            className="fixed inset-0 z-50 flex items-center justify-center bg-black/60 backdrop-blur-sm p-4 animate-fade-in">
            <div
                className="bg-surface w-full max-w-3xl rounded-2xl shadow-2xl flex flex-col max-h-[90vh] border border-border">

                <div className="flex justify-between items-center p-6 border-b border-border">
                    <div>
                        <h2 className="text-2xl font-bold text-primary">{t('subjectEdit.title')}</h2>
                        <p className="text-sm text-secondary mt-1">{t('subjectEdit.subtitle')}</p>
                    </div>
                    <button type="button" onClick={onClose}
                            className="text-secondary hover:text-danger p-2 transition-colors">
                        <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2}
                                  d="M6 18L18 6M6 6l12 12"/>
                        </svg>
                    </button>
                </div>

                <div className="p-6 overflow-y-auto flex flex-col gap-8">
                    {error && <div
                        className="p-4 bg-danger-subtle text-danger border border-danger-border rounded-lg text-sm">{error}</div>}

                    {subject.archived && (
                        <div
                            className="p-4 bg-amber-500/10 text-amber-600 border border-amber-500/20 rounded-lg text-sm font-bold flex items-center gap-2">
                            <svg className="w-5 h-5 flex-shrink-0" fill="none" stroke="currentColor"
                                 viewBox="0 0 24 24">
                                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2}
                                      d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z"/>
                            </svg>
                            {t('subjectEdit.archivedWarning')}
                        </div>
                    )}

                    <fieldset disabled={subject.archived || false} className="flex flex-col gap-8 disabled:opacity-60">
                        <section>
                            <h3 className="text-xs font-bold text-secondary tracking-widest mb-4 uppercase">{t('subjectEdit.generalInfo')}</h3>
                            <div className="flex flex-col gap-4">
                                <div>
                                    <label
                                        className="block text-sm font-semibold text-primary mb-1">{t('subjectEdit.nameLabel')}</label>
                                    <input
                                        type="text"
                                        value={name}
                                        onChange={e => setName(e.target.value)}
                                        className="w-full bg-base border border-border text-primary rounded-lg px-4 py-2 focus:outline-none focus:border-brand transition-colors"
                                    />
                                </div>
                                <div>
                                    <label
                                        className="block text-sm font-semibold text-primary mb-1">{t('subjectEdit.descLabel')}</label>
                                    <textarea
                                        rows={4}
                                        value={description}
                                        onChange={e => setDescription(e.target.value)}
                                        className="w-full bg-base border border-border text-primary rounded-lg px-4 py-2 focus:outline-none focus:border-brand transition-colors resize-none"
                                    />
                                </div>
                            </div>
                        </section>

                        {subject.canManageTeachers && (
                            <section className="bg-base p-6 rounded-xl border border-border">
                                <h3 className="text-xs font-bold text-secondary tracking-widest mb-4 uppercase">{t('subjectEdit.coTeachers')}</h3>
                                <TeacherAssignmentManager teachers={teachers} setTeachers={setTeachers}/>
                            </section>
                        )}

                        <section>
                            <h3 className="text-xs font-bold text-secondary tracking-widest mb-4 uppercase">{t('subjectEdit.giteaAnalysis')}</h3>
                            <div className="flex flex-col gap-6">

                                <div className="grid grid-cols-2 gap-4">
                                    <div>
                                        <label
                                            className="block text-sm font-semibold text-primary mb-1">{t('subjectEdit.tokens')}</label>
                                        <input
                                            type="number" min="0"
                                            value={tickets}
                                            onChange={e => setTickets(Number(e.target.value))}
                                            className="w-full bg-base border border-border text-primary rounded-lg px-4 py-2 focus:outline-none focus:border-brand"
                                        />
                                    </div>
                                    <div>
                                        <label
                                            className="block text-sm font-semibold text-primary mb-1">{t('subjectEdit.minTokens')}</label>
                                        <input
                                            type="number" min="0"
                                            value={minTokens}
                                            onChange={e => setMinTokens(Number(e.target.value))}
                                            className="w-full bg-base border border-border text-primary rounded-lg px-4 py-2 focus:outline-none focus:border-brand"
                                        />
                                    </div>
                                </div>

                                <label className="flex items-center gap-3 cursor-pointer">
                                    <input
                                        type="checkbox"
                                        checked={normalization}
                                        onChange={e => setNormalization(e.target.checked)}
                                        className="w-5 h-5 accent-brand rounded border-border"
                                    />
                                    <span
                                        className="text-sm font-semibold text-primary">{t('subjectEdit.normalization')}</span>
                                </label>

                                <div>
                                    <label
                                        className="block text-sm font-semibold text-primary mb-3">{t('subjectEdit.visibility')}</label>
                                    <div className="flex flex-col gap-3">
                                        {[
                                            {key: 'ONLY_PERCENTAGES', label: t('subjectEdit.levelPercentages')},
                                            {key: 'ONLY_HIGHEST_PERCENT', label: t('subjectEdit.levelHighest')},
                                            {key: 'NOTHING', label: t('subjectEdit.levelHidden')}
                                        ].map(level => (
                                            <label key={level.key} className="flex items-center gap-3 cursor-pointer">
                                                <input
                                                    type="radio"
                                                    name="visibility"
                                                    value={level.key}
                                                    checked={visibility === level.key}
                                                    onChange={e => setVisibility(e.target.value)}
                                                    className="w-4 h-4 accent-brand"
                                                />
                                                <span className="text-sm text-primary">{level.label}</span>
                                            </label>
                                        ))}
                                    </div>
                                </div>
                            </div>
                        </section>
                    </fieldset>

                    {subject.canManageTeachers && (
                        <section className="bg-base p-6 rounded-xl border border-border mt-2">
                            <h3 className="text-xs font-bold text-danger tracking-widest mb-4 uppercase">{t('subjectEdit.dangerZone')}</h3>
                            <div
                                className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4">
                                <div>
                                    <p className="text-sm font-bold text-primary">{subject.archived ? t('subjectEdit.unarchiveSubject') : t('subjectEdit.archiveSubject')}</p>
                                    <p className="text-xs text-secondary mt-1">
                                        {subject.archived ? t('subjectEdit.unarchiveDesc') : t('subjectEdit.archiveDesc')}
                                    </p>
                                </div>
                                <button
                                    type="button"
                                    onClick={handleToggleArchive}
                                    disabled={isArchiving}
                                    className={`px-4 py-2 rounded-lg text-sm font-bold transition-colors shadow-sm shrink-0 flex items-center gap-2 ${subject.archived ? 'bg-brand hover:bg-brand-hover text-white' : 'bg-danger-subtle text-danger hover:bg-danger hover:text-white'} disabled:opacity-50`}
                                >
                                    {isArchiving && <Loader2 className="w-4 h-4 animate-spin"/>}
                                    {subject.archived ? t('subjectEdit.unarchiveBtn') : t('subjectEdit.archiveBtn')}
                                </button>
                            </div>
                        </section>
                    )}

                </div>

                <div className="flex justify-end gap-4 p-6 border-t border-border bg-base rounded-b-2xl">
                    <button
                        type="button"
                        onClick={onClose}
                        disabled={isSubmitting}
                        className="px-6 py-2.5 rounded-lg text-sm font-bold text-secondary border border-border hover:bg-surface transition-colors"
                    >
                        {t('subjectEdit.cancel')}
                    </button>
                    <button
                        type="button"
                        onClick={handlePreSubmit}
                        disabled={isSubmitting || !!subject.archived}
                        className="px-6 py-2.5 rounded-lg text-sm font-bold text-white bg-brand hover:bg-brand-hover shadow-md transition-colors disabled:opacity-70 disabled:cursor-not-allowed"
                    >
                        {isSubmitting ? t('subjectEdit.loading') : t('subjectEdit.apply')}
                    </button>
                </div>

                {showConfirmEdit && (
                    <div
                        className="fixed inset-0 z-[60] flex items-center justify-center p-4 bg-black/60 backdrop-blur-sm animate-fade-in">
                        <div
                            className="bg-surface border border-border rounded-2xl shadow-xl max-w-md w-full p-6 space-y-6">
                            <div>
                                <h3 className="text-xl font-bold text-primary">
                                    {t('subjectEdit.confirmTitle')}
                                </h3>
                                <p className="text-sm text-secondary mt-2">
                                    {t('subjectEdit.confirmMessage')}
                                </p>
                            </div>
                            <div className="flex justify-end gap-3">
                                <button
                                    type="button"
                                    onClick={() => setShowConfirmEdit(false)}
                                    className="px-4 py-2.5 rounded-lg text-sm font-semibold text-secondary border border-border hover:bg-base transition-colors"
                                >
                                    {t('common.cancel')}
                                </button>
                                <button
                                    type="button"
                                    onClick={executeSubmit}
                                    className="px-5 py-2.5 rounded-lg text-sm font-bold text-white bg-brand hover:bg-brand-hover shadow-md transition-colors"
                                >
                                    {t('common.confirmAction')}
                                </button>
                            </div>
                        </div>
                    </div>
                )}
            </div>
        </div>
    );
}