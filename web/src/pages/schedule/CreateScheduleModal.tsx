import React, { useState } from 'react';
import { Tag, AlertCircle } from 'lucide-react';
import { useTranslation } from 'react-i18next';
import type { ScheduleDTO } from "../../types/schedule.types.ts";

interface CreateScheduleModalProps {
    isOpen: boolean;
    onClose: () => void;
    onSave: (schedule: ScheduleDTO) => void;
}

export const CreateScheduleModal: React.FC<CreateScheduleModalProps> = ({ isOpen, onClose, onSave }) => {
    const TODAY_STR = new Date().toISOString().split('T')[0];
    const { t } = useTranslation();

    const [date, setDate] = useState('');
    const [time, setTime] = useState('');
    const [tag, setTag] = useState('');

    const [isPastDateTime, setIsPastDateTime] = useState(false);

    if (!isOpen) return null;

    const validatePastTime = (d: string, t: string) => {
        if (!d || !t) {
            setIsPastDateTime(false);
            return;
        }
        const selectedDateTime = new Date(`${d}T${t}:00`).getTime();
        const now = Date.now();
        setIsPastDateTime(selectedDateTime <= now);
    };

    const handleDateChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        const newDate = e.target.value;
        setDate(newDate);
        validatePastTime(newDate, time);
    };

    const handleTimeChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        const newTime = e.target.value;
        setTime(newTime);
        validatePastTime(date, newTime);
    };

    const handleClose = () => {
        setDate('');
        setTime('');
        setTag('');
        setIsPastDateTime(false);
        onClose();
    };

    const isFormValid = date !== '' && time !== '' && tag.trim() !== '' && !isPastDateTime;

    const handleApplyChanges = () => {
        if (!isFormValid) return;

        const scheduleDateTime = `${date}T${time}:00.000`;

        onSave({
            id: null,
            scheduleDateTime,
            tag
        } as unknown as ScheduleDTO);

        handleClose();
    };

    return (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/50 backdrop-blur-sm animate-in fade-in duration-200">
            <div className="bg-surface rounded-2xl shadow-xl w-full max-w-lg p-8 flex flex-col animate-in zoom-in-95 duration-200 border border-border">

                <div className="mb-8">
                    <h2 className="text-2xl font-extrabold text-primary mb-2">
                        {t('schedule.modal.title')}
                    </h2>
                    <p className="text-secondary text-sm">
                        {t('schedule.modal.desc')}
                    </p>
                </div>

                <div className="flex flex-col gap-5 mb-8">
                    <div>
                        <label className="block text-xs font-bold text-secondary tracking-widest uppercase mb-2 ml-1">
                            {t('schedule.modal.deadline')} *
                        </label>
                        <div className="flex gap-4">
                            <div className="relative flex-1">
                                <input
                                    type="date"
                                    min={TODAY_STR}
                                    value={date}
                                    onChange={handleDateChange}
                                    className={`w-full bg-base border rounded-lg px-4 py-3 text-sm font-medium text-primary focus:outline-none focus:ring-1 transition-all appearance-none ${isPastDateTime ? 'border-danger focus:border-danger focus:ring-danger' : 'border-border focus:border-brand focus:ring-brand'}`}
                                />
                            </div>

                            <div className="relative w-1/3">
                                <input
                                    type="time"
                                    value={time}
                                    onChange={handleTimeChange}
                                    className={`w-full bg-base border rounded-lg px-4 py-3 text-sm font-medium text-primary focus:outline-none focus:ring-1 transition-all appearance-none ${isPastDateTime ? 'border-danger focus:border-danger focus:ring-danger' : 'border-border focus:border-brand focus:ring-brand'}`}
                                />
                            </div>
                        </div>
                        {isPastDateTime && (
                            <p className="text-danger text-xs mt-2 flex items-center gap-1 font-medium ml-1">
                                <AlertCircle size={14} />
                                {t('schedule.modal.errorPast')}
                            </p>
                        )}
                    </div>

                    <div>
                        <label className="block text-xs font-bold text-secondary tracking-widest uppercase mb-2 ml-1">
                            {t('schedule.modal.giteaTag')} *
                        </label>
                        <div className="relative">
                            <input
                                type="text"
                                value={tag}
                                onChange={(e) => setTag(e.target.value)}
                                placeholder={t('schedule.modal.tagPlaceholder')}
                                className="w-full bg-base border border-border rounded-lg pl-11 pr-4 py-3 text-sm font-medium text-primary focus:outline-none focus:border-brand focus:ring-1 focus:ring-brand transition-all"
                            />
                            <Tag
                                size={18}
                                className="absolute left-4 top-1/2 -translate-y-1/2 text-secondary pointer-events-none"
                            />
                        </div>
                    </div>
                </div>

                <div className="flex justify-end items-center gap-3 mt-4 pt-4 border-t border-border">
                    <button
                        type="button"
                        onClick={handleClose}
                        className="px-5 py-2.5 text-sm font-bold text-secondary hover:text-primary hover:bg-base rounded-md transition-colors"
                    >
                        {t('common.cancel')}
                    </button>
                    <button
                        type="button"
                        onClick={handleApplyChanges}
                        disabled={!isFormValid}
                        className="px-6 py-2.5 bg-brand hover:bg-brand-hover disabled:bg-surface disabled:text-secondary disabled:border disabled:border-border text-white text-sm font-bold rounded-lg transition-colors shadow-sm disabled:shadow-none"
                    >
                        {t('common.create')}
                    </button>
                </div>

            </div>
        </div>
    );
};