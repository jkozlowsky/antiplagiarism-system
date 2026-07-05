import { createContext, useContext } from "react";

export interface BreadcrumbContextType {
    dynamicBreadcrumb: string | null;
    setDynamicBreadcrumb: (val: string | null) => void;
}

export const BreadcrumbContext = createContext<BreadcrumbContextType>({
    dynamicBreadcrumb: null,
    setDynamicBreadcrumb: () => {},
});

export const useBreadcrumb = () => useContext(BreadcrumbContext);