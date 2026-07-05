import { useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { ruleService } from "../../services/ruleService";
import type { RulePresetDTO } from "../../types/rule.types";
import { RefreshCw, AlertCircle, Plus, X, Pencil, Trash2 } from "lucide-react";
import { useForm } from "react-hook-form";
import { yupResolver } from "@hookform/resolvers/yup";
import * as yup from "yup";
import axios from "axios";
import ConfirmationPopup from "../../shared/components/modals/ConfirmationPopup";
import { useBreadcrumb } from "../../contexts/BreadcrumbContext";

const schema = yup.object({
    raportLevelName: yup.string().required("validation.required"),
    studentTicketCount: yup.number()
        .transform((value) => (Number.isNaN(value) ? undefined : value))
        .required("validation.required")
        .min(0, "globalRules.error.invalidTickets"),
    minimumTokensMatch: yup.number()
        .transform((value) => (Number.isNaN(value) ? undefined : value))
        .required("validation.required")
        .min(1, "globalRules.error.invalidTokens"),
    enableNormalization: yup.boolean().required()
});

type RuleFormData = yup.InferType<typeof schema>;

export default function GlobalRulesPage() {
    const { t } = useTranslation();
    const { setDynamicBreadcrumb } = useBreadcrumb();

    const [rules, setRules] = useState<RulePresetDTO[]>([]);
    const [isLoading, setIsLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);
    const [apiError, setApiError] = useState<string | null>(null);

    const [showModal, setShowModal] = useState(false);
    const [editingRuleId, setEditingRuleId] = useState<string | null>(null);
    const [editingRuleVersionHash, setEditingRuleVersionHash] = useState<string | null>(null);

    const [isDeleteModalOpen, setIsDeleteModalOpen] = useState(false);
    const [isDeleting, setIsDeleting] = useState(false);
    const [ruleToDelete, setRuleToDelete] = useState<RulePresetDTO | null>(null);

    const [isEditConfirmOpen, setIsEditConfirmOpen] = useState(false);
    const [isUpdating, setIsUpdating] = useState(false);
    const [pendingEditData, setPendingEditData] = useState<RuleFormData | null>(null);

    const { register, handleSubmit, reset, formState: { errors, isSubmitting } } = useForm<RuleFormData>({
        resolver: yupResolver(schema),
        defaultValues: {
            raportLevelName: 'NOTHING',
            studentTicketCount: 0,
            minimumTokensMatch: 1,
            enableNormalization: false
        }
    });

    useEffect(() => {
        setDynamicBreadcrumb('sidebar.globalRules');
        return () => setDynamicBreadcrumb(null);
    }, [setDynamicBreadcrumb]);

    const fetchRules = async () => {
        setIsLoading(true);
        setError(null);
        try {
            const data = await ruleService.getRulePresetsTemplates();
            setRules(data);
        } catch {
            setError(t('globalRules.error.fetch'));
        } finally {
            setIsLoading(false);
        }
    };

    useEffect(() => {
        fetchRules();
    }, []);

    const openCreateModal = () => {
        setEditingRuleId(null);
        setEditingRuleVersionHash(null);
        reset({
            raportLevelName: 'NOTHING',
            studentTicketCount: 0,
            minimumTokensMatch: 1,
            enableNormalization: false
        });
        setApiError(null);
        setShowModal(true);
    };

    const openEditModal = (rule: RulePresetDTO) => {
        setEditingRuleId(rule.id);
        setEditingRuleVersionHash(rule.versionHash);
        reset({
            raportLevelName: rule.raportLevelName,
            studentTicketCount: rule.studentTicketCount,
            minimumTokensMatch: rule.minimumTokensMatch,
            enableNormalization: rule.enableNormalization
        });
        setApiError(null);
        setShowModal(true);
    };

    const closeModal = () => {
        setShowModal(false);
        setApiError(null);
    };

    const onSubmit = async (data: RuleFormData) => {
        setApiError(null);

        if (editingRuleId) {
            setPendingEditData(data);
            setIsEditConfirmOpen(true);
        } else {
            try {
                await ruleService.createRulePreset(data);
                setShowModal(false);
                fetchRules();
            } catch (err: unknown) {
                if (axios.isAxiosError(err)) {
                    setApiError(err.response?.data?.message || err.response?.data || t('globalRules.error.createFailed'));
                } else {
                    setApiError(t('globalRules.error.createFailed'));
                }
            }
        }
    };

    const handleConfirmEdit = async () => {
        if (!editingRuleId || !pendingEditData || !editingRuleVersionHash) return;

        setIsUpdating(true);
        setApiError(null);

        try {
            await ruleService.updateRulePreset(editingRuleId, pendingEditData, editingRuleVersionHash);
            setIsEditConfirmOpen(false);
            setShowModal(false);
            fetchRules();
        } catch (err: unknown) {
            setIsEditConfirmOpen(false);
            if (axios.isAxiosError(err)) {
                if (err.response?.status === 409) {
                    setApiError(t('globalRules.conflictError'));
                } else {
                    setApiError(err.response?.data?.message || err.response?.data || t('globalRules.error.updateFailed'));
                }
            } else {
                setApiError(t('globalRules.error.updateFailed'));
            }
        } finally {
            setIsUpdating(false);
        }
    };

    const openDeleteModal = (rule: RulePresetDTO) => {
        setRuleToDelete(rule);
        setIsDeleteModalOpen(true);
    };

    const handleConfirmDelete = async () => {
        if (!ruleToDelete) return;

        setIsDeleting(true);
        setError(null);

        try {
            await ruleService.deleteRulePreset(ruleToDelete.id, ruleToDelete.versionHash);
            fetchRules();
        } catch (err: unknown) {
            if (axios.isAxiosError(err)) {
                if (err.response?.status === 409) {
                    setError(t('globalRules.conflictError'));
                    fetchRules();
                } else if (err.response?.status === 404) {
                    setError(t('globalRules.deleteErrorNotFound'));
                    fetchRules();
                } else {
                    setError(t('globalRules.deleteError'));
                }
            } else {
                setError(t('globalRules.deleteError'));
            }
        } finally {
            setIsDeleting(false);
            setIsDeleteModalOpen(false);
            setRuleToDelete(null);
        }
    };

    return (
        <div className="min-h-screen bg-base p-8">
            <div className="max-w-7xl mx-auto">
                <div className="flex justify-between items-center mb-8">
                    <h1 className="text-3xl font-bold text-primary">
                        {t('sidebar.globalRules')}
                    </h1>
                    <div className="flex gap-4">
                        <button
                            onClick={fetchRules}
                            disabled={isLoading}
                            className="flex items-center gap-2 px-4 py-2 bg-surface cursor-pointer border border-border hover:bg-active rounded-md text-sm font-bold text-primary transition-colors"
                        >
                            <RefreshCw size={16} className={isLoading ? "animate-spin" : ""} />
                            {t('common.refresh')}
                        </button>
                        <button
                            onClick={openCreateModal}
                            className="flex items-center gap-2 px-4 py-2 bg-brand hover:bg-brand-hover cursor-pointer rounded-md text-sm font-bold text-white transition-colors"
                        >
                            <Plus size={16} />
                            {t('globalRules.add.title')}
                        </button>
                    </div>
                </div>

                {showModal && (
                    <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50 p-4">
                        <form onSubmit={handleSubmit(onSubmit)} className="bg-surface p-6 rounded-xl w-full max-w-md border border-border shadow-xl">
                            <div className="flex justify-between items-center mb-4">
                                <h2 className="text-xl font-bold text-primary">
                                    {editingRuleId ? t('globalRules.edit.title') : t('globalRules.add.title')}
                                </h2>
                                <button type="button" onClick={closeModal} className="text-secondary hover:text-primary transition-colors">
                                    <X size={20}/>
                                </button>
                            </div>

                            {apiError && (
                                <div className="mb-4 p-3 bg-danger-subtle border border-danger text-danger rounded-md text-sm flex items-center gap-2 font-semibold">
                                    <AlertCircle size={16} />
                                    {apiError}
                                </div>
                            )}

                            <div className="space-y-4">
                                <div>
                                    <label className="block text-sm font-semibold text-primary mb-1">
                                        {t('globalRules.form.raportLevelLabel')}
                                    </label>
                                    <select
                                        className={`w-full p-2 border rounded bg-surface text-primary transition-colors ${errors.raportLevelName ? 'border-danger' : 'border-border'}`}
                                        {...register("raportLevelName")}
                                        disabled={isSubmitting}
                                    >
                                        <option value="NOTHING">{t('globalRules.levels.NOTHING')}</option>
                                        <option value="ONLY_PERCENTAGES">{t('globalRules.levels.ONLY_PERCENTAGES')}</option>
                                        <option value="ONLY_HIGHEST_PERCENT">{t('globalRules.levels.ONLY_HIGHEST_PERCENT')}</option>
                                    </select>
                                    {errors.raportLevelName && <p className="text-danger text-xs mt-1">{t(errors.raportLevelName.message as string)}</p>}
                                </div>

                                <div>
                                    <label className="block text-sm font-semibold text-primary mb-1">
                                        {t('globalRules.form.studentTicketsLabel')}
                                    </label>
                                    <input
                                        type="text"
                                        inputMode="numeric"
                                        pattern="[0-9]*"
                                        className={`w-full p-2 border rounded bg-surface text-primary transition-colors ${errors.studentTicketCount ? 'border-danger' : 'border-border'}`}
                                        {...register("studentTicketCount")}
                                        onInput={(e) => {
                                            e.currentTarget.value = e.currentTarget.value.replace(/[^0-9]/g, '');
                                        }}
                                        disabled={isSubmitting}
                                    />
                                    {errors.studentTicketCount && <p className="text-danger text-xs mt-1">{t(errors.studentTicketCount.message as string)}</p>}
                                </div>

                                <div>
                                    <label className="block text-sm font-semibold text-primary mb-1">
                                        {t('globalRules.form.minTokensLabel')}
                                    </label>
                                    <input
                                        type="text"
                                        inputMode="numeric"
                                        pattern="[0-9]*"
                                        className={`w-full p-2 border rounded bg-surface text-primary transition-colors ${errors.minimumTokensMatch ? 'border-danger' : 'border-border'}`}
                                        {...register("minimumTokensMatch")}
                                        onInput={(e) => {
                                            e.currentTarget.value = e.currentTarget.value.replace(/[^0-9]/g, '');
                                        }}
                                        disabled={isSubmitting}
                                    />
                                    {errors.minimumTokensMatch && <p className="text-danger text-xs mt-1">{t(errors.minimumTokensMatch.message as string)}</p>}
                                </div>

                                <div>
                                    <label className="flex items-center gap-2 text-primary cursor-pointer mt-2">
                                        <input
                                            type="checkbox"
                                            className="rounded border-border w-4 h-4"
                                            {...register("enableNormalization")}
                                            disabled={isSubmitting}
                                        />
                                        <span className="text-sm font-medium">{t('globalRules.form.normalizationLabel')}</span>
                                    </label>
                                </div>
                            </div>

                            <button type="submit" disabled={isSubmitting} className="w-full mt-6 bg-brand text-white py-2 rounded font-bold hover:bg-brand-hover transition-colors disabled:opacity-50 flex justify-center items-center">
                                {isSubmitting ? (
                                    <RefreshCw className="animate-spin text-white" size={20} />
                                ) : (
                                    editingRuleId ? t('globalRules.edit.submit') : t('globalRules.add.submit')
                                )}
                            </button>
                        </form>
                    </div>
                )}

                <ConfirmationPopup
                    isOpen={isEditConfirmOpen}
                    onCancel={() => setIsEditConfirmOpen(false)}
                    onConfirm={handleConfirmEdit}
                    title={t('globalRules.editConfirmTitle')}
                    description={t('globalRules.editConfirmDescription')}
                    confirmText={t('common.save')}
                    cancelText={t('common.cancel')}
                    isLoading={isUpdating}
                />

                <ConfirmationPopup
                    isOpen={isDeleteModalOpen}
                    onCancel={() => {
                        setIsDeleteModalOpen(false);
                        setRuleToDelete(null);
                    }}
                    onConfirm={handleConfirmDelete}
                    title={t('globalRules.deleteTitle')}
                    description={t('globalRules.deleteConfirmation')}
                    confirmText={t('common.delete')}
                    cancelText={t('common.cancel')}
                    isLoading={isDeleting}
                />

                {error && (
                    <div className="mb-6 p-4 bg-danger-subtle border border-danger text-danger rounded-xl flex items-center gap-3 text-sm font-semibold animate-in fade-in slide-in-from-top-4">
                        <AlertCircle size={20} />
                        {error}
                    </div>
                )}

                <div className="bg-surface rounded-2xl shadow-sm border border-border overflow-hidden">
                    <table className="w-full text-left border-collapse">
                        <thead>
                        <tr className="border-b border-border bg-base">
                            <th className="py-5 px-8 text-xs font-bold text-secondary uppercase">{t('globalRules.table.raportLevel')}</th>
                            <th className="py-5 px-8 text-xs font-bold text-secondary uppercase">{t('globalRules.table.studentTickets')}</th>
                            <th className="py-5 px-8 text-xs font-bold text-secondary uppercase">{t('globalRules.table.minTokens')}</th>
                            <th className="py-5 px-8 text-xs font-bold text-secondary uppercase">{t('globalRules.table.normalization')}</th>
                            <th className="py-5 px-8 text-xs font-bold text-secondary uppercase text-right">{t('globalRules.table.actions')}</th>
                        </tr>
                        </thead>
                        <tbody className="divide-y divide-border">
                        {isLoading && rules.length === 0 ? (
                            <tr>
                                <td colSpan={5} className="py-12 text-center text-secondary">{t('common.loading')}</td>
                            </tr>
                        ) : rules.length === 0 ? (
                            <tr>
                                <td colSpan={5} className="py-12 text-center text-secondary">{t('globalRules.noResults')}</td>
                            </tr>
                        ) : (
                            rules.map((rule) => (
                                <tr key={rule.id} className="hover:bg-base/40 transition-colors">
                                    <td className="py-4 px-8 text-sm text-primary font-semibold">
                                        {t(`globalRules.levels.${rule.raportLevelName.toUpperCase().replace(/\s+/g, '_')}`, rule.raportLevelName.replace(/_/g, ' '))}
                                    </td>
                                    <td className="py-4 px-8 text-sm text-primary">{rule.studentTicketCount}</td>
                                    <td className="py-4 px-8 text-sm text-primary">{rule.minimumTokensMatch}</td>
                                    <td className="py-4 px-8 text-sm text-primary">{rule.enableNormalization ? t('common.yes') : t('common.no')}</td>
                                    <td className="py-4 px-8 text-sm text-primary text-right space-x-2">
                                        <button
                                            onClick={() => openEditModal(rule)}
                                            className="text-brand hover:text-brand-hover dark:text-brand-300 dark:hover:text-brand-100 transition-colors inline-flex items-center p-2 rounded-full hover:bg-brand/10 dark:hover:bg-brand/20"
                                            title={t('globalRules.edit.title')}
                                        >
                                            <Pencil size={18} />
                                        </button>
                                        <button
                                            onClick={() => openDeleteModal(rule)}
                                            className="text-red-800 hover:text-red-600 dark:text-danger dark:hover:text-danger/80 transition-colors inline-flex items-center p-2 rounded-full hover:bg-red-800/10 dark:hover:bg-danger/10"
                                            title={t('globalRules.deleteTitle')}
                                        >
                                            <Trash2 size={18} />
                                        </button>
                                    </td>
                                </tr>
                            ))
                        )}
                        </tbody>
                    </table>
                </div>
            </div>
        </div>
    );
}