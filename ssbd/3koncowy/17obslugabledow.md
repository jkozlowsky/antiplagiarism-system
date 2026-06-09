
## 17 Obsługa błędów 2 pkt

### Zunifikowana obsługa odpowiedzi różnych od 2xx 

W aplikacji internetowej wprowadzono zunifikowaną obsługę błędów wysyłanych w odpowiedzi z aplikacji serwerowej. Aplikacja serwerowa, kiedy wysyła odpowiedź z kodem HTTP różnym od 2xx, w treści zostawia komunikat o przyczynie błędu, który odczytuje aplikacja internetowa i wyświetla dla użytkownika w "chmurce" w prawym dolnym rogu widoku.

```typescript
axiosInstance.interceptors.response.use(  
    (response) => {  
[...]
    },  
    async (error) => {  
        const originalRequest = error.config;  
[...]
        if (error.response) {  
            const status = error.response.status;  
            if (status !== 401 && status !== 400) {  
                const backendMessage = error.response.data;  
                let toastMessage = backendMessage;  
                if (!toastMessage) {  
                    let errorMessageKey = 'errors.generic';  
                    switch (status) {  
                        case 403:  
                            errorMessageKey = 'errors.forbidden';  
                            break;  
                        case 404:  
                            errorMessageKey = 'errors.notFound';  
                            break;  
                        case 409:  
                            errorMessageKey = 'errors.conflict';  
                            break;  
                        case 412:  
                            errorMessageKey = 'errors.preconditionFailed';  
                            break;  
                        case 500:  
                        case 502:  
                        case 503:  
                        case 504:  
                            errorMessageKey = 'errors.serverError';  
                            break;  
                    }  
                    toastMessage = i18n.t(errorMessageKey);  
                }  
  
                toast.error(toastMessage, {  
                    toastId: toastMessage  
                });  
            }  
        } else if (error.request) {  
            toast.error(i18n.t('errors.networkDown'), {  
                toastId: 'global-network-error'  
            });  
        }  
        return Promise.reject(error);  
    }  
);
```

Listing 17.1. Fragment obsługi odpowiedzi http w aplikacji internetowej.

### Brak uwierzytelnienia lub brak autoryzacji do wykonania akcji

Aplikacja zabezpiecza dostęp po stronie aplikacji internetowej oraz obsługując błędy z warstwy aplikacji serwerowej.

W aplikacji internetowej w komponencie `ProtectedRoute` weryfikowana jest rola użytkownika. Jeśli użytkownik nie ma uprawnień do danego widoku, aplikacja wyświetla żółte powiadomienie ostrzegawcze `toast.warn` z tłumaczonym komunikatem `"common.accessDenied"`. Następnie użytkownik jest automatycznie przekierowywany na stronę domyślną odpowiadającą jego roli.

```typescript

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
```

Listing 17.1. Komponent ProtectedRoute, który przekierowuje nieuwierzytelnionego użytkownika na stronę logowania oraz nie autoryzowanego użytkownika na odpowiednią dla jego roli stronę i wysyła odpowiedni komunikat.

![17.1](3koncowy/img/17.1.png)

Rys. 17.1. Przykład skutku próby przejścia na stronę do której użytkownik z poziomem dostępu "prowadzący" nie ma dostępu. 

Jeśli sesja wygaśnie, główny interceptor przechwytuje błąd `401`. Aplikacja spróbuje automatycznie wysłać żądanie odświeżenia tokenu do `/auth/refresh`. Jeśli to się nie uda, czyści sesję w przeglądarce i przenosi użytkownika do okna logowania `/login`.

![17.2](3koncowy/img/17.2.png)

Rys. 17.2. Przykład skutku wygaśnięcia sesji uzytkownika. 

W przypadku próby wykonania żądania bez odpowiedniej autoryzacji, interceptor wyświetli komunikat z odpowiedzi, albo domyślny jeżeli odpowiedż była pusta.

![17.3](3koncowy/img/17.3.png)

Rys. 17.3. Przykład skutku próby edycji ha do przedmiotu do którego dany prowadzący nie ma dostępu.

### Niepoprawna ścieżka zasobu

W przypadku jeśli użytkownik wprowadzi w przeglądarce adres, który nie istnieje, to zostanie pokazany komponent z komunikatem o braku takiej strony.

![17.4](3koncowy/img/17.4.png)

Rys. 17.4. Strona nieistniejącego zasobu.

Jeżeli użytkownik dokona próby przejścia na zasób którego nie istnieje i będzie wysłane żądanie, a w odpowiedzi otrzyma kod HTTP 404, to zostanie powiadomiony za pomocą zunifikowanej obsługi błędów.

![17.5](3koncowy/img/17.5.png)

Rys. 17.5. Przykład próby dostępu do nieistniejącego zasobu.

### Niepoprawne formaty danych przesyłanych do warstwy logiki

Jeżeli użytkownik spróbuje wpisać niepoprawne dane do formularzy, na przykład, rejestracji, to zostanie o tym powiadomiony od razu po stronie aplikacji internetowej.

![17.6](3koncowy/img/17.6.png)

Rys. 17.6. Przykład próby podania niepoprawnego adresu email podczas rejestracji.

Jeżeli z jakiegoś powodu (najcześciej z powodu błędu) wymagania walidacji w aplikacji internetowej są mniej restrykcyjne niż te w warstwie logiki, to użytkownik również zostanie o tym powiadomiony.

![17.7](3koncowy/img/17.7.png)

Rys. 17.7. Przykład próby podania niepoprawnego adresu loginu podczas rejestracji.

### Realizacja akcji niemożliwej ze względu na niespełnienia warunków logiki biznesowej

W przypadku jeżeli użytkownik spróbuje wykonać akcję niemożliwą ze względu na niespełnienie warunków logiki biznesowej - to zostanie o tym powiadomiony za pomocą zunifikowanej obsługi błędów.

![17.8](3koncowy/img/17.8.png)

Rys. 17.8. Przykład próby Naruszenia warunków logiki biznesowej.

### Nie przewidziany uprzednio błąd

W przypadku jeżeli użytkownik wykona akcje które doprowadzą do nie przewidzianego uprzednio błędu, to zostanie o tym powiadomiony za pomocą zunifikowanej obsługi błędów.

![17.9](3koncowy/img/17.9.png)

Rys. 17.9. Przykład symulacji wystąpienia nie przewidzianego uprzednio błędu.
