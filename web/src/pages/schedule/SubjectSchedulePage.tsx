import React, {useCallback, useEffect, useMemo, useState} from 'react';
import {useTranslation} from 'react-i18next';
import {useNavigate, useParams} from 'react-router-dom';
import {scheduleService} from '../../services/scheduleService';
import {type FilterStatus, type ScheduleDTO, ScheduleStatus} from '../../types/schedule.types';
import {formatDate} from '../../services/reportService';
import {CreateScheduleModal} from "./CreateScheduleModal";
import {
    AlertCircle,
    ArrowDown,
    ArrowLeft,
    ArrowUp,
    ArrowUpDown,
    CalendarClock,
    Check,
    Edit,
    Search,
    RefreshCw,
    X,
    Trash2,
    Plus,
    ChevronDown
} from 'lucide-react';
import axios from 'axios';
import ConfirmationPopup from '../../shared/components/modals/ConfirmationPopup';

type SortColumn = 'date' | 'tag';

const getSafeTimestamp = (dateString?: string): number => {
    if (!dateString) return 0;
    const time = new Date(dateString).getTime();
    return isNaN(time) ? 0 : time;
};

const STATUS_STYLES: Record<ScheduleStatus, string> = {
    [ScheduleStatus.PLANNED]: 'bg-blue-600 text-white shadow-sm dark:bg-blue-700',
    [ScheduleStatus.EXECUTED]: 'bg-green-600 text-white shadow-sm dark:bg-green-700',
    [ScheduleStatus.FAILED]: 'bg-red-600 text-white shadow-sm dark:bg-red-700',
};

export const SubjectSchedulePage: React.FC = () => {
    const {id: subjectId} = useParams<{ id: string }>();
    const navigate = useNavigate();
    const {t} = useTranslation();

    const [schedules, setSchedules] = useState<ScheduleDTO[]>([]);
    const [isLoading, setIsLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);

    const [statusFilter, setStatusFilter] = useState<FilterStatus>('ALL');
    const [tagFilter, setTagFilter] = useState<string>('');

    const [sortColumn, setSortColumn] = useState<SortColumn>('date');
    const [sortDesc, setSortDesc] = useState<boolean>(true);

    const [isDeleteModalOpen, setIsDeleteModalOpen] = useState(false);
    const [isDeleting, setIsDeleting] = useState(false);
    const [scheduleToDelete, setScheduleToDelete] = useState<ScheduleDTO | null>(null);

    const [editingId, setEditingId] = useState<string | null>(null);
    const [editTag, setEditTag] = useState<string>('');
    const [editDate, setEditDate] = useState<string>('');

    const [isCreateModalOpen, setIsCreateModalOpen] = useState(false);

    const fetchSchedules = useCallback(async () => {
        if (!subjectId) return;
        setIsLoading(true);
        try {
            const {schedules: fetched} = await scheduleService.getSchedulesForSubject(subjectId);
            setSchedules(Array.isArray(fetched) ? fetched : []);
            setError(null);
        } catch (err: unknown) {
            if (axios.isAxiosError(err) && err.response) {
                console.log(err.response?.status);
            }
            setSchedules([]);
            setError(t('schedule.error.fetch'));
        } finally {
            setIsLoading(false);
        }
    }, [subjectId, t]);

    useEffect(() => {
        fetchSchedules();
    }, [fetchSchedules]);

    const handleSort = (column: SortColumn) => {
        if (sortColumn === column) {
            setSortDesc(prev => !prev);
        } else {
            setSortColumn(column);
            setSortDesc(false);
        }
    };

    const renderSortIcon = (column: SortColumn) => {
        if (sortColumn !== column)
            return <ArrowUpDown size={14}
                                className="text-secondary opacity-50 group-hover:opacity-100 transition-opacity"/>;
        return sortDesc
            ? <ArrowDown size={14} className="text-primary"/>
            : <ArrowUp size={14} className="text-primary"/>;
    };

    const openDeleteModal = (schedule: ScheduleDTO) => {
        setScheduleToDelete(schedule);
        setIsDeleteModalOpen(true);
    };

    const handleConfirmDelete = async () => {
        if (!scheduleToDelete) return;

        setIsDeleting(true);
        setError(null);

        try {
            await scheduleService.deleteSchedule(scheduleToDelete.id, scheduleToDelete.versionHash);
            await fetchSchedules();
        } catch (err: unknown) {
            if (axios.isAxiosError(err) && err.response?.status === 409) {
                await fetchSchedules();
            } else {
                setError(t('schedule.deleteScheduleError'));
            }
        } finally {
            setIsDeleting(false);
            setIsDeleteModalOpen(false);
            setScheduleToDelete(null);
        }
    };

    const handleEditClick = (schedule: ScheduleDTO) => {
        setEditingId(schedule.id);
        setEditTag(schedule.tag);
        if (schedule.scheduleDateTime) {
            setEditDate(schedule.scheduleDateTime.substring(0, 16));
        } else {
            setEditDate('');
        }
    };

    const handleSaveEdit = async (id: string) => {
        const schedule = schedules.find(s => s.id === id);
        if (!schedule) return;

        try {
            setIsLoading(true);
            const [datePart, timePart] = editDate.split('T');
            const formattedDate = `${datePart}T${timePart}:00.000`;

            const selectedDateTime = new Date(formattedDate).getTime();
            if (selectedDateTime <= Date.now()) {
                setError(t('schedule.modal.errorPast'));
                return;
            }

            await scheduleService.updateSchedule(id, {
                scheduleDateTime: formattedDate,
                tag: editTag,
                versionHash: schedule.versionHash,
            });

            setEditingId(null);
            setError(null);
            await fetchSchedules();
        } catch (err: unknown) {
            console.error(err);
            if (axios.isAxiosError(err) && err.response?.status === 409) {
                setEditingId(null);
                await fetchSchedules();
            } else {
                setEditingId(null);
                setError(t('schedule.error.update'));
            }
        } finally {
            setIsLoading(false);
        }
    };

    const sortedAndFiltered = useMemo(() => {
        const safeSchedules = Array.isArray(schedules) ? schedules : [];

        const mapped = safeSchedules.map(s => {
            let status: ScheduleStatus = ScheduleStatus.PLANNED;

            const isTaskFailed = s.hasFailed || (s as any).failed === true;
            const isTaskExecuted = s.isExecuted || (s as any).executed === true;

            if (isTaskFailed) {
                status = ScheduleStatus.FAILED;
            } else if (isTaskExecuted) {
                status = ScheduleStatus.EXECUTED;
            }

            return {
                ...s,
                calculatedStatus: status,
            };
        });

        const filtered = mapped.filter(s => {
            const matchesStatus = statusFilter === 'ALL' || s.calculatedStatus === statusFilter;
            const matchesTag = tagFilter === '' || (s.tag && s.tag.toLowerCase().includes(tagFilter.toLowerCase()));
            return matchesStatus && matchesTag;
        });

        return [...filtered].sort((a, b) => {
            const diff = sortColumn === 'tag'
                ? (a.tag || '').localeCompare(b.tag || '')
                : getSafeTimestamp(a.scheduleDateTime) - getSafeTimestamp(b.scheduleDateTime);

            return sortDesc ? -diff : diff;
        });
    }, [schedules, statusFilter, tagFilter, sortColumn, sortDesc]);

    const openCreateModal = () => {
        setIsCreateModalOpen(true);
    };

    const handleCreateSchedule = async (scheduleToSave: ScheduleDTO) => {
        if (!subjectId) return;

        try {
            setIsLoading(true);

            await scheduleService.createSchedule(subjectId, scheduleToSave);

            setIsCreateModalOpen(false);
            await fetchSchedules();
        } catch (err: unknown) {
            console.error("Błąd podczas dodawania harmonogramu:", err);
            setError('Wystąpił błąd podczas planowania analizy');
        } finally {
            setIsLoading(false);
        }
    };

    return (
        <div className="min-h-screen bg-base p-8">
            <div className="max-w-7xl mx-auto">

                <button
                    onClick={() => navigate(-1)}
                    className="flex items-center gap-2 text-secondary hover:text-primary transition-colors mb-6 font-medium"
                >
                    <ArrowLeft className="w-5 h-5"/>
                    {t('common.back')}
                </button>
                <div className="flex flex-col md:flex-row justify-between items-start md:items-center mb-6 gap-4">
                    <div>
                        <h1 className="text-3xl font-bold text-primary mb-2">
                            {t('schedule.modal.title')}
                        </h1>
                        <p className="text-secondary text-sm max-w-2xl leading-relaxed">
                            {t('schedule.subtitle')}
                        </p>
                    </div>

                    <div className="flex gap-4">
                        <button
                            onClick={fetchSchedules}
                            disabled={isLoading}
                            className="flex items-center gap-2 px-4 py-2 bg-surface border border-border hover:bg-active rounded-md text-sm font-bold text-primary transition-colors disabled:opacity-50"
                        >
                            <RefreshCw size={16} className={isLoading ? 'animate-spin' : ''}/>
                            {t('common.refresh')}
                        </button>
                        <button
                            onClick={openCreateModal}
                            className="flex items-center gap-2 px-4 py-2 bg-brand hover:bg-brand-hover rounded-md text-sm font-bold text-white transition-colors"
                        >
                            <Plus size={16}/>
                            {t('schedule.modal.title')}
                        </button>
                    </div>
                </div>

                <div className="flex flex-wrap items-center gap-3 mb-6">
                    <div
                        className="relative flex items-center bg-surface border border-border rounded-md px-3 py-1.5 focus-within:ring-2 focus-within:ring-brand transition-all">
                        <Search size={16} className="text-secondary mr-2"/>
                        <input
                            type="text"
                            placeholder={`${t('schedule.tag')}...`}
                            value={tagFilter}
                            onChange={e => setTagFilter(e.target.value)}
                            className="bg-transparent text-sm font-semibold text-primary focus:outline-none w-24 sm:w-32 placeholder:text-secondary/60"
                        />
                    </div>

                    <div
                        className="relative flex items-center bg-surface border border-border rounded-md px-3 py-1.5 focus-within:ring-2 focus-within:ring-brand transition-all">
                        <select
                            value={statusFilter}
                            onChange={e => setStatusFilter(e.target.value as FilterStatus)}
                            className="bg-transparent text-sm font-semibold text-primary focus:outline-none cursor-pointer appearance-none pr-4"
                        >
                            <option value="ALL">{t('schedule.status.all')}</option>
                            <option value={ScheduleStatus.PLANNED}>{t('schedule.status.planned')}</option>
                            <option value={ScheduleStatus.EXECUTED}>{t('schedule.status.executed')}</option>
                            <option value={ScheduleStatus.FAILED}>{t('schedule.status.failed')}</option>
                        </select>
                        <ChevronDown/>
                    </div>
                </div>

                {error && (
                    <div className="mb-6 p-4 bg-red-50 dark:bg-red-950/30 border border-red-200 dark:border-danger/30 text-danger rounded-lg flex items-center gap-3 text-sm font-medium animate-in fade-in slide-in-from-top-2">
                        <AlertCircle size={20} className="shrink-0" />
                        <p>{error}</p>
                        <button
                            onClick={() => setError(null)}
                            className="ml-auto p-1 hover:bg-red-100 dark:hover:bg-red-900/50 rounded-md transition-colors"
                        >
                            <X size={16} />
                        </button>
                    </div>
                )}

                <div className="bg-surface rounded-2xl shadow-sm border border-border overflow-visible">
                    <div className="overflow-x-visible">
                        <table className="w-full text-left border-collapse">
                            <thead>
                            <tr className="border-b border-border bg-surface select-none">
                                <th
                                    onClick={() => handleSort('date')}
                                    className="py-6 px-8 text-xs font-bold text-secondary uppercase tracking-widest cursor-pointer group hover:bg-active transition-colors"
                                >
                                    <div className="flex items-center gap-2">
                                        {t('schedule.date')}
                                        {renderSortIcon('date')}
                                    </div>
                                </th>
                                <th
                                    onClick={() => handleSort('tag')}
                                    className="py-6 px-8 text-xs font-bold text-secondary uppercase tracking-widest cursor-pointer group hover:bg-active transition-colors"
                                >
                                    <div className="flex items-center gap-2">
                                        {t('schedule.tag')}
                                        {renderSortIcon('tag')}
                                    </div>
                                </th>
                                <th className="py-6 px-8 text-xs font-bold text-secondary uppercase tracking-widest">
                                    {t('schedule.statusLabel')}
                                </th>
                                <th className="py-6 px-8 text-xs font-bold text-secondary uppercase tracking-widest text-right">
                                    {t('schedule.actions')}
                                </th>
                            </tr>
                            </thead>
                            <tbody
                                className={`divide-y divide-border transition-opacity duration-200 ${isLoading ? 'opacity-60 pointer-events-none' : ''}`}>
                            {sortedAndFiltered.length === 0 && !isLoading ? (
                                <tr>
                                    <td colSpan={4} className="py-8 text-center text-secondary">
                                        {t('schedule.empty')}
                                    </td>
                                </tr>
                            ) : (
                                sortedAndFiltered.map(s => {
                                    const isEditing = editingId === s.id;
                                    return (
                                        <tr key={s.id} className="hover:bg-base transition-colors group">
                                            <td className="py-5 px-8">
                                                <div className="flex items-center gap-3">
                                                    <div
                                                        className="w-10 h-10 rounded-lg bg-base flex items-center justify-center text-secondary border border-border group-hover:bg-surface transition-colors">
                                                        <CalendarClock size={20}/>
                                                    </div>
                                                    {isEditing ? (
                                                        <input
                                                            type="datetime-local"
                                                            value={editDate}
                                                            min={new Date().toISOString().slice(0, 16)}
                                                            onChange={e => setEditDate(e.target.value)}
                                                            className="bg-surface border border-border rounded px-2 py-1 text-sm text-primary focus:outline-none focus:ring-2 focus:ring-brand"
                                                        />
                                                    ) : (
                                                        <span className="text-sm font-bold text-primary">
                                                            {formatDate(s.scheduleDateTime)}
                                                        </span>
                                                    )}
                                                </div>
                                            </td>
                                            <td className="py-5 px-8 text-sm text-secondary font-medium font-mono">
                                                {isEditing ? (
                                                    <input
                                                        type="text"
                                                        value={editTag}
                                                        onChange={e => setEditTag(e.target.value)}
                                                        className="bg-surface border border-border rounded px-2 py-1 text-sm text-primary focus:outline-none focus:ring-2 focus:ring-brand w-full max-w-50"
                                                    />
                                                ) : (
                                                    s.tag
                                                )}
                                            </td>
                                            <td className="py-5 px-8">
                                                <span
                                                    className={`px-3 py-1 text-xs font-bold rounded-full whitespace-nowrap ${STATUS_STYLES[s.calculatedStatus]}`}>
                                                    {t(s.calculatedStatus).toUpperCase()}
                                                </span>
                                            </td>
                                            <td className="py-5 px-8 text-right">
                                                <div className="flex items-center justify-end gap-2">
                                                    {isEditing ? (
                                                        <>
                                                            <button
                                                                onClick={() => handleSaveEdit(s.id)}
                                                                className="p-1.5 text-green-600 hover:bg-green-50 dark:hover:bg-green-950/30 rounded transition-colors"
                                                                title={t('common.save')}
                                                            >
                                                                <Check size={18}/>
                                                            </button>
                                                            <button
                                                                onClick={() => setEditingId(null)}
                                                                className="p-1.5 text-red-500 hover:bg-red-50 dark:hover:bg-red-950/30 rounded transition-colors"
                                                                title={t('common.cancel')}
                                                            >
                                                                <X size={18}/>
                                                            </button>
                                                        </>
                                                    ) : (
                                                        s.calculatedStatus === ScheduleStatus.PLANNED && (
                                                            <button
                                                                onClick={() => handleEditClick(s)}
                                                                className="p-1.5 text-blue-500 hover:bg-blue-50 dark:hover:bg-blue-950/30 rounded transition-colors"
                                                                title={t('common.edit')}
                                                            >
                                                                <Edit size={18}/>
                                                            </button>
                                                        )
                                                    )}
                                                    <button
                                                        onClick={() => openDeleteModal(s)}
                                                        className="text-red-800 hover:text-red-600 dark:text-danger dark:hover:text-danger/80 transition-colors inline-flex items-center p-2 rounded-full hover:bg-red-800/10 dark:hover:bg-danger/10"
                                                        title={t('schedule.deleteScheduleTitle')}
                                                    >
                                                        <Trash2 size={18}/>
                                                    </button>
                                                </div>
                                            </td>
                                        </tr>
                                    );
                                })
                            )}
                            </tbody>
                        </table>
                    </div>
                </div>

                <CreateScheduleModal
                    isOpen={isCreateModalOpen}
                    onClose={() => setIsCreateModalOpen(false)}
                    onSave={handleCreateSchedule}
                />

                <ConfirmationPopup
                    isOpen={isDeleteModalOpen}
                    onCancel={() => {
                        setIsDeleteModalOpen(false);
                        setScheduleToDelete(null);
                    }}
                    onConfirm={handleConfirmDelete}
                    title={t('schedule.deleteScheduleTitle')}
                    description={t('schedule.deleteScheduleConfirm')}
                    confirmText={t('common.delete')}
                    cancelText={t('common.cancel')}
                    isLoading={isDeleting}
                />

            </div>
        </div>
    );
};