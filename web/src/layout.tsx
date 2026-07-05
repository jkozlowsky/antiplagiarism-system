import { Outlet } from "react-router-dom";
import { useState } from "react";
import Header from "./shared/components/header/Header.tsx";
import { BreadcrumbContext } from "./contexts/BreadcrumbContext.tsx";
import Sidebar from "./shared/components/sidebar/Sidebar.tsx";
import {Footer} from "./shared/components/Footer.tsx";
import {SessionExpirationModal} from "./shared/components/modals/SessionExpirationModal.tsx";

const Layout = () => {
    const [dynamicBreadcrumb, setDynamicBreadcrumb] = useState<string | null>(null);

    return (
        <BreadcrumbContext.Provider value={{ dynamicBreadcrumb, setDynamicBreadcrumb }}>
            <main>
                <Header />
                <div className="flex flex-1 overflow-hidden">
                    <Sidebar />
                    <main className="flex-1 overflow-y-auto">
                        <Outlet />
                    </main>
                    <SessionExpirationModal />
                </div>
                <Footer/>
            </main>
        </BreadcrumbContext.Provider>
    );
};

export default Layout;