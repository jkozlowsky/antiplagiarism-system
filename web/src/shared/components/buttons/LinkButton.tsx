import type {ReactNode} from "react";
import { Link, type LinkProps } from "react-router-dom";
interface LinkButtonProps extends LinkProps {
    variant?: 'primary' | 'ghost';
    children: ReactNode;
    className?: string;
}

export default function LinkButton({
                                       to,
                                       variant = 'primary',
                                       children,
                                       className = "",
                                       ...props
                                   }: LinkButtonProps) {

    const baseStyles = "inline-flex items-center justify-center transition-colors";

    const variants = {
        primary: "w-full bg-[#7A1014] text-white font-bold py-3 rounded-md hover:bg-red-900 shadow-sm",
        ghost: "text-sm font-semibold text-gray-500 hover:text-[#7A1014]"
    };

    return (
        <Link
            to={to}
            className={`${baseStyles} ${variants[variant]} ${className}`}
            {...props}
        >
            {children}
        </Link>
    );
}