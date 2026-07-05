import { Outlet, useLocation } from "react-router-dom";
import { motion, AnimatePresence } from "framer-motion";

import { Footer } from "./Footer";
import Header from "./header/Header.tsx";
import Sidebar from "./sidebar/Sidebar.tsx";

export default function DashboardLayout() {
    const location = useLocation();

    return (
        <div className="min-h-screen bg-base flex flex-col transition-colors duration-300">
            <Header />

            <div className="flex flex-1 relative max-w-[1600px] w-full mx-auto">

                <Sidebar />

                <main className="flex-1 flex flex-col min-w-0 bg-base">
                    <AnimatePresence mode="wait">
                        <motion.div
                            key={location.pathname}
                            initial={{ opacity: 0, y: 15 }}
                            animate={{ opacity: 1, y: 0 }}
                            exit={{ opacity: 0, y: -15 }}
                            transition={{ duration: 0.3, ease: "easeOut" }}
                            className="flex-1 w-full"
                        >
                            <Outlet />
                        </motion.div>
                    </AnimatePresence>

                    <Footer />
                </main>
            </div>
        </div>
    );
}