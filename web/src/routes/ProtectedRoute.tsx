import { Navigate, Outlet, useLocation } from 'react-router-dom';
import { PATHS } from './paths.ts';
import { useAuth } from "../hooks/useAuth";
import {type Role, RoleEnum} from "../types/role.types.ts";
import {toast} from "react-toastify";
import {useTranslation} from "react-i18next";

interface HandleProtectionProperties {
    allowedRoles?: Role[];
}

const ProtectedRoute = ({ allowedRoles }: HandleProtectionProperties) => {
    const { isAuthenticated, activeRole } = useAuth();
    const location = useLocation();
    const {t} = useTranslation();

    if (!isAuthenticated) {
        return <Navigate to={PATHS.LOGIN} replace />;
    }

    if (activeRole === RoleEnum.FORCE_PASSWORD_CHANGE) {
        if (location.pathname !== PATHS.FORCE_PASSWORD_CHANGE) {
            return <Navigate to={PATHS.FORCE_PASSWORD_CHANGE} replace />;
        }
        return <Outlet />;
    }

    const hasPermission = !allowedRoles || allowedRoles.includes(activeRole as Role);


    if (!hasPermission) {
        toast.warn(t("common.accessDenied"), {
            toastId: "common.accessDenied"
        });
        if (activeRole === RoleEnum.ADMINISTRATOR) {
            return <Navigate to={PATHS.USER_LIST} replace />;
        } else if (activeRole === RoleEnum.STUDENT) {
            return <Navigate to={PATHS.STUDENT_SUBJECT_LIST} replace />;
        } else if (activeRole === RoleEnum.TEACHER){
            return <Navigate to={PATHS.TEACHER_SUBJECT_LIST} replace/>
        }
    }

    return <Outlet />;
};

export default ProtectedRoute;