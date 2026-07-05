import {RouterProvider} from "react-router-dom";
import {router} from "./routes";
import {AuthProvider} from "./providers/AuthProvider.tsx";
import { ThemeProvider } from "./providers/ThemeProvider.tsx";
import {ToastContainer} from "react-toastify";

function App() {
    return (
        <>
            <ThemeProvider>
                <AuthProvider>
                    <RouterProvider router={router}/>
                    <ToastContainer
                        position="bottom-right"
                        autoClose={false}
                        closeOnClick={true}
                        draggable={true}
                        theme="colored"
                    />
                </AuthProvider>
            </ThemeProvider>
        </>
    )
}

export default App