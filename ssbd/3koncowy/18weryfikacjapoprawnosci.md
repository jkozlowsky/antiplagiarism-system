## 18 Weryfikacja poprawnosci danych 2 pkt

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

### 18.5 Walidacja zewnętrzna (względem stanu i więzów bazy danych)

```java
@Entity
@Table(name = "account", indexes = {
        @Index(name = "idx_account_language_version_id", columnList = "language_version_id"),
        @Index(name = "idx_account_created_by", columnList = "created_by"),
        @Index(name = "idx_account_modified_by", columnList = "modified_by")
})
@Getter
@Setter
public class Account extends AbstractEntity {

    public static final String UNIQUE_LOGIN_INDEX_NAME = "account_login_key";
    public static final String UNIQUE_EMAIL_INDEX_NAME = "account_email_key";

    @Column(name = "login", nullable = false, unique = true, updatable = false, length = AccountConsts.LOGIN_LENGTH)
    private String login;

    @Column(name = "password_hash", nullable = false, length = AccountConsts.PASSWORD_HASH_LENGTH)
    private String passwordHash;

    @Column(name = "email", nullable = false, unique = true, length = AccountConsts.EMAIL_LENGTH)
    private String email;

    // [...]
}
```
Listing 18.5.1. Definicja więzów unikalności (UNIQUE) w encji bazodanowej warstwy modelu.

Podstawą tak pojętej walidacji zewnętrznej jest narzucenie ograniczeń na poziomie samej bazy danych. Adnotacje `@Column(unique = true)` nad polami login oraz email gwarantują, że relacyjna baza danych na poziomie silnika odrzuci próbę zapisu zduplikowanych wartości, zapewniając bezwzględną spójność danych.

```java
@Transactional(propagation = Propagation.REQUIRES_NEW, transactionManager = "mokTransactionManager")
@MethodCallLogged
@PreAuthorize("permitAll()")
public void registerAccount(Account account) {
    account.setPasswordHash(passwordEncoder.encode(account.getPasswordHash()));
    account.getPasswordHistoryList().add(new PasswordHistory(account, account.getPasswordHash()));

    account.setIsVerified(false);
    account.setIsBlocked(false);
    account.setForcePasswordChange(true);
    account.setActivationToken(UUID.randomUUID().toString());
    account.setCreationDate(LocalDateTime.now());
    account.setSecondEmailSent(false);
    account.setFailedAttempts(0);

    LanguageDict defaultLang = languageDictRepository.findByLanguage("pl").orElseThrow(LanguageDictNotFoundException::new);
    account.setLanguageVersion(defaultLang);

    AccessLevelDict studentDict = accessLevelDictRepository.findByAccessLevelName(AccountConsts.ROLE_STUDENT).orElseThrow(AccessLevelDictNotFoundException::new);

    Student studentRole = new Student();
    studentRole.setAccessLevelDict(studentDict);
    studentRole.setAccount(account);
    studentRole.setActive(true);

    account.getAccessLevelList().add(studentRole);

    accountRepository.saveAndFlush(account);
    postCommitTransactionManager.register(() -> emailService.sendActivationEmail(account.getEmail(), account.getActivationToken(), defaultLang.getLanguage()));
}
```
Listing 18.5.2. Pasywna walidacja zewnętrzna (przechwytywanie naruszenia więzów) w warstwie logiki.

Aplikacja kompletuje obiekt konta i wymusza jego fizyczny zapis w bazie wywołaniem metody saveAndFlush(account). Jeśli wprowadzone przez użytkownika dane (login lub e-mail) naruszają zdefiniowane w encji więzy unikalności, baza odrzuca operację, a dostawca JPA zgłasza błąd (np. DataIntegrityViolationException). Aplikacja przechwytuje ten błąd na poziomie globalnego mechanizmu obsługi wyjątków, przerywa transakcję i poddaje błąd translacji na bezpieczny wyjątek biznesowy informujący użytkownika o zajętym loginie, ukrywając tym samym techniczne detale bazy danych przed warstwą prezentacji.