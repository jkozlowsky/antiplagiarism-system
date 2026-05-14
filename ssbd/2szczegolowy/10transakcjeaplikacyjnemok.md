
## 10 Identyfikacja transakcji aplikacyjnych MOK 3 pkt

W module MOK zarządzanie transakcjami realizowane jest w oparciu o strategię CMT z wykorzystaniem adnotacji `@Transactional`. 
Metody warstwy biznesowej (tzn. serwisy) rozpoczynają zawsze nowe transakcje aplikacyjne przy użyciu propagacji `REQUIRES_NEW`.

W celu uniknięcia redundancji kodu oraz zapewnienia spójności architektonicznej wszystkie komponenty dostępowe (tzn. repozytoria) 
w module MOK posiadają adnotację na poziomie klasy, która narzuca propagację `MANDATORY`dla wszystkich metod modyfikujących 
i odczytujących stan bazy danych. Metody te nie mogą samodzielnie otworzyć nowej transakcji i muszą dołączyć do transakcji 
nadrzędnej.

### MOK.1 - Rejestracja konta użytkownika.
Na powyższy przypadek użycia składają się dwie transakcje aplikacyjne. 

Pierwsza z nich jest odpowiedzialna za wstępne utworzenie profilu użytkownika poprzez nadanie mu domyślnej roli Studenta 
i zapisanie ze statusem konta niezweryfikowanego oraz przygotowanie tokenu aktywacyjnego. 

```java
    @Transactional(propagation = Propagation.REQUIRES_NEW, transactionManager = "mokTransactionManager")
    public void registerAccount(Account account) {
        account.setPasswordHash(passwordEncoder.encode(account.getPasswordHash()));

        account.setIsVerified(false);
        account.setIsBlocked(false);
        account.setActivationToken(UUID.randomUUID().toString());
        account.setCreationDate(LocalDateTime.now());
        account.setSecondEmailSent(false);
        account.setFailedAttempts(0);

        LanguageDict defaultLang = languageDictRepository.findByLanguage("pl")
                .orElseThrow(LanguageDictNotFoundException::new);
        account.setLanguageVersion(defaultLang);

        AccessLevelDict studentDict = accessLevelDictRepository.findByAccessLevelName(AccountConsts.ROLE_STUDENT)
                .orElseThrow(AccessLevelDictNotFoundException::new);

        Student studentRole = new Student();
        studentRole.setAccessLevelDict(studentDict);
        studentRole.setAccount(account);
        studentRole.setActive(true);

        account.getAccessLevelList().add(studentRole);

        accountRepository.saveAndFlush(account);
        postCommitTransactionManager.register(() ->
                emailService.sendActivationEmail(account.getEmail(), account.getActivationToken(), defaultLang.getLanguage()));
    }
```
Listing 10.1. Metoda rejestrująca użytkownika w systemie i rozpoczynająca pierwszą transakcję aplikacyjną dla przypadku MOK.1.

Transakcja jest inicjowana przez metodę `registerAccount(Account)` z `AccountService`, którą przedstawiono 
na listingu 10.1. W ramach tej transakcji wywoływana jest sekwencja metod komponentów dostępowych, które dołączają 
do niej z propagacją `MANDATORY` (metody te nie mogą samodzielnie otworzyć nowej transakcji). 

Są nimi kolejno: 
* `LanguageDictRepository.findByLanguage("pl")`, 
* `AccessLevelDictRepository.findByAccessLevelName(AccountConsts.ROLE_STUDENT)`,
* `AccountRepository.saveAndFlush(account)`, która wysyła natychmiastowo kwerendy do bazy danych i zapisuje w niej obiekt. 

Operacje te wykonywane są w granicach transakcji na następujących obiektach encji JPA: 
* **Account**, 
* **LanguageDict**, 
* **AccessLevelDict**,
* **Student** (dziedziczy po klasie **AccessLevel**).

Druga transakcja z kolei realizuje proces weryfikacji tokenu aktywującego na podstawie linku z wiadomości e-mail 
i ostatecznej aktywacji konta.

```java
    @Transactional(propagation = Propagation.REQUIRES_NEW, transactionManager = "mokTransactionManager")
    public void confirmActivation(String token) {
        Account account = accountRepository.findByActivationToken(token)
                .orElseThrow(AccountActivationException::new);

        if (account.getCreationDate().plusHours(24).isBefore(LocalDateTime.now())) {
            throw new AccountActivationException("error.activation.token.expired");
        }

        account.setIsBlocked(false);
        account.setIsVerified(true);
        account.setActivationToken(null);
        accountRepository.saveAndFlush(account);
    }
```
Listing 10.2. Metoda inicjująca drugą transakcję aplikacyjną w ramach przypadku MOK.1 oraz aktywująca konto użytkownika.

Druga transakcja w ramach MOK.1 jest inicjowana przez metodę `confirmActivation(String)` z `AccountService`, 
przedstawioną na listingu 10.2. W ramach tej transakcji wywoływane są metody komponentów dostępowych 
z propagacją `MANDATORY`:
* `AccountRepository.findByActivationToken(token)`,
* `AccountRepository.saveAndFlush(account)`.
Operacje te wykonywane są na encji JPA **Account**.

Opcjonalnie, w przypadku braku aktywacji konta przez użytkownika w ciągu wyznaczonego czasu (systemowo 24 godziny) 
lub konieczności wysłania przypomnienia (połowa wyznaczonego czasu), w systemie uruchamiana jest cykliczna 
transakcja aplikacyjna z domyślną propagacją REQUIRED, zarządzana przez komponent `AccountScheduler`.

```java
@Scheduled(fixedDelayString = "${app.scheduler.check.interval.ms}")
@Transactional(transactionManager = "mokTransactionManager")
public void processInactiveAccounts() {
    LocalDateTime now = LocalDateTime.now();
    int reminderHours = expirationHours / 2;

    accountRepository.findAllByIsVerifiedFalseAndSecondEmailSentFalseAndCreationDateBefore(now.minusHours(reminderHours))
            .forEach(acc -> {
                String lang = acc.getLanguageVersion().getLanguage();
                emailService.sendActivationReminderEmail(acc.getEmail(), acc.getActivationToken(), lang);
                acc.setSecondEmailSent(true);
            });

    accountRepository.findAllByIsVerifiedFalseAndCreationDateBefore(now.minusHours(expirationHours))
            .forEach(acc -> {
                String lang = acc.getLanguageVersion().getLanguage();
                emailService.sendAccountDeletedEmail(acc.getEmail(), lang);
                accountRepository.delete(acc);
            });
}
```
Listing 10.3. Metoda harmonogramu realizująca proces przypominania o aktywacji i usuwania niezweryfikowanych kont.

Transakcja inicjowana jest przez metodę `processInactiveAccounts()` przedstawioną na listingu 10.3, domyślnie tworzącą 
nową transakcję aplikacyjną. 
W jej ramach wywoływane są metody repozytorium z propagacją `MANDATORY`:
* `AccountRepository.findAllByIsVerifiedFalseAndSecondEmailSentFalseAndCreationDateBefore(...)`
* `AccountRepository.findAllByIsVerifiedFalseAndCreationDateBefore(...)`
* `AccountRepository.delete(acc)`

Operacje te, wykonywane są na obiektach encji JPA **Account**.
### MOK.2 - Uwierzytelnienie użytkownika.
Na powyższy przypadek użycia składają się dwie transakcje aplikacyjne inicjowane przez użytkownika oraz jedna 
transakcja inicjowana przez system, zarządzana przez harmonogram zadań.

Pierwsza transakcja aplikacyjna odpowiada za weryfikację loginu i hasła, aktualizację statystyk logowania oraz 
w przypadku poprawnej weryfikacji, wygenerowanie kodu 2FA.

```java
    @Transactional(propagation = Propagation.REQUIRES_NEW, transactionManager = "mokTransactionManager", noRollbackFor = InvalidCredentialsProvidedException.class)
    @Retryable(retryFor = ConcurrentUpdateException.class, backoff = @Backoff(delayExpression = "${app.retry.backoff}"))
    public void authenticate(String login, String password, String ipAddress) {
        Account account = accountRepository.findByLogin(login).orElseThrow(InvalidCredentialsProvidedException::new);

        if (account.getIsBlocked()) {
            throw new AccountIsBlockedException();
        }

        if (!account.getIsVerified()) {
            throw new AccountIsNotVerifiedException();
        }

        if (!passwordEncoder.matches(password, account.getPasswordHash())) {
            handleFailedLogin(account, ipAddress);
            throw new InvalidCredentialsProvidedException();
        }
        generate2FactorCode(account);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, transactionManager = "mokTransactionManager")
    public void generate2FactorCode(Account account) {
        List<OneTimeTokenEnt> activeTokens = tokenRepository
                .findByUsernameAndTokenTypeAndIsUsedIsFalse(account.getLogin(), OneTimeTokenType.CODE_2FA);
    
        if (!activeTokens.isEmpty()) {
            OneTimeTokenEnt oldToken = activeTokens.getFirst();
            if (oldToken.getExpiresAt() != null) {
                LocalDateTime createdAt = oldToken.getExpiresAt().minusMinutes(OneTimeTokenConsts.EXPIRATION_ONE_TIME_TOKEN_MINUTES);
                LocalDateTime canResend = createdAt.plusMinutes(OneTimeTokenConsts.RESEND_TIMEOUT_2FA_IN_MINUTES);
    
                if (LocalDateTime.now().isBefore(canResend)) {
                    throw new Resend2FATokenException();
                }
            }
            oldToken.setUsed(true);
            tokenRepository.save(oldToken);
        }
    
        OneTimeTokenEnt tokenEnt = new OneTimeTokenEnt();
        String twoFactorCode = tokenValueGenerator.generate2FAToken();
        postCommitTransactionManager.register(() -> emailService.send2FAuthEmail(twoFactorCode, account.getEmail(), account.getLanguageVersion().getLanguage()));
    
        tokenEnt.setTokenValue(hashToken(twoFactorCode));
        tokenEnt.setUsername(account.getLogin());
        tokenEnt.setExpiresAt(LocalDateTime.now().plusMinutes(OneTimeTokenConsts.EXPIRATION_ONE_TIME_TOKEN_MINUTES));
        tokenEnt.setTokenType(OneTimeTokenType.CODE_2FA);
        tokenEnt.setUsed(false);
    
        tokenRepository.saveAndFlush(tokenEnt);
}
```
Listing 10.4. Metoda weryfikująca poświadczenia i generująca kod weryfikujący, która rozpoczyna pierwszą transakcję dla MOK.2.

Transakcja ta jest inicjowana przez metodę `authenticate(...)` z klasy AuthService z propagacją `REQUIRES_NEW`. 
Wykorzystanie parametru noRollbackFor jest kluczowe, aby w przypadku podania błędnego hasła system mógł trwale 
zapisać w bazie zwiększony licznik `failedAttempts`. W ramach tej transakcji wywoływane są metody komponentów 
dostępowych z propagacją `MANDATORY`:
* `AccountRepository.findByLogin(login)`
* `AccountRepository.saveAndFlush(account)`

Operacje te wykonywane są na encji JPA: **Account**.

Druga transakcja aplikacyjna odpowiada za weryfikację kodu 2FA i finalizację procesu uwierzytelnienia. 
Jest ona inicjowana przez metodę `verify2FactorCode(...)` z klasy `AuthService` z propagacją `REQUIRES_NEW`.
```java
    @Transactional(propagation = Propagation.REQUIRES_NEW,
            transactionManager = "mokTransactionManager",
            noRollbackFor = {OneTimeTokenInvalidException.class, AccountNotFoundException.class})
    public AuthResponseDTO verify2FactorCode(String token, String login, String ipAddress, String langHeader) {

        String hashedToken = hashToken(token);

        Optional<OneTimeTokenEnt> tokenOpt = tokenRepository
                .findByTokenValueAndUsernameAndTokenTypeAndIsUsedIsFalseAndExpiresAtAfter(
                        hashedToken, login, OneTimeTokenType.CODE_2FA, LocalDateTime.now());

        if (tokenOpt.isEmpty()) {
            Account account = accountRepository.findByLogin(login)
                    .orElseThrow(OneTimeTokenInvalidException::new);
            handleFailedLogin(account, ipAddress);
            throw new OneTimeTokenInvalidException();
        }

        OneTimeTokenEnt tokenEntity = tokenOpt.get();
        Account account = accountRepository.findByLogin(tokenEntity.getUsername())
                .orElseThrow(AccountNotFoundException::new);

        tokenEntity.setUsed(true);
        tokenRepository.saveAndFlush(tokenEntity);

        return handleSuccessfulLogin(account, ipAddress, langHeader);
    }
```
Listing 10.5. Metoda weryfikująca poprawność podanego kodu weryfikacyjnego.

W jej ramach wywoływane są metody repozytoriów z propagacją `MANDATORY`: 
* `OneTimeTokenRepository.findByTokenValueAndUsernameAndTokenTypeAndIsUsedIsFalseAndExpiresAtAfter(...)`, 
* `AccountRepository.findByLogin(...)` 
* `AccountRepository.saveAndFlush(account)`

Operacje te przeprowadzane są na encjach JPA: **Account** oraz **OneTimeTokenEnt**.
### MOK.3 - Reset hasła.
Podobnie jak we wcześniejszych przypadkach, ten przypadek użycia składa się z dwóch niezależnych transakcji aplikacyjnych.

Pierwsza z nich odpowiada za zarejestrowanie żądania zresetowania hasła, unieważnienie ewentualnych starych 
tokenów resetujących oraz wygenerowanie i zapisanie nowego tokenu, który zostanie wysłany użytkownikowi.

```java
    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = false, transactionManager = "mokTransactionManager")
    public void initiatePasswordReset(String email) {
        Optional<Account> accountOpt = accountRepository.findByEmail(email);
        if (accountOpt.isEmpty()) {
            //Po prostu zwracamy bo użytkownik nie powinien wiedzieć czy podany mail jest w bazie.
            return;
        }

        List<OneTimeTokenEnt> potentialClashedToken = tokenRepository.findByUsernameAndTokenTypeAndIsUsedIsFalse(email, OneTimeTokenType.PASSWORD_RESET);
        potentialClashedToken.forEach(item -> item.setUsed(true));

        if (!potentialClashedToken.isEmpty()) {
            tokenRepository.saveAllAndFlush(potentialClashedToken);
        }

        OneTimeTokenEnt token = new OneTimeTokenEnt();
        String rawToken = UUID.randomUUID().toString();
        token.setTokenValue(hashToken(rawToken));
        token.setUsername(email);
        token.setExpiresAt(LocalDateTime.now().plusMinutes(OneTimeTokenConsts.EXPIRATION_TIME_MINUTES));
        token.setTokenType(OneTimeTokenType.PASSWORD_RESET);
        token.setUsed(false);
        tokenRepository.saveAndFlush(token);

        emailService.sendResetPasswordEmail(email, rawToken, accountOpt.get().getLanguageVersion().getLanguage());
    }
```
Listing 10.6. Metoda rozpoczynająca pierwszą transakcję aplikacyjną żądania resetu hasła dla przypadku MOK.3.

Transakcja jest inicjowana przez metodę `initiatePasswordReset(String)` z klasy `PasswordResetService` z propagacją `REQUIRES_NEW`.
W jej ramach wywoływane są metody repozytoriów z propagacją `MANDATORY`:
* `AccountRepository.findByEmail(...)`
* `OneTimeTokenRepository.findByUsernameAndTokenTypeAndIsUsedIsFalse(...)`
* `OneTimeTokenRepository.saveAllAndFlush(...)`
* `OneTimeTokenRepository.saveAndFlush(...)`

Operacje te działają na encjach JPA: **Account** oraz **OneTimeTokenEnt**.

Druga transakcja odpowiada za zatwierdzenie żądania poprzez weryfikację tokenu i utrwalenie nowo wprowadzonego 
skrótu hasła, a także dodanie go do historii haseł.

```java
@Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = false, transactionManager = "mokTransactionManager")
public void resolvePasswordReset(String token, String passwordHash) {
        String hashedTokenInput = hashToken(token);
        OneTimeTokenEnt tokenEntity = tokenRepository
                .findByTokenValueAndTokenTypeAndIsUsedIsFalseAndExpiresAtAfter(hashedTokenInput, OneTimeTokenType.PASSWORD_RESET, LocalDateTime.now())
                .orElseThrow(() -> new OneTimeTokenInvalidException("error.token.reset.expired"));

        tokenEntity.setUsed(true);

        Account account = accountRepository.findByEmail(tokenEntity.getUsername())
                .orElseThrow(() -> new AccountNotFoundException("Account associated with that email is not found"));

        if (passwordEncoder.matches(passwordHash, account.getPasswordHash())) {
            throw new PasswordSameAsOldException();
        }

        for (PasswordHistory history : account.getPasswordHistoryList()) {
            if (passwordEncoder.matches(passwordHash, history.getPasswordHash())) {
                throw new PasswordAlreadyUsedInPastException();
            }
        }

        String newHash = passwordEncoder.encode(passwordHash);
        account.setPasswordHash(newHash);
        account.getPasswordHistoryList().add(new PasswordHistory(account, newHash));
    }
```
Listing 10.7. Metoda weryfikująca token resetujący i ustawiająca nowe hasło.

Transakcja inicjowana jest przez metodę `resolvePasswordReset(String, String)` z propagacją `REQUIRES_NEW`.

Podlegają pod nią wywołania metod dostępowych z propagacją MANDATORY:
* `OneTimeTokenRepository.findByTokenValueAndTokenTypeAndIsUsedIsFalseAndExpiresAtAfter(...)`
* `AccountRepository.findByEmail(...)`

Operacje te działają na encjach JPA: **Account** oraz **OneTimeTokenEnt**.
### MOK.4 - Zablokuj konto użytkownika.
Na powyższy przypadek użycia składa się jedna transakcja aplikacyjna.
Jest ona inicjowana przez metodę `blockUserAccount(UUID, String)` z klasy `AccountService` z propagacją `REQUIRES_NEW`.
```java
    @MethodCallLogged
    @PreAuthorize("hasAuthority(T(pl.lodz.p.it.ssbd2026.ssbd01.config.consts.AccountConsts).ROLE_ADMIN)")
    @Retryable(retryFor = ConcurrentUpdateException.class)
    @Transactional(propagation = Propagation.REQUIRES_NEW, transactionManager = "mokTransactionManager")
    public void blockUserAccount(UUID id, String versionHash) {
        Account account = accountRepository.findById(id).orElseThrow(AccountNotFoundException::new);

        mokRefreshRepository.refresh(account);
        if (!versionHash.equals(versionHashService.hashObjectVersion(id, account.getVersion()))) {
            throw new ConcurrentUpdateException();
        }

        if (account.getIsBlocked()) {
            throw new AccountIsBlockedException();
        }

        account.setIsBlocked(true);
        accountRepository.saveAndFlush(account);
        postCommitTransactionManager.register(() ->
                emailService.sendAccountIsBlockedInfo(account.getEmail(), account.getLogin(), account.getLanguageVersion().getLanguage()));
    }
```
Listing 10.8. Metoda inicjująca transakcję aplikacyjną dla przypadku MOK.4.

W ramach tej transakcji wywoływane są metody komponentów dostępowych z propagacją `MANDATORY`:
* `AccountRepository.findById(...)`
* `MokRefreshRepository.refresh(...)` odpowiada za wymuszenie odświeżenia stanu encji z bazy danych przed weryfikacją skrótu wersji,
* `AccountRepository.saveAndFlush(...)`

Operacje te wykonywane są na encji JPA **Account**.

Przypadek ten jest jednym ze scenariuszy wymagających mechanizmu blokad optymistycznych, opisanego zbiorowo w sekcji 
poświęconej blokadzie optymistycznej poniżej.

### MOK.5 - Odblokuj konto użytkownika.
Na powyższy przypadek użycia składa się jedna transakcja aplikacyjna.

Jest ona inicjowana przez metodę `unblockUserAccount(UUID, String)` z klasy `AccountService` z propagacją `REQUIRES_NEW`.
```java
    @MethodCallLogged
    @PreAuthorize("hasAuthority(T(pl.lodz.p.it.ssbd2026.ssbd01.config.consts.AccountConsts).ROLE_ADMIN)")
    @Retryable(retryFor = ConcurrentUpdateException.class)
    @Transactional(propagation = Propagation.REQUIRES_NEW, transactionManager = "mokTransactionManager")
    public void unblockUserAccount(UUID id, String versionHash) {
        Account account = accountRepository.findById(id).orElseThrow(AccountNotFoundException::new);

        mokRefreshRepository.refresh(account);
        if (!versionHash.equals(versionHashService.hashObjectVersion(id, account.getVersion()))) {
            throw new ConcurrentUpdateException();
        }

        if (!account.getIsBlocked()) {
            throw new AccountIsNotBlockedException();
        }

        account.setIsBlocked(false);
        accountRepository.saveAndFlush(account);
        postCommitTransactionManager.register(() ->
                emailService.sendAccountIsUnblockedInfo(account.getEmail(), account.getLogin(), account.getLanguageVersion().getLanguage()));
    }
```
Listing 10.9. Metoda inicjująca transakcję aplikacyjną dla przypadku MOK.5.

W ramach tej transakcji wywoływane są metody komponentów dostępowych z propagacją `MANDATORY`:
* AccountRepository.findById(id),
* MokRefreshRepository.refresh(account),
* AccountRepository.saveAndFlush(account).

Operacje te wykonywane są na encji JPA **Account**.
### MOK.6 - Dołącz poziom dostępu do konta. 
Na powyższy przypadek użycia składa się jedna transakcja aplikacyjna.
Jest ona inicjowana przez metodę `grantAccessLevel(UUID, String, String)` z klasy `AccountService` z propagacją `REQUIRES_NEW`.
```java
    @Transactional(propagation = Propagation.REQUIRES_NEW, transactionManager = "mokTransactionManager")
    @PreAuthorize("hasAuthority(T(pl.lodz.p.it.ssbd2026.ssbd01.config.consts.AccountConsts).ROLE_ADMIN)")
    @Retryable(
            retryFor = {JpaSystemException.class, ConcurrentUpdateException.class},
            backoff = @Backoff(delayExpression = "${app.retry.backoff}"),
            maxAttemptsExpression = "${app.retry.maxattempts}"
    )
    public AccountWithAccessLevelsDTO grantAccessLevel(UUID uuid, String accessLevelName, String versionHash) {
        Account account = accountRepository.findById(uuid).orElseThrow(AccountNotFoundException::new);
        mokRefreshRepository.refresh(account);

        if (!versionHash.equals(versionHashService.hashObjectVersion(uuid, account.getVersion()))) {
            throw new ConcurrentUpdateException();
        }

        AccessLevelDict accessLevelDict = accessLevelDictRepository.findByAccessLevelName(accessLevelName)
                .orElseThrow(AccessLevelDictNotFoundException::new);

        var existingAccessLevelOpt = account.getAccessLevelList().stream()
                .filter(al -> al.getAccessLevelDict() != null &&
                        al.getAccessLevelDict().getId().equals(accessLevelDict.getId()))
                .findFirst();

        if (existingAccessLevelOpt.isPresent()) {
            AccessLevel existingAccessLevel = existingAccessLevelOpt.get();
            if (existingAccessLevel.getActive()) {
                throw new AccessLevelAlreadyAssignedException();
            } else {
                existingAccessLevel.setActive(true);
                Account saved = accountRepository.saveAndFlush(account);

                postCommitTransactionManager.register(() ->
                        emailService.sendAccessLevelGrantedInfo(account.getEmail(),
                                account.getLogin(),
                                accessLevelName,
                                account.getLanguageVersion().getLanguage()));
                return accountMapper.toAccountWithAccessLevelsAndVersionHashDTO(
                        saved,
                        versionHashService.hashObjectVersion(saved.getId(), saved.getVersion())
                );
            }
        }

        AccessLevel newAccessLevel = switch (accessLevelName) {
            case AccountConsts.ROLE_ADMIN -> new Admin();
            case AccountConsts.ROLE_STUDENT -> new Student();
            case AccountConsts.ROLE_TEACHER -> new Teacher();
            default -> throw new AccessLevelDictNotFoundException();
        };

        newAccessLevel.setAccessLevelDict(accessLevelDict);
        newAccessLevel.setAccount(account);
        newAccessLevel.setActive(true);

        account.getAccessLevelList().add(newAccessLevel);
        Account saved = accountRepository.saveAndFlush(account);

        postCommitTransactionManager.register(() ->
                emailService.sendAccessLevelGrantedInfo(account.getEmail(), account.getLogin(), accessLevelName, account.getLanguageVersion().getLanguage()));
        return accountMapper.toAccountWithAccessLevelsAndVersionHashDTO(
                saved,
                versionHashService.hashObjectVersion(saved.getId(), saved.getVersion())
        );
    }
```
Listing 10.10. Metoda inicjująca transakcję aplikacyjną dla przypadku MOK.6.
W ramach tej transakcji wywoływane są metody komponentów dostępowych z propagacją `MANDATORY`:
* `AccountRepository.findById(uuid)`,
* `MokRefreshRepository.refresh(account)`,
* `AccessLevelDictRepository.findByAccessLevelName(accessLevelName)`,
* `AccountRepository.saveAndFlush(account)`.

Operacje te wykonywane są na encjach JPA: **Account**, **AccessLevelDict** oraz typach dziedziczących po **AccessLevel** 
(**Admin**, **Student**, **Teacher**).
### MOK.7 - Odłącz poziom dostępu do konta.
Na powyższy przypadek użycia składa się jedna transakcja aplikacyjna.
Jest ona inicjowana przez metodę `revokeAccessLevel(UUID, String, String)` z klasy `AccountService` z propagacją `REQUIRES_NEW`.
```java
    @Transactional(propagation = Propagation.REQUIRES_NEW, transactionManager = "mokTransactionManager")
    @PreAuthorize("hasAuthority(T(pl.lodz.p.it.ssbd2026.ssbd01.config.consts.AccountConsts).ROLE_ADMIN)")
    @Retryable(
            retryFor = {JpaSystemException.class, ConcurrentUpdateException.class},
            backoff = @Backoff(delayExpression = "${app.retry.backoff}"),
            maxAttemptsExpression = "${app.retry.maxattempts}"
    )
    public void revokeAccessLevel(UUID uuid, String accessLevelName, String versionHash) {
        Account account = accountRepository.findById(uuid).orElseThrow(AccountNotFoundException::new);
        mokRefreshRepository.refresh(account);

        if (!versionHash.equals(versionHashService.hashObjectVersion(uuid, account.getVersion()))) {
            throw new ConcurrentUpdateException();
        }

        AccessLevelDict accessLevelDict = accessLevelDictRepository.findByAccessLevelName(accessLevelName)
                .orElseThrow(AccessLevelDictNotFoundException::new);

        AccessLevel accessLevelToRevoke = account.getAccessLevelList().stream()
                .filter(al -> al.getAccessLevelDict() != null &&
                        al.getAccessLevelDict().getId().equals(accessLevelDict.getId()))
                .findFirst()
                .orElseThrow(AccessLevelNotAssignedException::new);

        if (!accessLevelToRevoke.getActive()) {
            throw new AccessLevelAlreadyRevokedException();
        }

        accessLevelToRevoke.setActive(false);

        accountRepository.saveAndFlush(account);

        postCommitTransactionManager.register(() ->
                emailService.sendAccessLevelRevokedInfo(account.getEmail(), account.getLogin(), accessLevelName, account.getLanguageVersion().getLanguage()));
    }
```
Listing 10.11. Metoda inicjująca transakcję aplikacyjną dla przypadku MOK.7.
W ramach tej transakcji wywoływane są metody komponentów dostępowych z propagacją `MANDATORY`:
* `AccountRepository.findById(uuid)`,
* `MokRefreshRepository.refresh(account)`,
* `AccessLevelDictRepository.findByAccessLevelName(accessLevelName)`,
* `AccountRepository.saveAndFlush(account)`.

Operacje te wykonywane są na encjach JPA: **Account**, **AccessLevelDict** oraz **AccessLevel**.

### MOK.8 - Zmień własne hasło.
Na powyższy przypadek użycia składa się jedna transakcja aplikacyjna.
Jest ona inicjowana przez metodę **changeOwnPassword(String, String, String, String)** z klasy **AccountService** 
z propagacją **REQUIRES_NEW**.

```java
    @PreAuthorize("isAuthenticated()")
    @Transactional(propagation = Propagation.REQUIRES_NEW,
            readOnly = false,
            transactionManager = "mokTransactionManager")
    @Retryable(
            retryFor = {ConcurrentUpdateException.class},
            backoff = @Backoff(delay = 1000)
    )
    @MethodCallLogged
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
        accountRepository.saveAndFlush(account);
    }
```
Listing 10.12. Metoda inicjująca transakcję aplikacyjną dla przypadku MOK.8.

Metoda pomocnicza `validateAndSetNewPassword` weryfikuje, że nowe hasło nie jest tożsame z bieżącym ani z żadnym hasłem 
historycznym, koduje je i dołącza nowy wpis do historii haseł.

W ramach transakcji wywoływane są metody komponentów dostępowych z propagacją `MANDATORY`:
* `AccountRepository.findByLogin(login)`,
* `MokRefreshRepository.refresh(account)`,
* `AccountRepository.saveAndFlush(account)`.

Operacje te wykonywane są na encjach JPA: **Account** oraz **PasswordHistory**.
### MOK.9 - Zmiana hasła innego użytkownika.
Na powyższy przypadek użycia składa się jedna transakcja aplikacyjna.
Jest ona inicjowana przez metodę `changeOtherPassword(UUID, String, String)` z klasy `AccountService` 
z propagacją `REQUIRES_NEW`.

```java
    @MethodCallLogged
    @PreAuthorize("hasAuthority(T(pl.lodz.p.it.ssbd2026.ssbd01.config.consts.AccountConsts).ROLE_ADMIN)")
    @Retryable(retryFor = ConcurrentUpdateException.class)
    @Transactional(propagation = Propagation.REQUIRES_NEW, transactionManager = "mokTransactionManager")
    public void changeOtherPassword(UUID id, String newPassword, String ifMatch) {
        Account account = accountRepository.findById(id).orElseThrow(AccountNotFoundException::new);

        mokRefreshRepository.refresh(account);
        if (!ifMatch.equals(versionHashService.hashObjectVersion(account.getId(), account.getVersion()))) {
            throw new ConcurrentUpdateException();
        }

        validateAndSetNewPassword(account, newPassword);
        accountRepository.saveAndFlush(account);
    }
```
Listing 10.13. Metoda inicjująca transakcję aplikacyjną dla przypadku MOK.9.

W ramach transakcji wywoływane są metody komponentów dostępowych z propagacją `MANDATORY`:
* `AccountRepository.findById(id)`,
* `MokRefreshRepository.refresh(account)`,
* `AccountRepository.saveAndFlush(account)`.

Operacje te wykonywane są na encjach JPA: `Account` oraz `PasswordHistory`.
### MOK.10 - Zmień adres e-mail własnego konta.
Na powyższy przypadek użycia składają się dwie transakcje aplikacyjne i opcjonalnie trzecia pozwalająca cofnąć zmianę.
Pierwsza transakcja generuje jednorazowy token i wysyła użytkownikowi link do strony zmiany adresu e-mail. 
Jest inicjowana przez metodę `createUrlOwnEmailChange(String)` z klasy `AccountService` z propagacją `REQUIRES_NEW`.
```java
    @MethodCallLogged
    @Retryable(retryFor = ConcurrentUpdateException.class)
    @Transactional(propagation = Propagation.REQUIRES_NEW, transactionManager = "mokTransactionManager")
    public void createUrlOwnEmailChange(String login) {
        Account account = accountRepository.findByLogin(login)
                .orElseThrow(() -> new AccountNotFoundException());

        String generateUrl = UUID.randomUUID().toString();
        OneTimeTokenEnt token = new OneTimeTokenEnt();
        token.setTokenValue(generateUrl);
        token.setUsername(login);
        token.setExpiresAt(LocalDateTime.now().plusMinutes(OneTimeTokenConsts.EXPIRATION_TIME_MINUTES));
        token.setTokenType(OneTimeTokenType.OWN_EMAIL_CHANGE);
        token.setUsed(false);

        tokenRepository.saveAndFlush(token);

        String changeUrl = frontendUrl + "/change-email?token=" + generateUrl;
        postCommitTransactionManager.register(() ->
                emailService.sendUrlToChangeOwnEmail(account.getEmail(), account.getLogin(), changeUrl, account.getLanguageVersion().getLanguage()));
    }
```
Listing 10.14. Metoda inicjująca pierwszą transakcję aplikacyjną dla przypadku MOK.10.

W ramach tej transakcji wywoływane są metody komponentów dostępowych z propagacją `MANDATORY`:
* `AccountRepository.findByLogin(login)`,
* `OneTimeTokenRepository.saveAndFlush(token)`.

Operacje te wykonywane są na encjach JPA: **Account** oraz **OneTimeTokenEnt**.

Druga transakcja weryfikuje token i dokonuje faktycznej zmiany adresu e-mail. Tworzy przy tym token umożliwiający 
cofnięcie zmiany. Jest inicjowana przez metodę `confirmOwnEmailChange(String, String, String)` z propagacją `REQUIRES_NEW`.

```java
    @MethodCallLogged
    @Retryable(retryFor = ConcurrentUpdateException.class)
    @Transactional(propagation = Propagation.REQUIRES_NEW, transactionManager = "mokTransactionManager")
    public void confirmOwnEmailChange(String token, String password, String newEmail) {

        OneTimeTokenEnt tokenEntity = tokenRepository
                .findByTokenValueAndTokenTypeAndIsUsedIsFalseAndExpiresAtAfter(token, OneTimeTokenType.OWN_EMAIL_CHANGE, LocalDateTime.now())
                .orElseThrow(OneTimeTokenInvalidException::new);

        Account account = accountRepository.findByLogin(tokenEntity.getUsername())
                .orElseThrow(AccountNotFoundException::new);

        if (!passwordEncoder.matches(password, account.getPasswordHash())) {
            throw new InvalidCredentialsProvidedException();
        }

        String oldEmail = account.getEmail();
        account.setEmail(newEmail);
        tokenEntity.setUsed(true);

        String revertUrlToken = UUID.randomUUID().toString();
        EmailRevertTokenEnt revertToken = new EmailRevertTokenEnt();
        revertToken.setTokenValue(revertUrlToken);
        revertToken.setUsername(account.getLogin());
        revertToken.setOldEmail(oldEmail);
        revertToken.setExpiresAt(LocalDateTime.now().plusMinutes(OneTimeTokenConsts.EXPIRATION_TIME_MINUTES));
        revertToken.setUsed(false);

        accountRepository.saveAndFlush(account);
        tokenRepository.saveAndFlush(tokenEntity);
        revertTokenRepository.saveAndFlush(revertToken);

        String revertUrl = frontendUrl + "/revert-email?token=" + revertUrlToken;
        postCommitTransactionManager.register(() ->
                emailService.sendComebackEmailAddressLink(oldEmail, account.getLogin(), newEmail, revertUrl, account.getLanguageVersion().getLanguage()));
    }
```
Listing 10.15. Metoda inicjująca drugą transakcję aplikacyjną dla przypadku MOK.10.

W ramach tej transakcji wywoływane są metody komponentów dostępowych z propagacją `MANDATORY`:
* `OneTimeTokenRepository.findByTokenValueAndTokenTypeAndIsUsedIsFalseAndExpiresAtAfter(...),`
* `AccountRepository.findByLogin(login)`,
* `AccountRepository.saveAndFlush(account)`,
* `OneTimeTokenRepository.saveAndFlush(tokenEntity)`,
* `EmailRevertTokenRepository.saveAndFlush(revertToken)`.

Operacje te wykonywane są na encjach JPA: **Account**, **OneTimeTokenEnt** oraz **EmailRevertTokenEnt**

Opcjonalnie, użytkownik może cofnąć zmianę adresu e-mail korzystając z linku revertującego. Transakcja realizująca 
tę operację jest inicjowana przez metodę `revertEmailChange(String)` z propagacją `REQUIRES_NEW`.
```java
    @MethodCallLogged
    @Retryable(retryFor = ConcurrentUpdateException.class)
    @Transactional(propagation = Propagation.REQUIRES_NEW, transactionManager = "mokTransactionManager")
    public void revertEmailChange(String token) {
        EmailRevertTokenEnt revertToken = revertTokenRepository.findByTokenValue(token)
                .orElseThrow(OneTimeTokenInvalidException::new);

        if (revertToken.isUsed() || revertToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new OneTimeTokenInvalidException();
        }

        Account account = accountRepository.findByLogin(revertToken.getUsername())
                .orElseThrow(AccountNotFoundException::new);

        account.setEmail(revertToken.getOldEmail());
        revertToken.setUsed(true);

        accountRepository.saveAndFlush(account);
        revertTokenRepository.saveAndFlush(revertToken);
    }
```
Listing 10.16. Metoda inicjująca opcjonalną transakcję cofnięcia zmiany adresu e-mail.

W ramach tej transakcji wywoływane są metody komponentów dostępowych z propagacją `MANDATORY`:
* `EmailRevertTokenRepository.findByTokenValue(token)`
* `AccountRepository.findByLogin(login)`
* `AccountRepository.saveAndFlush(account)`
* `EmailRevertTokenRepository.saveAndFlush(revertToken)`

Operacje te wykonywane są na encjach JPA: **Account** oraz **EmailRevertTokenEnt**.
### MOK.11 - Zmień adres e-mail innego konta.
Na powyższy przypadek użycia składa się jedna transakcja aplikacyjna.
Jest ona inicjowana przez metodę changeEmailByAdmin(UUID, String, String) z klasy AccountService z propagacją REQUIRES_NEW.
```java
    @PreAuthorize("hasAuthority(T(pl.lodz.p.it.ssbd2026.ssbd01.config.consts.AccountConsts).ROLE_ADMIN)")
    @Transactional(
            propagation = Propagation.REQUIRES_NEW,
            readOnly = false,
            transactionManager = "mokTransactionManager"
    )
    @Retryable(
            retryFor = {JpaSystemException.class},
            backoff = @Backoff(delayExpression = "${app.retry.backoff}"),
            maxAttemptsExpression = "${app.retry.maxattempts}"
    )
    public Account changeEmailByAdmin(UUID targetAccountId, String newEmail, String frontendVersionHash) {
        Account account = accountRepository.findById(targetAccountId).orElseThrow(AccountNotFoundException::new);
        mokRefreshRepository.refresh(account);
        String expectedVersionHash = hashService.hashObjectVersion(targetAccountId, account.getVersion());
        if (!expectedVersionHash.equals(frontendVersionHash)) {
            throw new ConcurrentUpdateException();
        }
        String oldEmail = account.getEmail();
        account.setEmail(newEmail);

        postCommitTransactionManager.register(() -> emailService.sendEmailAddressChangedByAdmin(oldEmail, account.getLanguageVersion().getLanguage()));
        postCommitTransactionManager.register(() -> emailService.sendEmailAddressChangedByAdmin(newEmail, account.getLanguageVersion().getLanguage()));

        return accountRepository.saveAndFlush(account);
    }
```
Listing 10.17. Metoda inicjująca transakcję aplikacyjną dla przypadku MOK.11.

W ramach tej transakcji wywoływane są metody komponentów dostępowych z propagacją `MANDATORY`:
* `AccountRepository.findById(targetAccountId)`
* `MokRefreshRepository.refresh(account)`
* `AccountRepository.saveAndFlush(account)`

Operacje te wykonywane są na encji JPA **Account**.
### MOK.12 - Edytuj dane własnego konta.
Na powyższy przypadek użycia składa się jedna transakcja aplikacyjna.
Jest ona inicjowana przez metodę `updateAccountByLogin(String, String, Account)` z klasy `AccountService` 
z propagacją `REQUIRES_NEW`.
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
Listing 10.18. Metoda inicjująca transakcję aplikacyjną dla przypadku MOK.12.

W ramach tej transakcji wywoływane są metody komponentów dostępowych z propagacją `MANDATORY`:
* `AccountRepository.findByLogin(login)`
* `MokRefreshRepository.refresh(account)`
* `AccountRepository.saveAndFlush(account)`

Operacje te wykonywane są na encji JPA **Account**.
### MOK.13 - Edytuj dane innego użytkownika.
Na powyższy przypadek użycia składa się jedna transakcja aplikacyjna.
Jest ona inicjowana przez metodę `updateAccount(UUID, String, Account)` z klasy `AccountService` z propagacją `REQUIRES_NEW`.
```java
    @PreAuthorize("hasAuthority(T(pl.lodz.p.it.ssbd2026.ssbd01.config.consts.AccountConsts).ROLE_ADMIN)")
    @Transactional(propagation = Propagation.REQUIRES_NEW, transactionManager = "mokTransactionManager")
    public AccountWithAccessLevelsDTO updateAccount(UUID id, String versionHash, Account source) {
        Account target = accountRepository.findById(id).orElseThrow(AccountNotFoundException::new);
        mokRefreshRepository.refresh(target);

        if (!versionHash.equals(versionHashService.hashObjectVersion(id, target.getVersion()))) {
            throw new ConcurrentUpdateException();
        }

        accountMapper.updateExistingAccount(source, target);
        Account savedAccount = accountRepository.saveAndFlush(target);
        return accountMapper.toAccountWithAccessLevelsDTO(savedAccount);
    }
```
Listing 10.19. Metoda inicjująca transakcję aplikacyjną dla przypadku MOK.13.

W ramach tej transakcji wywoływane są metody komponentów dostępowych z propagacją `MANDATORY`:
* `AccountRepository.findById(id)`
* `MokRefreshRepository.refresh(account)`
* `AccountRepository.saveAndFlush(account)`

Operacje te wykonywane są na encji JPA **Account**.
### MOK.14 - Wyloguj.
Z racji, że aplikacja jest oparta na uwierzytelnieniu poprzez bezstanowy token JWT, operacja wylogowania się polega 
tylko i wyłącznie na porzuceniu tokenu po stronie aplikacji klienckiej. Z tego faktu, po stronie warstwy logiki 
biznesowej aplikacji, nie jest wywoływana żadna metoda modyfikująca bądź odczytująca stan bazy danych, a zatem 
w ramach tego przypadku użycia nie istnieje żadna transakcja aplikacyjna.

### MOK.15 - Pobierz konta użytkowników w systemie
Na powyższy przypadek użycia składają się dwie niezależne transakcje aplikacyjne. Obie są inicjowane przez metody 
z klasy `AccountService` z propagacją `REQUIRES_NEW`.

Pierwsza transakcja pobiera stronicowaną listę kont.
```java
    @Transactional(propagation = Propagation.REQUIRES_NEW, transactionManager = "mokTransactionManager")
    @PreAuthorize("hasAuthority(T(pl.lodz.p.it.ssbd2026.ssbd01.config.consts.AccountConsts).ROLE_ADMIN)")
    @Retryable(
            retryFor = {JpaSystemException.class, ConcurrentUpdateException.class},
            backoff = @Backoff(delayExpression = "${app.retry.backoff}"),
            maxAttemptsExpression = "${app.retry.maxattempts}"
    )
    public Page<Account> getAllAccounts(Integer page, Integer size, String sortBy, Boolean sortDesc, String username) {
        Account admin = accountRepository.findByLogin(username)
                .orElseThrow(AccountNotFoundException::new);

        updatePreferencesIfProvided(admin, size, sortBy, sortDesc);

        Pageable pageable = createPageable(page, admin);
        return accountRepository.findAll(pageable);
    }
```
Listing 10.22. Metoda inicjująca transakcję aplikacyjną pobierającą stronicowaną listę kont dla przypadku MOK.15.

Metoda pomocnicza `updatePreferencesIfProvided` wywołuje `AccountRepository.saveAndFlush(admin)` warunkowo tzn. tylko 
gdy któryś z parametrów stronicowania uległ zmianie.

W ramach tej transakcji wywoływane są metody komponentów dostępowych z propagacją `MANDATORY`:
* `AccountRepository.findAll(pageable)`
* `AccountRepository.saveAndFlush(admin)` opcjonalnie, gdy preferencje stronicowania uległy zmianie.

Operacje te wykonywane są na encji JPA **Account**.

Druga transakcja realizuje wyszukiwanie kont po frazie (login, e-mail, imię lub nazwisko). 
Analogicznie jak powyżej, przy okazji zapisuje preferencje stronicowania administratora.
```java
    @Transactional(propagation = Propagation.REQUIRES_NEW, transactionManager = "mokTransactionManager")
    @PreAuthorize("hasAuthority(T(pl.lodz.p.it.ssbd2026.ssbd01.config.consts.AccountConsts).ROLE_ADMIN)")
    @Retryable(
            retryFor = {JpaSystemException.class, ConcurrentUpdateException.class},
            backoff = @Backoff(delayExpression = "${app.retry.backoff}"),
            maxAttemptsExpression = "${app.retry.maxattempts}"
    )
    public Page<Account> searchAccounts(String phrase, Integer page, Integer size, String sortBy, Boolean sortDesc, String username) {
        Account admin = accountRepository.findByLogin(username)
                .orElseThrow(AccountNotFoundException::new);

        updatePreferencesIfProvided(admin, size, sortBy, sortDesc);

        Pageable pageable = createPageable(page, admin);
        return accountRepository.findByNameContainingIgnoreCaseOrSurnameContainingIgnoreCase(phrase, phrase, pageable);
    }
```
Listing 10.23. Metoda inicjująca transakcję aplikacyjną wyszukiwania kont po frazie dla przypadku MOK.15.

W ramach tej transakcji wywoływane są metody komponentów dostępowych z propagacją `MANDATORY`:
* `AccountRepository.findByLogin(username)`
* `AccountRepository.saveAndFlush(admin) — opcjonalnie`
* `AccountRepository.findByLoginContainingIgnoreCaseOrEmailContainingIgnoreCaseOrNameContainingIgnoreCaseOrSurnameContainingIgnoreCase(...)`

Operacje te wykonywane są na encji JPA **Account**.

### MOK.16 - Wyświetl szczegóły swojego konta
Na powyższy przypadek użycia składa się jedna transakcja aplikacyjna o charakterze wyłącznie odczytowym.
```java
    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true, transactionManager = "mokTransactionManager")
    @PreAuthorize("hasAuthority(T(pl.lodz.p.it.ssbd2026.ssbd01.config.consts.AccountConsts).ROLE_ADMIN) or #login == authentication.name")
    @Retryable(
            retryFor = {JpaSystemException.class, ConcurrentUpdateException.class},
            backoff = @Backoff(delayExpression = "${app.retry.backoff}"),
            maxAttemptsExpression = "${app.retry.maxattempts}"
    )
    public Account getAccountByLogin(String login) {
        Account account = accountRepository.findByLogin(login)
                .orElseThrow(AccountNotFoundException::new);

        account.getAccessLevelList().forEach(accessLevel -> {
            if (accessLevel.getAccessLevelDict() != null) {
                accessLevel.getAccessLevelDict().getAccessLevelName();
            }
        });

        return account;
    }
```
Listing 10.24. Metoda inicjująca transakcję aplikacyjną dla przypadku MOK.16.

Jawne odwołanie do `getAccessLevelName()` w pętli służy wymuszeniu załadowania "leniwych" relacji w granicach otwartej 
transakcji, zanim obiekt trafi do warstwy prezentacji.

W ramach tej transakcji wywoływana jest metoda komponentu dostępowego z propagacją MANDATORY:
* `AccountRepository.findByLogin(login)`

Operacja ta wykonywana jest na encji JPA **Account**.

### Blokady optymistyczne.
Mechanizm blokad optymistycznych stosowany jest we wszystkich scenariuszach modułu MOK, w których dane odczytane 
w pierwszej transakcji mogą zostać zmodyfikowane przez inny wątek przed ich zapisem w drugiej transakcji. 
Dotyczy to przypadków: MOK.4, MOK.5, MOK.6, MOK.7, MOK.8, MOK.9, MOK.11, MOK.12 oraz MOK.13.

Dla wszystkich wymienionych scenariuszy schemat jest jednolity:
* Metoda odczytu danych np. metoda `getAccountByLogin(String)` lub `getAccountWithAccessLevels(UUID)` wczytuje encję 
**Account** w ramach pierwszej transakcji i zwraca ją wraz ze skrótem wersji, wyliczonym przez `VersionHashService.hashObjectVersion(UUID id, Long version)`. 
Skrót ten jest przekazywany do warstwy prezentacji jako nagłówek HTTP **ETag**.
* Metoda modyfikacji danych np. `blockUserAccount(UUID, String)`, `changeOwnPassword(...)` itp. przed dokonaniem zmian 
wywołuje `MokRefreshRepository.refresh(account)` w celu odświeżenia stanu encji bezpośrednio z bazy danych, 
a następnie porównuje przekazany przez warstwę prezentacji skrót wersji ze świeżo wyliczonym skrótem. 
Niezgodność skutkuje rzuceniem `ConcurrentUpdateException`, co powoduje wycofanie transakcji.
* Mechanizm zapewniający wiarygodność wersji, to skrót wersji, który jest generowany na podstawie identyfikatora encji 
i jej pola `@Version`. Uniemożliwia to sfałszowanie wartości wersji po stronie klienta. Przy zapisie JPA automatycznie 
weryfikuje pole `@Version` i jeśli wartość w bazie różni się od wartości w pamięci, rzucany jest `OptimisticLockException`.
* W przypadku innych metod wpływających na mechanizm blokady, wywołanie `MokRefreshRepository.refresh(account)` 
przed weryfikacją skrótu zapobiega posłużeniu się przestarzałą wartością wersji wczytaną wcześniej przez kontekst utrwalania. 
Adnotacja `@Retryable` na metodach modyfikujących powoduje automatyczne ponowienie operacji po wykryciu `ConcurrentUpdateException`.

Przykładowy scenariusz dla blokady optymistycznej na podstawie przypadku MOK.8:
```java
    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true, transactionManager = "mokTransactionManager")
    @PreAuthorize("hasAuthority(T(pl.lodz.p.it.ssbd2026.ssbd01.config.consts.AccountConsts).ROLE_ADMIN) or #login == authentication.name")
    @Retryable(
            retryFor = {JpaSystemException.class, ConcurrentUpdateException.class},
            backoff = @Backoff(delayExpression = "${app.retry.backoff}"),
            maxAttemptsExpression = "${app.retry.maxattempts}"
    )
    public Account getAccountByLogin(String login) {
        Account account = accountRepository.findByLogin(login)
                .orElseThrow(AccountNotFoundException::new);

        account.getAccessLevelList().forEach(accessLevel -> {
            if (accessLevel.getAccessLevelDict() != null) {
                accessLevel.getAccessLevelDict().getAccessLevelName();
            }
        });

        return account;
    }
```
Listing 10.25. Krok 1. Wczytanie danych konta i zwrócenie ich wraz ze skrótem wersji przekazywanym do aplikacji klienckiej.
```java
    @PreAuthorize("isAuthenticated()")
    @Transactional(propagation = Propagation.REQUIRES_NEW,
            readOnly = false,
            transactionManager = "mokTransactionManager")
    @Retryable(
            retryFor = {ConcurrentUpdateException.class},
            backoff = @Backoff(delay = 1000)
    )
    @MethodCallLogged
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
        accountRepository.saveAndFlush(account);
    }
```
Listing 10.26. Krok 2. Modyfikacja danych konta.

Jeżeli w czasie między odczytem a zapisem inny wątek zmodyfikuje encję **Account**, pole `@Version` zmieni wartość w bazie danych. 
Wywołanie `mokRefreshRepository.refresh(account)` pobiera aktualny stan encji, co skutkuje niezgodnością skrótu 
i rzuceniem `ConcurrentUpdateException`. 
Adnotacja `@Retryable` powoduje automatyczne ponowienie operacji po zdefiniowanym czasie oczekiwania, tym razem już z aktualną wartością ETag.

### Blokady pesymistyczne.
W module MOK zastosowano jedną blokadę pesymistyczną, zlokalizowaną w metodzie 
`findByUsernameAndTokenTypeAndIsUsedIsFalse(String, OneTimeTokenType)` repozytorium `OneTimeTokenRepository`:

```java
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<OneTimeTokenEnt> findByUsernameAndTokenTypeAndIsUsedIsFalse(String username, OneTimeTokenType tokenType);
```
Listing 10.27. Blokada pesymistyczna w metodzie repozytorium `OneTimeTokenRepository`.

Blokada jest zakładana na wszystkich obiektach encji `OneTimeTokenEnt` spełniających podane kryteria 
(pasujący username, tokenType oraz token nie może być zużyty). Jest to blokada zapisu, co oznacza, że żadna inna transakcja 
nie może odczytać ani zmodyfikować zablokowanych wierszy aż do zakończenia transakcji wywołującej.

Metoda ta jest wywoływana w dwóch miejscach:
1. `AuthService.generate2FactorCode(Account)` w ramach przypadku użycia MOK.2. Blokada zapobiega wyścigowi, 
w którym dwa równoległe żądania jednocześnie wczytałyby aktywny token 2FA i próbowały go unieważnić oraz wygenerować nowy.
2. `PasswordResetService.initiatePasswordReset(String)` w ramach przypadku użycia MOK.3, przy wyszukiwaniu aktywnych 
tokenów resetujących do unieważnienia. Blokada eliminuje analogiczny wyścig przy jednoczesnym wysłaniu kilku żądań 
resetu hasła dla tego samego adresu e-mail.

Blokada obowiązuje przez cały czas trwania transakcji nadrzędnej i jest zwalniana automatycznie wraz z jej 
zatwierdzeniem lub wycofaniem.