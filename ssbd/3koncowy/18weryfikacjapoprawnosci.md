## 18 Weryfikacja poprawnosci danych 2 pkt

[//]: # (Omówić działanie użytych mechanizmów weryfikacji poprawności wprowadzonych danych na wybranych przykładach (jeżeli dany sposób walidacji jest zastosowany w projekcie)
[//]: # (* walidacja przez specjalizowaną bibliotekę (BeanValidation itp.)
[//]: # (* walidacja implementowana przez metodę/funkcję warstwy prezentacji)
[//]: # (* walidacja implementowana przez metodę warstwy logiki, o ile takowa występuje w projekcie,)
[//]: # (* walidacja zgodności wprowadzonych haseł, jesli zastosowano powtórzenie hasła w celu wyeliminowania pomyłek przy ustawianiu hasła użytkownika)
[//]: # (Jeżeli dana forma walidacji jest zrealizowana zarówno w warstwie prezentacji, jak i w warstwie logiki, należy przytoczyć przykłady z obu warstw.)
[//]: # (Oprócz wyżej wymienionych przykładów walidacji wewnętrznej należy zaprezentować przykład kontroli poprawności danych dla walidacji zewnętrznej (spełniania warunków logiki biznesowej)

### 18.1 Walidacja przez specjalizowaną bibliotekę (Jakarta Bean Validation, yup)

Aplikacja wykorzystuje podejście walidacji deklaratywnej po stronie serwera oraz klienta. Pozwala to na zdefiniowanie jednolitych reguł walidacji dla obiektów transferu danych (DTO).

```java
package pl.lodz.p.it.ssbd2026.ssbd01.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.ToString;
import pl.lodz.p.it.ssbd2026.ssbd01.dto.vgroups.OnCreate;

@Data
public class RegisterAccountDTO {
    @NotBlank(groups = OnCreate.class)
    @Size(min = 3, max = 32, groups = OnCreate.class)
    @Pattern(regexp = "^[\\p{L}][\\p{L}\\s\\-']+$", groups = OnCreate.class, message = "{validation.name.pattern}")
    private String name;

    @NotBlank(groups = OnCreate.class)
    @Size(min = 3, max = 32, groups = OnCreate.class)
    @Pattern(regexp = "^[\\p{L}][\\p{L}\\s\\-']+$", groups = OnCreate.class, message = "{validation.surname.pattern}")
    private String surname;

    @NotBlank(groups = OnCreate.class)
    @Email(groups = OnCreate.class)
    private String email;

    @NotBlank(groups = OnCreate.class)
    @Size(min = 3, max = 32, groups = OnCreate.class)
    @Pattern(regexp = "^[a-zA-Z0-9]+$", groups = OnCreate.class, message = "{validation.login.pattern}")
    private String login;

    @ToString.Exclude
    @NotBlank(groups = OnCreate.class)
    @Size(min = 12, max = 64, groups = OnCreate.class)
    @Pattern(
            regexp = "^(?=.*[A-Z])(?=.*[!@#$%^&*(),.?\":{}|<>]).*$",
            groups = OnCreate.class,
            message = "{validation.password.complex}"
    )
    private String password;
}
```
Listing 18.1.1 Przykładowy obiekt DTO (RegisterAccountDTO) z adnotacjami Jakarta Bean Validation.

W warstwie logiki zastosowano specyfikację Jakarta Bean Validation. Adnotacje takie jak @NotBlank, @Size, @Email oraz @Pattern określają reguły, które pole musi spełnić przed przetworzeniem. Użycie mechanizmu grup (groups = OnCreate.class) pozwala na kontekstowe aktywowanie reguł wyłącznie podczas procesu rejestracji konta.

```typescript jsx
import * as yup from "yup";

export const registerSchema = yup.object({
    name: yup.string()
        .required("validation.required")
        .min(3, "validation.user.nameMin")
        .max(32, "validation.user.nameMax")
        .matches(/^[\p{L}][\p{L}\s\-']+$/u, "validation.user.nameFormat"),

    surname: yup.string()
        .required("validation.required")
        .min(3, "validation.user.surnameMin")
        .max(32, "validation.user.surnameMax")
        .matches(/^[\p{L}][\p{L}\s\-']+$/u, "validation.user.surnameFormat"),

    login: yup.string()
        .required("validation.required")
        .min(3, "validation.user.loginMin")
        .max(32, "validation.user.loginMax")
        .matches(/^[a-zA-Z0-9]+$/, "validation.user.loginFormat"),

    email: yup.string()
        .required("validation.required")
        .email("validation.email.invalid"),

    password: yup.string()
        .required("validation.required")
        .min(12, "validation.password.minLength")
        .max(64, "validation.password.maxLength")
        .matches(/^(?=.*[A-Z])(?=.*[!@#$%^&*(),.?":{}|<>]).*$/, "validation.password.specialChar"),

    confirmPassword: yup.string()
        .required("validation.password.confirmRequired")
        .oneOf([yup.ref('password')], "validation.password.mismatch"),

}).required();

export type RegisterFormData = yup.InferType<typeof registerSchema>;
```
Listing 18.1.2 Schemat walidacji Yup w warstwie prezentacji (registerSchema.ts).

W warstwie prezentacji tożsame reguły odwzorowano przy użyciu biblioteki Yup. Schemat registerSchema weryfikuje stan formularza w czasie rzeczywistym przed wysłaniem żądania HTTP. Zapewnia to natychmiastową informację zwrotną dla użytkownika, redukuje zbędny ruch sieciowy i odciąża serwer z obsługi oczywiście błędnych żądań.

![64/67](3koncowy/img/rozdzial18walidacja.png)
Rys. 18.1.1 Przykład weryfikacji danych przez komponent warstwy prezentacji (widok rejestracji).

### 18.2 Walidacja implementowana przez metodę/funkcję warstwy prezentacji

W przypadkach, gdzie walidacja deklaratywna jest niewystarczająca lub wymagane jest szybkie, proceduralne sprawdzenie stanu komponentu przed uruchomieniem logiki potwierdzającej, stosuje się walidację imperatywną bezpośrednio wewnątrz metod warstwy prezentacji.

```typescript jsx
[...]

    const handlePreSubmit = () => {
        if (!name.trim()) {
            setError(t('subjectEdit.errorEmptyName'));
            return;
        }
        if (name.trim().length < 3) {
            setError(t('subjectEdit.errorShortName'));
            return;
        }
        if (description.trim() && description.trim().length > 500) {
            setError(t('subjectEdit.errorLongDesc'));
            return;
        }

        setError(null);
        setShowConfirmEdit(true);
    };

[...]
```
Listing 18.2.1 Walidacja proceduralna w funkcji obsługi zdarzenia komponentu (EditSubjectModal) w warstwie prezentacji.

Funkcja handlePreSubmit realizuje walidację bezpośrednio w metodzie komponentu UI. Przed dopuszczeniem użytkownika do kroku potwierdzenia zmian (setShowConfirmEdit(true)), ciągi znaków są oczyszczane z białych znaków (trim()), a następnie sprawdzane pod kątem minimalnej i maksymalnej długości. W przypadku niespełnienia kryteriów, funkcja przerywa działanie i ustawia stan błędu (setError), blokując przejście do kolejnego kroku procedury zapisu.

### 18.3 Walidacja implementowana przez metodę warstwy logiki

Walidacja w warstwie logiki biznesowej może dotyczyć integralności stanu i spójności danych wielodostępnych. Przykładem takiej kontroli jest weryfikacja sumy kontrolnej wersji obiektu (blokada optymistyczna).

```java
    @MethodCallLogged
    @PreAuthorize("hasAuthority(T(pl.lodz.p.it.ssbd2026.ssbd01.config.consts.AccountConsts).ROLE_ADMIN) or #login == authentication.name")
    @Transactional(propagation = Propagation.REQUIRES_NEW, transactionManager = "mokTransactionManager")
    public AccountWithAccessLevelsDTO updateAccountByLogin(String login, String versionHash, Account source) {
        Account target = accountRepository.findByLogin(login).orElseThrow(AccountNotFoundException::new);
        mokRefreshRepository.refresh(target);
        if (!versionHash.equals(versionHashService.hashObjectVersion(target.getId(), target.getVersion()))) {
            throw new ConcurrentUpdateException();
        }

        accountMapper.updateExistingAccount(source, target);
        Account savedAccount = accountRepository.saveAndFlush(target);
        return accountMapper.toAccountWithAccessLevelsDTO(savedAccount);
    }
```
Listing 18.3.1 Weryfikacja sumy kontrolnej wersji w warstwie logiki biznesowej.

Metoda warstwy logiki sprawdza, czy modyfikowany przez użytkownika obiekt nie został zmieniony w bazie danych przez inną transakcję w czasie, gdy użytkownik wyświetlał formularz edycji. Przekazany parametr versionHash jest porównywany z aktualnym skrótem wersji encji pobranej z bazy danych. Niezgodność skutkuje przerwaniem transakcji i rzuceniem wyjątku ConcurrentUpdateException.

```typescript jsx
[...]

    updateUserDetails: async (id: string, data: UpdateAccountDTO, version_hash: string) => {
        const response = await axiosInstance.patch(`/account/${id}`,
            data,
            {
                headers: {
                    'If-Match': version_hash
                }
            });
        return response.data;
    },

[...]
```
Listing 18.3.2 Przekazywanie tokenu wersji (ETag/If-Match) z warstwy prezentacji.

Warstwa prezentacji wspiera ten mechanizm poprzez pobranie aktualnego hasha wersji z obiektu danych, przechowywanie go w stanie i wstrzyknięcie do nagłówka żądania (If-Match) podczas wysyłania żądania aktualizacji danych.

### 18.4 Walidacja zgodności wprowadzonych haseł

W celu wyeliminowania pomyłek użytkownika podczas ustawiania bądź zmiany hasła stosuje się mechanizm powtórzenia hasła, który weryfikowany jest w warstwie prezentacji, natomiast warstwa logiki dba o poprawność autentyczności żądania.

```typescript jsx
[...]

const passwordChangeSchema = yup.object({
    oldPassword: yup.string()
        .required("validation.required"),
    newPassword: yup.string()
        .required("validation.required")
        .min(12, "validation.password.minLength")
        .matches(/[A-Z]/, "validation.password.uppercase")
        .matches(/[a-z]/, "validation.password.lowercase")
        .matches(/[0-9]/, "validation.password.number"),
    confirmPassword: yup.string()
        .required("validation.password.confirmRequired")
        .oneOf([yup.ref('newPassword')], "validation.password.mismatch")
}).required();

[...]
```
Listing 18.4.1 Definicja reguły współzależności pól hasła w schemacie Yup.

Walidacja ta realizowana jest w warstwie prezentacji przy użyciu metody `.oneOf([yup.ref('newPassword')])`. Konstrukcja ta tworzy dynamiczną referencję do wartości pola newPassword i wymusza, aby wartość pola confirmPassword była z nią identyczna. Jeśli użytkownik popełni błąd podczas powtórnego wpisywania hasła, mechanizm automatycznie generuje błąd o kodzie klucza tłumaczenia validation.password.mismatch. Ponieważ powtórzone hasło służy wyłącznie celom interfejsu użytkownika, nie jest ono przesyłane do warstwy logiki, a serwer otrzymuje w DTO wyłącznie jedno, zweryfikowane pod kątem złożoności nowe hasło.

```java
@MethodCallLogged
    @Retryable(retryFor = {ConcurrentUpdateException.class}, backoff = @Backoff(delay = 1000))
    @PreAuthorize("isAuthenticated()")
    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = false, transactionManager = "mokTransactionManager")
    public void changeOwnPassword(String login, String oldPassword, String newPassword, String ifMatch) {

        Account account = accountRepository.findByLogin(login)
                .orElseThrow(AccountNotFoundException::new);
        mokRefreshRepository.refresh(account);
        if (!ifMatch.equals(versionHashService.hashObjectVersion(account.getId(), account.getVersion()))) {
            throw new ConcurrentUpdateException();
        }

        if (!passwordEncoder.matches(oldPassword, account.getPasswordHash())) {
            throw new InvalidOldPasswordException();
        }

        validateAndSetNewPassword(account, newPassword);
        account.setForcePasswordChange(false);
        accountRepository.saveAndFlush(account);
    }
```
Listing 18.4.2 Walidacja poprawności procesu zmiany hasła w warstwie logiki.

Warstwa logiki uzupełnia ten proces poprzez weryfikację, czy przekazane hasło dotychczasowe (oldPassword) zgadza się ze skrótem zapisanym w bazie danych przy użyciu komponentu passwordEncoder.matches(). Dopiero pomyślny wynik obu tych kroków pozwala na zmianę danych.

### 18.5 Walidacja zewnętrzna (spełniania warunki logiki biznesowej)

Walidacja zewnętrzna polega na kontroli poprawności żądania w kontekście aktualnego stanu całego systemu, ograniczeń zasobów oraz reguł biznesowych przedsiębiorstwa, których nie da się zweryfikować badając wyłącznie strukturę i format samego obiektu żądania DTO.

```java
@Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = false, transactionManager = "mppTransactionManager")
    @PreAuthorize("hasAuthority(T(pl.lodz.p.it.ssbd2026.ssbd01.config.consts.AccountConsts).ROLE_STUDENT)")
    public StudentReportDetailsDTO generateStudentReport(UUID repositoryId, String studentTag, String taskTag, String login) {
        Repository repository = giteaRepositoryRepository.findById(repositoryId)
                .orElseThrow(RepositoryNotFoundException::new);

        boolean isStudentInThisRepository = repository.getStudentList().stream()
                .anyMatch(s -> s.getAccount().getLogin().equals(login));
        if (!isStudentInThisRepository) {
            throw new SubjectAccessDeniedException();
        }

        Subject subject = repository.getSubject();
        RulePreset preset = subject.getRulePreset();

        if (preset == null || "NOTHING".equals(preset.getRaportLevel().getName())) {
            throw new SubjectAccessDeniedException();
        }

        int usedTickets = repository.getTicketCount();

        if (usedTickets >= preset.getStudentTicketCount()) {
            throw new NoTicketsLeftException();
        }

        repository.setTicketCount(usedTickets + 1);
        giteaRepositoryRepository.saveAndFlush(repository);
        
        [...]

        return getStudentReportDetails(report.getId(), login);
    }
```
Listing 18.5.1 Walidacja warunków logiki biznesowej w metodzie warstwy logiki.

Przedstawiona metoda generateStudentReport realizuje trzy kroki walidacji zewnętrznej:

* System sprawdza w bazie danych, czy zalogowany student (login) faktycznie jest przypisany do żądanego repozytorium kodu.
* Sprawdzane jest, czy konfiguracja przedmiotu (RulePreset) dopuszcza generowanie raportów (czy poziom widoczności nie jest ustawiony jako NOTHING).
* Repozytorium posiada licznik wykorzystanych żądań (getTicketCount()). System weryfikuje ten stan z maksymalnym dopuszczalnym limitem skonfigurowanym dla przedmiotu. Pomimo że przesyłane parametry żądania (identyfikatory UUID) są poprawne semantycznie i składniowo, transakcja zostanie bezpiecznie przerwana poprzez zgłoszenie wyjątku biznesowego NoTicketsLeftException, jeśli limit został wyczerpany.