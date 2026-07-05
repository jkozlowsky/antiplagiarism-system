import {type ReactNode, useMemo, useState} from "react";
import { jwtDecode } from "jwt-decode";
import { AuthContext, type JwtPayload } from "../hooks/useAuth.ts";
import {userService} from "../services/userService.ts";
import {authService} from "../services/authService.ts";
import {RoleEnum} from "../types/role.types.ts";

const determineActiveRole = (roles: string[]): string | null => {
    if (!roles || roles.length === 0) return null;

    const storedRole = sessionStorage.getItem("active_role");
    if (storedRole && roles.includes(storedRole)) {
        return storedRole;
    }

    const defaultRole = roles.includes(RoleEnum.ADMINISTRATOR) ? RoleEnum.ADMINISTRATOR : roles[0];
    sessionStorage.setItem("active_role", defaultRole);
    return defaultRole;
};

export const AuthProvider = ({ children }: { children: ReactNode }) => {
    const [token, setToken] = useState<string | null>(() => sessionStorage.getItem("access_token"));
    const [refreshToken, setRefreshToken] = useState<string | null>(() => sessionStorage.getItem("refresh_token"));

    const { availableRoles, userLogin } = useMemo<{ availableRoles: string[], userLogin: string | null }>(() => {
        if (token && token !== "undefined" && token !== "null") {
            try {
                const decoded = jwtDecode<JwtPayload>(token);
                return {
                    availableRoles: decoded.roles || [],
                    userLogin: decoded.sub
                };
            } catch (e) {
                console.error("JWT Decode Error:", e);
                return { availableRoles: [], userLogin: null };
            }
        }
        return { availableRoles: [], userLogin: null };
    }, [token]);

    const [activeRole, setActiveRole] = useState<string | null>(() => determineActiveRole(availableRoles));

    const changeActiveRole = (role: string) => {
        if (availableRoles.includes(role)) {
            setActiveRole(role);
            sessionStorage.setItem("active_role", role);
            userService.updateActiveRole(role);
        } else {
            console.warn(`Attempted to set unauthorized role: ${role}`);
        }
    };

    const setTokens = (accessToken: string | null, rToken: string | null) => {
        if (accessToken) {
            sessionStorage.setItem('access_token', accessToken);
            try {
                const decoded = jwtDecode<JwtPayload>(accessToken);
                setActiveRole(determineActiveRole(decoded.roles || []));
            } catch {
                setActiveRole(null);
            }
        } else {
            sessionStorage.removeItem('access_token');
            setActiveRole(null);
        }
        setToken(accessToken);
        if (rToken) {
            sessionStorage.setItem('refresh_token', rToken);
        } else {
            sessionStorage.removeItem('refresh_token');
        }
        setRefreshToken(rToken);
    };

    const logout = () => {
        sessionStorage.removeItem('access_token');
        sessionStorage.removeItem('refresh_token');
        sessionStorage.removeItem('active_role');
        setToken(null);
        setRefreshToken(null);
        setActiveRole(null);
    };

    const extendSession = async () => {
        if (refreshToken) {
            try {
                const data = await authService.refreshSession(refreshToken);
                setTokens(data.token, data.refreshToken);
            } catch (error) {
                console.error("Failed to extend session", error);
                logout();
            }
        }
    };


    const contextValue = useMemo(() => ({
        token,
        refreshToken,
        setTokens,
        activeRole,
        availableRoles,
        changeActiveRole,
        userLogin,
        isAuthenticated: !!token,
        logout,
        extendSession
    }), [token, refreshToken, activeRole, availableRoles, userLogin]);

    return <AuthContext.Provider value={contextValue}>{children}</AuthContext.Provider>;
};


export default AuthProvider;