import React from "react";

// By extending ButtonHTMLAttributes, this component automatically accepts
// all standard button props (type, onClick, onMouseEnter, etc.)
interface SubmitButtonProps extends React.ButtonHTMLAttributes<HTMLButtonElement> {
    isLoading?: boolean;
    children: React.ReactNode;
}

export default function SubmitButton({
                                         isLoading = false,
                                         children,
                                         className = "",
                                         disabled,
                                         ...props
                                     }: SubmitButtonProps) {
    return (
        <button
            disabled={isLoading || disabled}
            className={`w-full bg-[#7A1014] text-white font-bold py-3 rounded-md mt-2 hover:bg-red-900 transition-colors shadow-sm disabled:bg-gray-400 disabled:cursor-not-allowed flex justify-center items-center ${className}`}
            {...props}
        >
            {isLoading ? (
                <span className="w-5 h-5 border-2 border-white border-t-transparent rounded-full animate-spin"></span>
            ) : (
                children
            )}
        </button>
    );
}