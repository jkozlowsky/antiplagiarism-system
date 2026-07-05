import { AlertTriangle } from "lucide-react";
import { useTranslation } from "react-i18next";

export interface ConfirmationModalProps {
    isOpen: boolean;
    title: string;
    description: React.ReactNode;
    confirmText: string;
    cancelText?: string;
    onConfirm: () => void;
    onCancel: () => void;
    isLoading?: boolean;
}

export default function ConfirmationModal({
                                              isOpen,
                                              title,
                                              description,
                                              confirmText,
                                              cancelText,
                                              onConfirm,
                                              onCancel,
                                              isLoading = false
                                          }: ConfirmationModalProps) {
    const { t } = useTranslation();

    const finalCancelText = cancelText || t('common.cancel', 'Cancel');

    if (!isOpen) return null;

    return (
        <div className="fixed inset-0 z-[100] flex items-center justify-center p-4 bg-black/50 backdrop-blur-sm animate-in fade-in duration-200">
            <div className="bg-surface rounded-xl shadow-xl w-full max-w-md p-6 flex flex-col animate-in zoom-in-95 duration-200 border border-border">
                <div className="flex gap-4">
                    <div className="flex-shrink-0 w-10 h-10 rounded-lg bg-danger-subtle flex items-center justify-center border border-danger-border">
                        <AlertTriangle className="text-danger" size={20} strokeWidth={2.5} />
                    </div>

                    <div className="pt-1">
                        <h2 className="text-lg font-bold text-primary mb-2">
                            {title}
                        </h2>
                        <div className="text-sm text-secondary leading-relaxed">
                            {description}
                        </div>
                    </div>
                </div>

                <div className="flex justify-end items-center gap-2 mt-8">
                    <button
                        type="button"
                        onClick={onCancel}
                        disabled={isLoading}
                        className="px-4 py-2 text-sm font-bold text-secondary hover:text-primary transition-colors disabled:opacity-50"
                    >
                        {finalCancelText}
                    </button>
                    <button
                        id="confirmPopupBtn"
                        onClick={onConfirm}
                        disabled={isLoading}
                        className="px-6 py-2 bg-brand hover:bg-brand-hover text-white text-sm font-bold rounded-md transition-colors shadow-sm disabled:opacity-50 disabled:cursor-not-allowed flex items-center justify-center min-w-[120px]"
                    >
                        {isLoading ? (
                            <span className="w-4 h-4 border-2 border-white border-t-transparent rounded-full animate-spin"></span>
                        ) : (
                            confirmText
                        )}
                    </button>
                </div>
            </div>
        </div>
    );
}