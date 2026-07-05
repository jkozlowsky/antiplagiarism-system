import { Link, useLocation } from "react-router-dom";
import type { LucideIcon } from "lucide-react";
import { useAuth } from "../../../hooks/useAuth";
import type { Role } from "../../../types/role.types";
import { motion } from "framer-motion";

export interface SidebarItemProps {
    to: string;
    icon: LucideIcon;
    label: string;
    allowedRoles?: Role[];
}

export default function SidebarItem({ to, icon: Icon, label, allowedRoles }: SidebarItemProps) {
    const location = useLocation();
    const { activeRole } = useAuth();

    const isAllowed = !allowedRoles || (activeRole && allowedRoles.includes(activeRole as Role));
    if (!isAllowed) return null;

    const isActive = to === "/" ? location.pathname === "/" : location.pathname.startsWith(to);

    return (
        <motion.div whileHover={{ x: 4 }} whileTap={{ scale: 0.98 }}>
            <Link
                to={to}
                className={`flex items-center gap-4 p-3.5 rounded-2xl text-sm font-bold transition-all border ${
                    isActive
                        ? "bg-brand/10 text-brand shadow-sm border-brand/20"
                        : "text-secondary border-transparent hover:bg-active hover:border-border hover:shadow-sm hover:text-primary"
                }`}
            >
                <Icon
                    size={22}
                    className={`shrink-0 ${isActive ? "text-brand" : "text-secondary/70"}`}
                    strokeWidth={isActive ? 2.5 : 2}
                />
                <span className="whitespace-nowrap">{label}</span>
            </Link>
        </motion.div>
    );
}