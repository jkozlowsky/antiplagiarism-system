import { createContext, useContext } from "react";

export interface JwtPayload {
    sub: string;
    roles: string[];
    exp: number;
}

export interface AuthContextType {
    token: string | null;
    refreshToken: string | null;
    setTokens: (accessToken: string | null, refreshToken: string | null) => void;
    activeRole: string | null;
    availableRoles: string[];
    changeActiveRole: (role: string) => void;
    userLogin: string | null;
    isAuthenticated: boolean;
    logout: () => void;
    extendSession: () => Promise<void>;
}

export const AuthContext = createContext<AuthContextType | null>(null);

export const useAuth = () => {
    const context = useContext(AuthContext);
    if (!context) throw new Error("useAuth must be used within AuthProvider");
    return context;
};