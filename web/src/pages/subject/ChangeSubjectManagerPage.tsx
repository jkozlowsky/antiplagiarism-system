import React, {useEffect, useState} from 'react';
import {useTranslation} from 'react-i18next';
import {AlertCircle, CheckCircle2, Loader2, UserCheck} from 'lucide-react';
import axios from 'axios';
import SubmitButton from '../../shared/components/buttons/SubmitButton';
import {getSubjectTeachers, subjectService} from '../../services/subjectService';
import type {SubjectDTO, TeacherAssignmentDTO} from '../../types/SubjectDTO';

export const ChangeSubjectManagerPage: React.FC = () => {
    const {t} = useTranslation();

    const [subjects, setSubjects] = useState<SubjectDTO[]>([]);
    const [selectedSubjectId, setSelectedSubjectId] = useState<string>('');
    const [isSubjectsLoading, setIsSubjectsLoading] = useState<boolean>(false);

    const [assignedTeachers, setAssignedTeachers] = useState<TeacherAssignmentDTO[]>([]);
    const [selectedTeacherLogin, setSelectedTeacherLogin] = useState<string>('');
    const [isTeachersLoading, setIsTeachersLoading] = useState<boolean>(false);

    const [isLoading, setIsLoading] = useState<boolean>(false);
    const [submitError, setSubmitError] = useState<string | null>(null);
    const [successMessage, setSuccessMessage] = useState<string | null>(null);

    useEffect(() => {
        const fetchSubjects = async () => {
            setIsSubjectsLoading(true);
            try {
                const data = await subjectService.getAllSubjectsForAdmin();
                setSubjects(data);
            } catch (err) {
                setSubmitError(t('subject.manager.error.fetchSubjects'));
            } finally {
                setIsSubjectsLoading(false);
            }
        };
        fetchSubjects();
    }, [t]);

    useEffect(() => {
        if (!selectedSubjectId) {
            setAssignedTeachers([]);
            setSelectedTeacherLogin('');
            return;
        }

        const fetchTeachers = async () => {
            setIsTeachersLoading(true);
            setSubmitError(null);
            setSuccessMessage(null);

            try {
                const data = await getSubjectTeachers(selectedSubjectId);
                const candidates = (data || []).filter((t: TeacherAssignmentDTO) => t.role !== 'OWNER');
                setAssignedTeachers(candidates);
                setSelectedTeacherLogin('');
            } catch (err) {
                console.error(err);
                setSubmitError(t('subject.manager.error.fetchTeachers'));
            } finally {
                setIsTeachersLoading(false);
            }
        };

        fetchTeachers();
    }, [selectedSubjectId, t]);

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();

        if (!selectedSubjectId || !selectedTeacherLogin) {
            setSubmitError(t('subject.manager.error.notVerified'));
            return;
        }

        const selectedSubject = subjects.find(s => s.id === selectedSubjectId);
        if (!selectedSubject || !selectedSubject.versionHash) {
            setSubmitError(t('subject.deleteVersionError'));
            return;
        }

        setIsLoading(true);
        setSubmitError(null);
        setSuccessMessage(null);

        try {
            await subjectService.changeSubjectManager(selectedSubjectId, selectedTeacherLogin, selectedSubject.versionHash);
            setSuccessMessage(t('subject.manager.success'));
            setSelectedTeacherLogin('');
            setSelectedSubjectId('');
            setAssignedTeachers([]);

            const updatedSubjects = await subjectService.getAllSubjectsForAdmin();
            setSubjects(updatedSubjects);

        } catch (err) {
            if (axios.isAxiosError(err)) {
                if (err.response?.status === 404) setSubmitError(t('subject.manager.error.notFound'));
                else if (err.response?.status === 409) setSubmitError(t('subject.manager.error.conflict'));
                else if (err.response?.status === 403) setSubmitError(t('subject.manager.error.forbidden'));
                else setSubmitError(t('subject.manager.error.unexpected'));
            } else {
                setSubmitError(t('subject.manager.error.unexpected'));
            }
        } finally {
            setIsLoading(false);
        }
    };

    return (
        <div className="min-h-screen bg-base p-8">
            <div className="max-w-7xl mx-auto">
                <div className="flex flex-col md:flex-row justify-between items-start md:items-center mb-8 gap-4">
                    <div>
                        <h1 className="text-3xl font-bold text-primary mb-2 flex items-center gap-3">
                            {t('subject.manager.change.title')}
                        </h1>
                        <p className="text-secondary text-sm max-w-2xl">
                            {t('subject.manager.subtitle')}
                        </p>
                    </div>
                </div>

                <div className="bg-surface w-full max-w-3xl rounded-2xl shadow-sm border border-border p-6 md:p-8">
                    <form onSubmit={handleSubmit} className="space-y-6">
                        {submitError && (
                            <div className="mb-6 p-4 bg-danger-subtle border border-danger text-danger rounded-xl flex items-center gap-3 text-sm font-semibold animate-in fade-in duration-300">
                                <AlertCircle size={20} className="shrink-0" />
                                {submitError}
                            </div>
                        )}

                        {successMessage && (
                            <div className="mb-6 p-4 bg-emerald-50 border border-emerald-200 text-emerald-800 rounded-xl flex items-center gap-3 text-sm font-semibold animate-in fade-in duration-300">
                                <CheckCircle2 size={20} className="shrink-0" />
                                {successMessage}
                            </div>
                        )}

                        <div>
                            <label className="block text-xs font-bold text-secondary uppercase tracking-wider mb-2">
                                {t('subject.manager.selectSubject')}
                            </label>
                            <select
                                value={selectedSubjectId}
                                onChange={(e) => setSelectedSubjectId(e.target.value)}
                                className="w-full px-4 py-3 bg-surface border border-border rounded-md text-primary focus:ring-2 focus:ring-brand outline-none transition-all text-sm font-medium cursor-pointer"
                                required
                                disabled={isSubjectsLoading}
                            >
                                <option value="" disabled>
                                    {isSubjectsLoading ? t('common.loading') : t('subject.manager.selectPlaceholder')}
                                </option>
                                {subjects.map((subject) => (
                                    <option key={subject.id} value={subject.id || ''}>
                                        {subject.name} ({subject.edition})
                                    </option>
                                ))}
                            </select>
                        </div>

                        <div>
                            <label className="block text-xs font-bold text-secondary uppercase tracking-wider mb-2">
                                {t('subject.manager.selectNewManager')}
                            </label>
                            <div className="relative border border-border rounded-md focus-within:ring-2 focus-within:ring-brand transition-all bg-surface">
                                <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none text-secondary">
                                    {isTeachersLoading ? <Loader2 size={16} className="animate-spin text-brand" /> : <UserCheck size={16} />}
                                </div>
                                <select
                                    value={selectedTeacherLogin}
                                    onChange={(e) => {
                                        setSelectedTeacherLogin(e.target.value);
                                        setSubmitError(null);
                                        setSuccessMessage(null);
                                    }}
                                    className="w-full pl-10 pr-4 py-3 bg-transparent outline-none text-primary text-sm font-medium cursor-pointer disabled:opacity-50"
                                    required
                                    disabled={!selectedSubjectId || isTeachersLoading || assignedTeachers.length === 0}
                                >
                                    <option value="" disabled>
                                        {isTeachersLoading
                                            ? t('common.loading')
                                            : (!selectedSubjectId
                                                ? t('subject.manager.selectFirst')
                                                : t('subject.manager.selectFromList'))}
                                    </option>
                                    {assignedTeachers.map((teacher) => (
                                        <option key={teacher.login} value={teacher.login}>
                                            {teacher.login} {t('subject.manager.currentRole', { role: teacher.role })}
                                        </option>
                                    ))}
                                </select>
                            </div>
                            {selectedSubjectId && assignedTeachers.length === 0 && !isTeachersLoading && (
                                <p className="text-xs text-danger mt-2">
                                    {t('subject.manager.error.noCandidates')}
                                </p>
                            )}
                        </div>

                        <div className="pt-4 border-t border-border mt-2">
                            <p className="mb-6 text-xs text-secondary leading-relaxed bg-base p-3 rounded border border-border">
                                {t('subject.manager.warning')}
                            </p>
                            <div className="flex justify-end">
                                <SubmitButton
                                    isLoading={isLoading}
                                    disabled={!selectedTeacherLogin || !selectedSubjectId || isTeachersLoading}
                                    className="px-8 py-2.5 w-auto mt-0 text-sm tracking-widest uppercase"
                                >
                                    {t('subject.manager.change.submit')}
                                </SubmitButton>
                            </div>
                        </div>
                    </form>
                </div>
            </div>
        </div>
    );
};