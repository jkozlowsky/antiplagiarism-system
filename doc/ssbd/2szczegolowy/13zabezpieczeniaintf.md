Danylo
## 13 Zabezpieczenia interfejsu użytkownika 2 pkt

Widoki są zabezpieczane w głównym pliku konfiguracyjnym rutowania `index.tsx`, gdzie określono jakie poziomy dostępu mają prawo do renderowania danych widoków. Fragment tego pliku zaprezentowano w listingu 13.1.

```TypeScript
import type {RouteObject} from "react-router-dom";  
import {createBrowserRouter} from "react-router-dom";  
import {PATHS} from "./paths.ts";  
import UserListPage from "../pages/UserList/UserListPage.tsx";  
import LoginPage from "../pages/auth/login/LoginPage.tsx";  
import Layout from "../layout.tsx";  
import {RoleEnum} from "../types/role.types.ts";  
import ProtectedRoute from "./ProtectedRoute.tsx";  
import AuthLayout from "../shared/layouts/AuthLayout.tsx";  
import RegisterPage from "../pages/auth/register/RegisterPage.tsx";  
import ActivatePage from "../pages/auth/activate/ActivatePage.tsx";  
import PasswordResetInitPage from "../pages/auth/password_reset/PasswordResetInitPage.tsx";  
import PasswordResetConfirmPage from "../pages/auth/password_reset/PasswordResetConfirmPage.tsx";  
  
import EmailChangeConfirmPage from "../pages/own_email_change/EmailChangeConfirmPage.tsx";  
import UserEditPage from "../pages/UserEdit/UserEditPage.tsx";  
import EmailChangeRevertPage from "../pages/own_email_change/EmailChangeRevertPage.tsx";  
import TwoFactorVerifyPage from "../pages/auth/login/TwoFactorAuthorizationPage.tsx";  
import {StudentSubjectListPage} from "../pages/student/StudentSubjectListPage.tsx";  
import {TeacherSubjectListPage} from "../pages/teacher/TeacherSubjectListPage.tsx";  
  
const routes: RouteObject[] = [  
    {  
        element: <AuthLayout/>,  
        children: [  
            {  
                index: true,  
                element: <LoginPage/>  
            },  
            {  
                path: PATHS.LOGIN,  
                element: <LoginPage/>  
            },  
[...] 
            {  
                path: PATHS.OWN_EMAIL_CHANGE_REVERT,  
                element: <EmailChangeRevertPage/>  
            }  
        ]  
    },  
	{  
        element: <ProtectedRoute allowedRoles={[RoleEnum.ADMINISTRATOR, RoleEnum.STUDENT, RoleEnum.TEACHER]}/>,  
        children: [  
            {  
                element: <Layout/>,  
                children: [  
                    {  
                        path: PATHS.PROFILE,  
                        element: <UserEditPage/>  
                    }  
                ]  
            }  
        ]  
    },  
    {  
        element:<ProtectedRoute allowedRoles={[RoleEnum.STUDENT]}/>,  
        children:[  
            {  
                element: <Layout/>,  
                children: [  
                    {  
                        path: PATHS.STUDENT_SUBJECT_LIST,  
                        element: <StudentSubjectListPage/>  
                    }  
                ]  
            }  
        ]  
    },  
    {  
        element:<ProtectedRoute allowedRoles={[RoleEnum.TEACHER]}/>,  
        children:[  
            {  
                element: <Layout/>,  
                children: [  
                    {  
                        path: PATHS.TEACHER_SUBJECT_LIST,  
                        element: <TeacherSubjectListPage/>  
                    }  
                ]  
            }  
        ]  
    },  
    {  
        element: <ProtectedRoute allowedRoles={[RoleEnum.ADMINISTRATOR]}/>,  
        children: [  
            {  
                element: <Layout/>,  
                children: [  
                    {  
                        path: PATHS.USER_LIST,  
                        element: <UserListPage/>  
                    },  
                    {  
                        path: PATHS.USER_EDIT,  
                        element: <UserEditPage/>  
                    }  
                ]  
            }  
        ]  
    }  
]  
  
export const router = createBrowserRouter(routes);
```

Listing 13.1. Fragment pliku `index.tsx`.

Za właściwą logikę przekierowania użytkownika bez poziomu dostępu (gość) do strony logowania lub w przypadku braku wystarczających uprawnień do widoku domyślnego dla poszczególnych poziomów dostępu odpowiada komponent `<ProtectedRoute/>` którego implementację zaprezentowano na listingu 13.2. Ten komponent pobiera informację o tym czy użytkownik jest uwierzytelniony oraz jaki ma aktywny poziom dostępu z hook'a `useAuth()`.

```TypeScript
import { Navigate, Outlet } from 'react-router-dom';  
import { PATHS } from './paths.ts';  
import { useAuth } from "../hooks/useAuth";  
import {type Role, RoleEnum} from "../types/role.types.ts";  
  
interface HandleProtectionProperties {  
    allowedRoles?: Role[];  
}  
  
const ProtectedRoute = ({ allowedRoles }: HandleProtectionProperties) => {  
    const { isAuthenticated, activeRole } = useAuth();  
  
    if (!isAuthenticated) {  
        return <Navigate to={PATHS.LOGIN} replace />;  
    }  
  
    const hasPermission = !allowedRoles || allowedRoles.includes(activeRole as Role);  
  
    if (!hasPermission) {  
        console.warn("Access Denied. User Role:", activeRole, "Allowed:", allowedRoles);  
  
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
```

Listing 13.2. plik komponentu `ProtectedRoute.tsx`.

Żródłem wiedzy o tożsamości oraz poziomach dostępu użytkownika jest token JWT który jest wysyłany z serwera po pomyślnym uwierzytelnieniu użytkownika i zapisywany do pamięci sesyjnej aplikacji web w przeglądarce.

W komponencie globalnego stanu autoryzacji `AuthProvider.tsx` JWT token jest dekodowany za pomocą biblioteki `jwt-decode` (listing 13.3) dla wyciągnięcia niezbędnych informacji: 
- Właściwość `sub` odpowiada za login/tożsamość;
- Właściwość `roles` określa dostępne poziomy dostępu.
Informacje zdekodowane z tokenu są następnie dostarczane do komponentu `ProtectedRoute` i innych części aplikacji za pośrednictwem hook'a `useAuth()`.

```TypeScript
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
```

Listing 13.3 Fragment pliku `AuthProvider.tsx` prezentujący dekodowanie tokenu JWT i wyciągnięcie poziomów dostępu oraz loginu użytkownika.
