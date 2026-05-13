## 12 Interfejs użytkownika 3 pkt

### Szablon GUI

Widok GUI jest utworzony za pomocą TypeScript biblioteki React z dodatkowym wykorzystaniem bibliotek:
- Tailwind: dla ostylowania elementów.
- i18next: dla internacjonalizacji widoku.
Szablon strony po pomyślnym uwierzytelnieniu użytkownika znajduje się w pliku `layout.tsx`, zaprezentowany poniżej w listingu 12.1.

```typeScript
import { Outlet } from "react-router-dom";  
import { useState } from "react";  
import Header from "./shared/components/header/Header.tsx";  
import { BreadcrumbContext } from "./contexts/BreadcrumbContext.tsx";  
import Sidebar from "./shared/components/sidebar/Sidebar.tsx";  
import {Footer} from "./shared/components/Footer.tsx";  
  
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
                </div>
                <Footer/>            
            </main>
        </BreadcrumbContext.Provider>    
        );  
};  
  
export default Layout;
```
Listing 12.1. Kod głównego szablonu aplikacji web.

Szablon widoku zawiera następujące elementy:
- Nagłówek w postaci niestandardowego komponentu `<Header/>` zawierający nazwę aplikacji, która jednocześnie jest przyciskiem do przejścia na widok ekranu głównego danego poziomu dostępu użytkownika.
- Menu boczne nawigacyjne w postaci niestandardowego komponentu `<Sidebar/>` który zawiera przyciski do przejścia na poszczególne widoki dla danego poziomu dostępu użytkownika.
- Sekcja główna w postaci komponentu `<main/>` z zagnieżdżonym elementem z biblioteki React `<Outlet/>` do którego podstawiane są wybrane widoki użytkownika.
- Stópka w postaci niestandardowego komponentu `<Footer/>` która zawiera informację o zespole który utworzył oprogramowanie.

### Domyślne (główne) strony dla poszczególnych ról użytkowników.

Po uwierzytelnieniu dla użytkownika będzie dostępny jeden z następujących widoków w zależności od jego aktywnego poziomu dostępu:
- Administrator - lista użytkowników (rys. 12.1);
- Prowadzący - lista przedmiotów (rys. 12.2);
- Student - lista przedmiotów (rys. 12.3).

Także w zależności od aktywnego poziomu dostępu poszczególne role widzą różne kolory linii oddzielającej nagłówek od głównej zawartości strony dla szybkiej identyfikacji wybranego poziomu dostępu:
- Administrator - niebieski (rys. 12.1);
- Prowadzący - czerwony (rys. 12.2);
- Student - zielony (rys. 12.3).

![admin](2szczegolowy/img/12-1.png)

Rysunek 12.1. Strona główna użytkownika z poziomem dostępu administrator.

![prowadzący](2szczegolowy/img/12-2.png)

Rysunek 12.2. Strona główna użytkownika z poziomem dostępu prowadzący.

![student](2szczegolowy/img/12-3.png)

Rysunek 12.3. Strona główna użytkownika z poziomem dostępu student.

### Mechanizm dobóru widoku głównego dla konta z kilkoma poziomami dostępu.

Po uwierzytelnieniu (poprzez zwykłę dwufazowe jak i za pomocą usługi google) aplikacja dekoduje otrzymany token JWT w celu identyfikacji ról użytkownika. Następnie, wybiera odpowiedni poziom dostępu jako aktywny, priorytetyzując poziom dostępu "ADMIN" jeżeli jest dostępny (listing 12.4), po czym za pomocą funkcji pomocniczej `getDashboardPath`, zaprezentowaną na listingu 12.5 wyznacza odpowiednią ścieżkę przekierowania właściwą dla aktywnego poziomu dostępu.

```TypeScript
if (response?.token) {  
    setTokens(response.token, response.refreshToken);  
    const decoded = jwtDecode<JwtPayload>(response.token);  
    const newRole = decoded.roles?.includes("ADMIN") ? "ADMIN" : decoded.roles?.[0];  
    const targetPath = location.state?.from?.pathname || getDashboardPath(newRole);  
    navigate(targetPath, {replace: true});  
}
```

Listing 12.4. Fragment funkcji `handleGoogleLogin` odpowiadający za przekierowanie użytkownika do odpowiedniego widoku zgodnie z jego poziomem dostępu.

```TypeScript
export const getDashboardPath = (role: string | null): string => {  
    switch (role?.toUpperCase()) {  
        case 'ADMIN':  
            return PATHS.USER_LIST;  
        case 'TEACHER':  
            return PATHS.TEACHER_SUBJECT_LIST;  
        case 'STUDENT':  
            return PATHS.STUDENT_SUBJECT_LIST;  
        default:  
            return PATHS.PROFILE;  
    }  
};
```

Listing 12.5 funkcja pomocnicza `getDashboardPath`.