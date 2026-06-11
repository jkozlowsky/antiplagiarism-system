
## 21 Zgłaszane wyjątki 4 pkt

### Wyjątki aplikacyjne

Wszystkie wyjątki aplikacyjne dziedziczą po wspólnej klasie bazowej `AppBaseException`, która definiuje standardowy konstruktor przyjmujący kod HTTP oraz powód rzucenia wyjątku. Klasa bazowa rozszerza `ResponseStatusException`.

```java
public abstract class AppBaseException extends ResponseStatusException {
    protected AppBaseException(HttpStatusCode status, String reason) {
        super(status, reason);
    }

    protected AppBaseException(HttpStatusCode status, String reason, Throwable cause) {
        super(status, reason, cause);
    }
}
```

### 1. Wyjątek bezpieczeństwa celowo zwracający kod 2xx (202 Accepted)

#### 1.1 `InvalidCredentialsProvidedException`
* **Miejsca zgłoszenia**: `AuthService.authenticate()`, `AuthService.authenticateWithGoogle()`, `AuthService.refreshToken()`, `AuthService.resend2FactorCode()`, `AccountService.confirmOwnEmailChange()`
* **Klucz internacjonalizacji**: `error.invalid.credentials`
* **Kod błędu HTTP**: 202
* **Możliwe przyczyny wystąpienia**: Wprowadzenie przez użytkownika nieprawidłowych danych uwierzytelniających (błędny login, hasło lub token autoryzacji). Zastosowanie kodu 202 ma na celu utrudnienie enumeracji kont i maskowanie informacji o ewentualnym braku istnienia konta o podanym loginie.
* **Sposób obsługi**: Wyjątek jest przechwytywany przez globalny handler w adnotowanej metodzie `GlobalAppExceptionHandler.handleAppBaseException()`, która zwraca odpowiedź HTTP 202 ze zlokalizowaną wiadomością z klucza `error.invalid.credentials`.
* **Odwołanie transakcji**: Tak (z wyjątkiem wywołania w `AuthService.authenticate()`, gdzie adnotacja definiuje `noRollbackFor`, aby zapisać błędną próbę logowania i zaktualizować odpowiednie pole w profilu).

### 2. Wyjątki wynikające z błędnych żądań (400 Bad Request)

#### 2.1 `AccountActivationException`
* **Miejsca zgłoszenia**: `AccountService.confirmActivation()`
* **Klucz internacjonalizacji**: `error.activation.invalid` lub `error.activation.token.expired`
* **Kod błędu HTTP**: 400
* **Możliwe przyczyny wystąpienia**: Nieudana próba aktywacji nowo zarejestrowanego konta (np. przeterminowany po upływie 24 godzin token weryfikacyjny).
* **Sposób obsługi**: Wyjątek jest przechwytywany przez globalny handler w adnotowanej metodzie `GlobalAppExceptionHandler.handleAppBaseException()`, która zwraca odpowiedź HTTP 400 ze zlokalizowaną wiadomością z zadanego klucza.
* **Odwołanie transakcji**: Tak

#### 2.2 `InvalidOldPasswordException`
* **Miejsca zgłoszenia**: `AccountService.changeOwnPassword()`
* **Klucz internacjonalizacji**: `error.password.invalid.old`
* **Kod błędu HTTP**: 400
* **Możliwe przyczyny wystąpienia**: Weryfikacja dotychczasowego hasła podczas procedury zmiany własnego hasła kończy się niepowodzeniem (użytkownik wpisał w formularzu błędne obecne hasło).
* **Sposób obsługi**: Wyjątek jest przechwytywany przez globalny handler w adnotowanej metodzie `GlobalAppExceptionHandler.handleAppBaseException()`, która zwraca odpowiedź HTTP 400 ze zlokalizowaną wiadomością z klucza `error.password.invalid.old`.
* **Odwołanie transakcji**: Tak

#### 2.3 `RulePresetInvalidPartametersException`
* **Miejsca zgłoszenia**: `RuleService.createRulePreset()`
* **Klucz internacjonalizacji**: `error.rolePreset.invalid.parameters`
* **Kod błędu HTTP**: 400
* **Możliwe przyczyny wystąpienia**: Próba zapisania nowego globalnego zestawu reguł oceny kodu (np. skanera plagiatów) z przekazanymi w żądaniu pustymi (`null`) lub nieprawidłowymi polami.
* **Sposób obsługi**: Wyjątek jest przechwytywany przez globalny handler w adnotowanej metodzie `GlobalAppExceptionHandler.handleAppBaseException()`, która zwraca odpowiedź HTTP 400 ze zlokalizowaną wiadomością z klucza `error.rolePreset.invalid.parameters`.
* **Odwołanie transakcji**: Tak

#### 2.4 `RulePresetNoAccessException`
* **Miejsca zgłoszenia**: `RuleService.deleteRulePreset()`
* **Klucz internacjonalizacji**: `error.rule.preset.access.denied`
* **Kod błędu HTTP**: 400
* **Możliwe przyczyny wystąpienia**: Administrator lub prowadzący próbuje za pośrednictwem modułu szablonów usunąć lokalny zestaw reguł, który nie jest globalnym szablonem (`isPreset == false`), a jest przypisany na sztywno do specyficznego przedmiotu.
* **Sposób obsługi**: Wyjątek jest przechwytywany przez globalny handler w adnotowanej metodzie `GlobalAppExceptionHandler.handleAppBaseException()`, która zwraca odpowiedź HTTP 400 ze zlokalizowaną wiadomością z klucza `error.rule.preset.access.denied`.
* **Odwołanie transakcji**: Tak

#### 2.5 `SubjectMustHaveOwnerException`
* **Miejsca zgłoszenia**: `SubjectService.updateSubject()`
* **Klucz internacjonalizacji**: `error.subject.must.have.owner`
* **Kod błędu HTTP**: 400
* **Możliwe przyczyny wystąpienia**: Próba modyfikacji ról przypisanych do przedmiotu doprowadzająca do sytuacji, w której dany przedmiot nie posiadałby żadnego prowadzącego pełniącego funkcję właściciela głównego (`OWNER`).
* **Sposób obsługi**: Wyjątek jest przechwytywany przez globalny handler w adnotowanej metodzie `GlobalAppExceptionHandler.handleAppBaseException()`, która zwraca odpowiedź HTTP 400 ze zlokalizowaną wiadomością z klucza `error.subject.must.have.owner`.
* **Odwołanie transakcji**: Tak

### 3. Wyjątki braku uprawnień (403 Forbidden)

#### 3.1 `NoPermissionForThisSubjectException`
* **Miejsca zgłoszenia**: `ScheduleService.getSchedules()`, `ScheduleService.createSchedule()`
* **Klucz internacjonalizacji**: `subject.assignment.no.permission`
* **Kod błędu HTTP**: 403
* **Możliwe przyczyny wystąpienia**: Wywołujący żądanie prowadzący próbuje utworzyć lub odczytać harmonogram dla przedmiotu, do którego nie jest przypisany.
* **Sposób obsługi**: Wyjątek jest przechwytywany przez globalny handler w adnotowanej metodzie `GlobalAppExceptionHandler.handleAppBaseException()`, która zwraca odpowiedź HTTP 403 ze zlokalizowaną wiadomością z klucza `subject.assignment.no.permission`.
* **Odwołanie transakcji**: Tak

#### 3.2 `NoPermissionToEditScheduleException`
* **Miejsca zgłoszenia**: `ScheduleService.updateSchedule()`, `ScheduleService.deleteSchedule()`
* **Klucz internacjonalizacji**: `schedule.cannot.edit`
* **Kod błędu HTTP**: 403
* **Możliwe przyczyny wystąpienia**: Prowadzący przypisany do danego przedmiotu nie posiada uprawnień pozwalających na wprowadzanie zmian w harmonogramach (próba edycji lub usunięcia harmonogramu bez odpowiednich uprawnień).
* **Sposób obsługi**: Wyjątek jest przechwytywany przez globalny handler w adnotowanej metodzie `GlobalAppExceptionHandler.handleAppBaseException()`, która zwraca odpowiedź HTTP 403 ze zlokalizowaną wiadomością z klucza `schedule.cannot.edit`.
* **Odwołanie transakcji**: Tak

#### 3.3 `NoTicketsLeftException`
* **Miejsca zgłoszenia**: `ReportService.generateStudentReport()`
* **Klucz internacjonalizacji**: `error.repository.no.tickets`
* **Kod błędu HTTP**: 403
* **Możliwe przyczyny wystąpienia**: Student wykorzystał przypisaną mu, maksymalną dopuszczalną ilość generacji osobistego raportu antyplagiatowego (brak wolnych tzw. "biletów").
* **Sposób obsługi**: Wyjątek jest przechwytywany przez globalny handler w adnotowanej metodzie `GlobalAppExceptionHandler.handleAppBaseException()`, która zwraca odpowiedź HTTP 403 ze zlokalizowaną wiadomością z klucza `error.repository.no.tickets`.
* **Odwołanie transakcji**: Tak

#### 3.4 `SubjectAccessDeniedException`
* **Miejsca zgłoszenia**: `SubjectService.syncSubjectWithGitea()`, `SubjectService.getSubjectDetails()`, `SubjectService.getStudentSubjectDetails()`, `SubjectService.updateSubject()`, `SubjectService.deleteSubject()`, `ReportService.generateReportOnDemand()`, `ReportService.getStudentReportDetails()`, `ReportService.getTeacherReportDetails()`, `ReportService.generateStudentReport()`
* **Klucz internacjonalizacji**: `error.subject.access.denied`
* **Kod błędu HTTP**: 403
* **Możliwe przyczyny wystąpienia**: Brak uprawnień do skanowania przedmiotu na żądanie, zmieniania ustawień przedmiotu.
* **Sposób obsługi**: Wyjątek jest przechwytywany przez globalny handler w adnotowanej metodzie `GlobalAppExceptionHandler.handleAppBaseException()`, która zwraca odpowiedź HTTP 403 ze zlokalizowaną wiadomością z klucza `error.subject.access.denied`.
* **Odwołanie transakcji**: Tak

### 4. Wyjątki braku oczekiwanych zasobów (404 Not Found)

#### 4.1 `AccountNotFoundException`
* **Miejsca zgłoszenia**: `AccountService.getAccountWithAccessLevels()`, `AccountService.getAllAccounts()`, `AccountService.getAccountByLogin()`, `AccountService.searchAccounts()`, `AccountService.grantAccessLevel()`, `AccountService.revokeAccessLevel()`, `AccountService.changeEmailByAdmin()`, `AccountService.changeOwnPassword()`, `AccountService.blockUserAccount()`, `AccountService.unblockUserAccount()`, `AccountService.createUrlOwnEmailChange()`, `AccountService.confirmOwnEmailChange()`, `AccountService.revertEmailChange()`, `AccountService.updateAccount()`, `AccountService.updateAccountByLogin()`, `AccountService.changeOtherPassword()`, `AccountService.unblockAccountViaEmailToken()`, `AccountService.changeAccountLanguage()`, `AuthService.authenticateWithGoogle()`, `AuthService.verify2FactorCode()`, `AuthService.authenticateOnlyWithEmail()`, `AuthService.verifyEmailOnlyAuthCode()`, `PasswordResetService.resolvePasswordReset()`
* **Klucz internacjonalizacji**: `error.account.not.found`
* **Kod błędu HTTP**: 404
* **Możliwe przyczyny wystąpienia**: Brak możliwości znalezienia przypisanego do wskazanego loginu lub UUID rekordu konta użytkownika.
* **Sposób obsługi**: Wyjątek jest przechwytywany przez globalny handler w adnotowanej metodzie `GlobalAppExceptionHandler.handleAppBaseException()`, która zwraca odpowiedź HTTP 404 ze zlokalizowaną wiadomością z klucza `error.account.not.found`.
* **Odwołanie transakcji**: Tak

#### 4.2 `GitCloneException`
* **Miejsca zgłoszenia**: `ReportService.generateReportOnDemand()`
* **Klucz internacjonalizacji**: `error.git.tag.notfound`
* **Kod błędu HTTP**: 404
* **Możliwe przyczyny wystąpienia**: Próba wygenerowania skanowania raportu z repozytoriów dla zadanego tagu, który jeszcze nie występuje u żadnego ze studentów w projekcie.
* **Sposób obsługi**: Wyjątek jest przechwytywany przez globalny handler w adnotowanej metodzie `GlobalAppExceptionHandler.handleAppBaseException()`, która zwraca odpowiedź HTTP 404 ze zlokalizowaną wiadomością z klucza `error.git.tag.notfound`.
* **Odwołanie transakcji**: Tak

#### 4.3 `OriginalLanguageNotFoundException`
* **Miejsca zgłoszenia**: `SubjectDescriptionTranslationService.translateSubjectDescription()`
* **Klucz internacjonalizacji**: `error.original.language.notfound`
* **Kod błędu HTTP**: 404
* **Możliwe przyczyny wystąpienia**: Próba tłumaczenia opisu przedmiotu na nowy język w sytuacji, w której oryginalny słownik / wzorzec nie znajduje się w bazie.
* **Sposób obsługi**: Wyjątek jest przechwytywany przez globalny handler w adnotowanej metodzie `GlobalAppExceptionHandler.handleAppBaseException()`, która zwraca odpowiedź HTTP 404 ze zlokalizowaną wiadomością z klucza `error.original.language.notfound`.
* **Odwołanie transakcji**: Tak

#### 4.4 `RaportLevelDictNotFoundException`
* **Miejsca zgłoszenia**: `SubjectService.createSubjectManual()`, `SubjectService.createSubjectFromTemplate()`, `SubjectService.updateSubject()`, `RuleService.createRulePreset()`, `RuleService.updateRulePreset()`.
* **Klucz internacjonalizacji**: `error.reportLevel.dict.not.found`
* **Kod błędu HTTP**: 404
* **Możliwe przyczyny wystąpienia**: Próba utworzenia przedmiotu / raportu z etykietą raportowania, która nie istnieje w słowniku w bazie danych systemu.
* **Sposób obsługi**: Wyjątek jest przechwytywany przez globalny handler w adnotowanej metodzie `GlobalAppExceptionHandler.handleAppBaseException()`, która zwraca odpowiedź HTTP 404 ze zlokalizowaną wiadomością z klucza `error.reportLevel.dict.not.found`.
* **Odwołanie transakcji**: Tak

#### 4.5 `ReportNotFoundException`
* **Miejsca zgłoszenia**: `ReportService.getStudentReportDetails()`, `ReportService.getTeacherReportDetails()`, `ReportService.deleteReport()`
* **Klucz internacjonalizacji**: `error.report.notfound`
* **Kod błędu HTTP**: 404
* **Możliwe przyczyny wystąpienia**: Próba analizy, wglądu w detale lub ręcznego usunięcia nieistniejącego obiektu podsumowania skanowania JPlag (błędny UUID raportu).
* **Sposób obsługi**: Wyjątek jest przechwytywany przez globalny handler w adnotowanej metodzie `GlobalAppExceptionHandler.handleAppBaseException()`, która zwraca odpowiedź HTTP 404 ze zlokalizowaną wiadomością z klucza `error.report.notfound`.
* **Odwołanie transakcji**: Tak

#### 4.6 `RepositoryNotFoundException`
* **Miejsca zgłoszenia**: `ReportService.generateStudentReport()`
* **Klucz internacjonalizacji**: `error.repository.not.found`
* **Kod błędu HTTP**: 404
* **Możliwe przyczyny wystąpienia**: Użytkownik żąda skanowania dla wskazanego identyfikatora repozytorium Gitea w kontekście określonego zadania, jednak wpis powiązany z tym repozytorium nie istnieje.
* **Sposób obsługi**: Wyjątek jest przechwytywany przez globalny handler w adnotowanej metodzie `GlobalAppExceptionHandler.handleAppBaseException()`, która zwraca odpowiedź HTTP 404 ze zlokalizowaną wiadomością z klucza `error.repository.not.found`.
* **Odwołanie transakcji**: Tak

#### 4.7 `RulePresetNotFoundException`
* **Miejsca zgłoszenia**: `RuleService.updateRulePreset()`, `RuleService.deleteRulePreset()`
* **Klucz internacjonalizacji**: `error.rule.preset.not.found`
* **Kod błędu HTTP**: 404
* **Możliwe przyczyny wystąpienia**: Nie zlokalizowano rekordu z bazowym zestawem reguł (np. skasowanego wcześniej).
* **Sposób obsługi**: Wyjątek jest przechwytywany przez globalny handler w adnotowanej metodzie `GlobalAppExceptionHandler.handleAppBaseException()`, która zwraca odpowiedź HTTP 404 ze zlokalizowaną wiadomością z klucza `error.rule.preset.not.found`.
* **Odwołanie transakcji**: Tak

#### 4.8 `RulePresetTemplateNotFoundException`
* **Miejsca zgłoszenia**: `SubjectService.createSubjectFromTemplate()`
* **Klucz internacjonalizacji**: `error.rulePreset.template.not.found`
* **Kod błędu HTTP**: 404
* **Możliwe przyczyny wystąpienia**: Operacja tworzenia przedmiotu oparta na szablonie reguł, którego nie odnaleziono w bazie danych.
* **Sposób obsługi**: Wyjątek jest przechwytywany przez globalny handler w adnotowanej metodzie `GlobalAppExceptionHandler.handleAppBaseException()`, która zwraca odpowiedź HTTP 404 ze zlokalizowaną wiadomością z klucza `error.rulePreset.template.not.found`.
* **Odwołanie transakcji**: Tak

#### 4.9 `ScheduleNotFoundException`
* **Miejsca zgłoszenia**: `ScheduleService.updateSchedule()`, `ScheduleService.deleteSchedule()`
* **Klucz internacjonalizacji**: `schedule.not.found.message`
* **Kod błędu HTTP**: 404
* **Możliwe przyczyny wystąpienia**: Błędny UUID przekazany w warstwie obsługi zdarzeń dla modyfikacji lub usunięcia harmonogramu.
* **Sposób obsługi**: Wyjątek jest przechwytywany przez globalny handler w adnotowanej metodzie `GlobalAppExceptionHandler.handleAppBaseException()`, która zwraca odpowiedź HTTP 404 ze zlokalizowaną wiadomością z klucza `schedule.not.found.message`.
* **Odwołanie transakcji**: Tak

#### 4.10 `SubjectAssignmentNotFoundException`
* **Miejsca zgłoszenia**: `SubjectService.changeSubjectManager()`
* **Klucz internacjonalizacji**: `error.subject.assignment.not.found`
* **Kod błędu HTTP**: 404
* **Możliwe przyczyny wystąpienia**: Próba oddania praw kierowniczych dla przedmiotu przez prowadzącego, w momencie gdy w systemie nie odnaleziono powiązania rekordu nauczyciela ze wskazanym przedmiotem.
* **Sposób obsługi**: Wyjątek jest przechwytywany przez globalny handler w adnotowanej metodzie `GlobalAppExceptionHandler.handleAppBaseException()`, która zwraca odpowiedź HTTP 404 ze zlokalizowaną wiadomością z klucza `error.subject.assignment.not.found`.
* **Odwołanie transakcji**: Tak

#### 4.11 `SubjectNotFoundException`
* **Miejsca zgłoszenia**: `SubjectService.syncSubjectWithGitea()`, `SubjectService.getSubjectDetails()`, `SubjectService.getStudentSubjectDetails()`, `SubjectService.updateSubject()`, `SubjectService.deleteSubject()`, `SubjectService.changeSubjectManager()`, `SubjectService.getSubjectUsers()`, `ReportService.generateReportOnDemand()`, `ScheduleService.createSchedule()`, `SubjectDescriptionTranslationService.translateSubjectDescription()`
* **Klucz internacjonalizacji**: `error.subject.not.found`
* **Kod błędu HTTP**: 404
* **Możliwe przyczyny wystąpienia**: Żądanie operacji modyfikacji, pobrania statystyk skanowania czy zmian przypisania Gitea w odniesieniu do przedmiotu, którego UUID nie istnieje w bazie.
* **Sposób obsługi**: Wyjątek jest przechwytywany przez globalny handler w adnotowanej metodzie `GlobalAppExceptionHandler.handleAppBaseException()`, która zwraca odpowiedź HTTP 404 ze zlokalizowaną wiadomością z klucza `error.subject.not.found`.
* **Odwołanie transakcji**: Tak

#### Wyjątki konfliktów danych (409 Conflict)

#### 5.1 `AccessLevelAlreadyAssignedException`
* **Miejsca zgłoszenia**: `AccountService.grantAccessLevel()`
* **Klucz internacjonalizacji**: `error.accesslevel.already.assigned`
* **Kod błędu HTTP**: 409
* **Możliwe przyczyny wystąpienia**: Próba nadania roli dostępowej z poziomu panelu administratora, która to rola jest już aktywna na tym koncie.
* **Sposób obsługi**: Wyjątek jest przechwytywany przez globalny handler w adnotowanej metodzie `GlobalAppExceptionHandler.handleAppBaseException()`, która zwraca odpowiedź HTTP 409 ze zlokalizowaną wiadomością z klucza `error.accesslevel.already.assigned`.
* **Odwołanie transakcji**: Tak

#### 5.2 `AccessLevelAlreadyRevokedException`
* **Miejsca zgłoszenia**: `AccountService.revokeAccessLevel()`
* **Klucz internacjonalizacji**: `error.accesslevel.already.revoked`
* **Kod błędu HTTP**: 409
* **Możliwe przyczyny wystąpienia**: Próba odłączenia określonego poziomu dostępu w danym profilu, gdy ta rola już jest niedostępna dla danego użytkownika.
* **Sposób obsługi**: Wyjątek jest przechwytywany przez globalny handler w adnotowanej metodzie `GlobalAppExceptionHandler.handleAppBaseException()`, która zwraca odpowiedź HTTP 409 ze zlokalizowaną wiadomością z klucza `error.accesslevel.already.revoked`.
* **Odwołanie transakcji**: Tak

#### 5.3 `AccessLevelDictNotFoundException`
* **Miejsca zgłoszenia**: `AccountService.registerAccount()`, `AccountService.grantAccessLevel()`
* **Klucz internacjonalizacji**: `error.accesslevel.dict.not.found`
* **Kod błędu HTTP**: 409
* **Możliwe przyczyny wystąpienia**: Brak dostępnego w słowniku wskazanego poziomu ról.
* **Sposób obsługi**: Wyjątek jest przechwytywany przez globalny handler w adnotowanej metodzie `GlobalAppExceptionHandler.handleAppBaseException()`, która zwraca odpowiedź HTTP 409 ze zlokalizowaną wiadomością z klucza `error.accesslevel.dict.not.found`.
* **Odwołanie transakcji**: Tak

#### 5.4 `AccessLevelNotAssignedException`
* **Miejsca zgłoszenia**: `AccountService.revokeAccessLevel()`, `SubjectService.assignTeachers()`, `SubjectService.createSubjectManual()`, `SubjectService.createSubjectFromTemplate()`, `SubjectService.updateSubject()`, `SubjectService.changeSubjectManager()`
* **Klucz internacjonalizacji**: `error.accesslevel.not.assigned`
* **Kod błędu HTTP**: 409
* **Możliwe przyczyny wystąpienia**: Użytkownik, który ma zostać przypisany do panelu przedmiotu i nie posiada roli Nauczyciela.
* **Sposób obsługi**: Wyjątek jest przechwytywany przez globalny handler w adnotowanej metodzie `GlobalAppExceptionHandler.handleAppBaseException()`, która zwraca odpowiedź HTTP 409 ze zlokalizowaną wiadomością z klucza `error.accesslevel.not.assigned`.
* **Odwołanie transakcji**: Tak

#### 5.5 `AccountConstraintViolationException`
* **Miejsca zgłoszenia**: `AccountConstraintViolationsHandlingInterceptor.handleDataIntegrityViolationException()` (przechwytuje błędy przy pomocy AOP z punktu cięcia `Pointcuts.allRepositoryMethods()`, obejmując zautomatyzowane wywołania podczas zapisu we wszystkich repozytoriach).
* **Klucz internacjonalizacji**: `error.account.constraint.violation`
* **Kod błędu HTTP**: 409
* **Możliwe przyczyny wystąpienia**: Utworzenie lub modyfikacja użytkownika uniemożliwiona z racji konfliktu dla parametrów ograniczonych unikalnością (np. dublowany email lub nazwa użytkownika).
* **Sposób obsługi**: Wyjątek jest przechwytywany przez globalny handler w adnotowanej metodzie `GlobalAppExceptionHandler.handleAppBaseException()`, która zwraca odpowiedź HTTP 409 ze zlokalizowaną wiadomością z klucza `error.account.constraint.violation`.
* **Odwołanie transakcji**: Tak

#### 5.6 `AccountIsBlockedException`
* **Miejsca zgłoszenia**: `AuthService.authenticateWithGoogle()`, `AuthService.authenticate()`, `AuthService.resend2FactorCode()`, `AuthService.refreshToken()`, `AuthService.authenticateOnlyWithEmail()`, `AccountService.blockUserAccount()`
* **Klucz internacjonalizacji**: `error.account.already.blocked`
* **Kod błędu HTTP**: 409
* **Możliwe przyczyny wystąpienia**: Próba uwierzytelnienia się zablokowanego z powodów bezpieczeństwa konta.
* **Sposób obsługi**: Wyjątek jest przechwytywany przez globalny handler w adnotowanej metodzie `GlobalAppExceptionHandler.handleAppBaseException()`, która zwraca odpowiedź HTTP 409 ze zlokalizowaną wiadomością z klucza `error.account.already.blocked`.
* **Odwołanie transakcji**: Tak

#### 5.7 `AccountIsNotBlockedException`
* **Miejsca zgłoszenia**: `AccountService.unblockUserAccount()`, `AccountService.unblockAccountViaEmailToken()`
* **Klucz internacjonalizacji**: `error.account.not.blocked`
* **Kod błędu HTTP**: 409
* **Możliwe przyczyny wystąpienia**: Operacja ściągnięcia blokady awaryjnej dla konta, które nie ma takiej blokady.
* **Sposób obsługi**: Wyjątek jest przechwytywany przez globalny handler w adnotowanej metodzie `GlobalAppExceptionHandler.handleAppBaseException()`, która zwraca odpowiedź HTTP 409 ze zlokalizowaną wiadomością z klucza `error.account.not.blocked`.
* **Odwołanie transakcji**: Tak

#### 5.8 `AccountIsNotVerifiedException`
* **Miejsca zgłoszenia**: `AuthService.authenticate()`, `AuthService.resend2FactorCode()`, `AuthService.authenticateOnlyWithEmail()`
* **Klucz internacjonalizacji**: `error.account.not.verified`
* **Kod błędu HTTP**: 409
* **Możliwe przyczyny wystąpienia**: Wywołanie żądania o kod i procedury uwierzytelniania w procesie, gdy samo konto nie zdążyło przejść przez walidację początkowego tokenu aktywacyjnego linku wysłanego po procesie rejestracji.
* **Sposób obsługi**: Wyjątek jest przechwytywany przez globalny handler w adnotowanej metodzie `GlobalAppExceptionHandler.handleAppBaseException()`, która zwraca odpowiedź HTTP 409 ze zlokalizowaną wiadomością z klucza `error.account.not.verified`.
* **Odwołanie transakcji**: Tak

#### 5.9 `ConcurrentUpdateException`
* **Miejsca zgłoszenia**: Przechwytywany i zgłaszany przez interceptor `GenericOptimisticLockHandlingInterceptor.handleOptimisticLockException()`, a także rzucany jawnie w metodach biznesowych: `AccountService.grantAccessLevel()`, `AccountService.revokeAccessLevel()`, `AccountService.changeEmailByAdmin()`, `AccountService.changeOwnPassword()`, `AccountService.blockUserAccount()`, `AccountService.unblockUserAccount()`, `AccountService.updateAccount()`, `AccountService.updateAccountByLogin()`, `AccountService.changeOtherPassword()`, `RuleService.updateRulePreset()`, `RuleService.deleteRulePreset()`, `ScheduleService.deleteSchedule()`, `SubjectService.updateSubject()`, `SubjectService.deleteSubject()`, `SubjectService.changeSubjectManager()`
* **Klucz internacjonalizacji**: `error.concurrent.update`
* **Kod błędu HTTP**: 409
* **Możliwe przyczyny wystąpienia**: Wyjątek występuje w dwóch scenariuszach blokady optymistycznej:
    1. Niezgodność wartości nagłówka `versionHash` z poświadczeniami przekazanymi z aplikacji klienta względem stanu w bazie danych (ręczna weryfikacja w logice biznesowej).
    2. Przechwycenie wywołania wyjątku systemowego `OptimisticLockingFailureException` rzucanego przez mechanizm JPA/Hibernate podczas zapisu danych, w sytuacji, gdy doszło do wyścigu wątków.
* **Sposób obsługi**: Wyjątek jest przechwytywany przez globalny handler w adnotowanej metodzie `GlobalAppExceptionHandler.handleAppBaseException()`, która zwraca odpowiedź HTTP 409 ze zlokalizowaną wiadomością z klucza `error.concurrent.update`.
* **Odwołanie transakcji**: Tak

#### 5.10 `OneTimeTokenInvalidException`
* **Miejsca zgłoszenia**: `PasswordResetService.resolvePasswordReset()`, `AuthService.verify2FactorCode()`, `AuthService.verifyEmailOnlyAuthCode()`, `AccountService.confirmOwnEmailChange()`, `AccountService.revertEmailChange()`, `AccountService.unblockAccountViaEmailToken()`
* **Klucz internacjonalizacji**: `error.token.jwt.expired`
* **Kod błędu HTTP**: 409
* **Możliwe przyczyny wystąpienia**: Token jednorazowy zdefiniowany w bazie stał się nieważny w wyniku utraty ważności i przedawnienia w systemie kontroli, bądź został z flagą uaktywnioną już w ramach przeszłej weryfikacji.
* **Sposób obsługi**: Wyjątek jest przechwytywany przez globalny handler w adnotowanej metodzie `GlobalAppExceptionHandler.handleAppBaseException()`, która zwraca odpowiedź HTTP 409 ze zlokalizowaną wiadomością z odpowiedniego klucza.
* **Odwołanie transakcji**: Tak (Wyjątkiem są tutaj metody `AuthService.verify2FactorCode` i `verifyEmailOnlyAuthCode`, u których adnotacja wyłącza transakcję w celu aktualizacji licznika prób dla `Account`).

#### 5.11 `PasswordAlreadyUsedInPastException`
* **Miejsca zgłoszenia**: `AccountService.changeOwnPassword()`, `AccountService.changeOtherPassword()`, `PasswordResetService.resolvePasswordReset()`
* **Klucz internacjonalizacji**: `error.password.already.used`
* **Kod błędu HTTP**: 409
* **Możliwe przyczyny wystąpienia**: Skrypt weryfikacji tabeli wykazuje wystąpienie nowego odcisku zmienianego hasła na osi historii `PasswordHistory`.
* **Sposób obsługi**: Wyjątek jest przechwytywany przez globalny handler w adnotowanej metodzie `GlobalAppExceptionHandler.handleAppBaseException()`, która zwraca odpowiedź HTTP 409 ze zlokalizowaną wiadomością z klucza `error.password.already.used`.
* **Odwołanie transakcji**: Tak

#### 5.12 `PasswordSameAsOldException`
* **Miejsca zgłoszenia**: `AccountService.changeOwnPassword()`, `AccountService.changeOtherPassword()`, `PasswordResetService.resolvePasswordReset()`
* **Klucz internacjonalizacji**: `error.password.same.as.old`
* **Kod błędu HTTP**: 409
* **Możliwe przyczyny wystąpienia**: Hash wprowadzonego w prośbie nowego hasła jest dokładnie jednoznaczny z poświadczeniami używanymi i przechowywanymi aktywnie na profilu w danej sekundzie.
* **Sposób obsługi**: Wyjątek jest przechwytywany przez globalny handler w adnotowanej metodzie `GlobalAppExceptionHandler.handleAppBaseException()`, która zwraca odpowiedź HTTP 409 ze zlokalizowaną wiadomością z klucza `error.password.same.as.old`.
* **Odwołanie transakcji**: Tak

#### 5.13 `Resend2FATokenException`
* **Miejsca zgłoszenia**: `AuthService.generate2FactorCode()`, `AuthService.generateEmailOnlyAuthCode()`, `AuthService.invalidateActiveToken()`
* **Klucz internacjonalizacji**: `Resend 2FA Token Time Exceeded`
* **Kod błędu HTTP**: 409
* **Możliwe przyczyny wystąpienia**: Zapobieganie spamowaniu kanałów komunikacji. Klient zainicjował prośbę ponownego generowania autoryzacji bez wymaganego opóźnienia względem ubiegłego wywołania zdarzenia w interwale.
* **Sposób obsługi**: Wyjątek jest przechwytywany przez globalny handler w adnotowanej metodzie `GlobalAppExceptionHandler.handleAppBaseException()`, która zwraca odpowiedź HTTP 409 z bezpośrednim komunikatem z klucza lub tekstem konstruktora.
* **Odwołanie transakcji**: Tak

#### Wyjątki serwerowe (500 Internal Server Error)

#### 6.1 `HashEncryptionException`
* **Miejsca zgłoszenia**: `VersionHashService.hashObjectVersion()`, `PasswordResetService.hashToken()`, `AuthService.hashToken()`
* **Klucz internacjonalizacji**: `error.jgit.problem`
* **Kod błędu HTTP**: 500
* **Możliwe przyczyny wystąpienia**: Poważny wewnętrzny awaryjny problem mechaniki klonowania i łączenia zdalnych repozytoriów z użyciem natywnych rozwiązań platformy Eclipse JGit.
* **Sposób obsługi**: Wyjątek jest przechwytywany przez globalny handler w adnotowanej metodzie `GlobalAppExceptionHandler.handleAppBaseException()`, która zwraca odpowiedź HTTP 500 ze zlokalizowaną wiadomością z klucza `error.jgit.problem`.
* **Odwołanie transakcji**: Tak

#### 6.2 `JplagScanException`
* **Miejsca zgłoszenia**: `JPlagService.runPlagiarismScanOnOrganisation()`
* **Klucz internacjonalizacji**: `error.jplag.scan`
* **Kod błędu HTTP**: 500
* **Możliwe przyczyny wystąpienia**: Skrypty wywołujące zewnętrzną zależność (JPlag) ulegają błędom awarii jądra przy przeliczaniu algorytmiki z powodu np. niewspieranych znaków, wyłączenia stosu lub innych kryzysów środowiska wirtualnego Javy.
* **Sposób obsługi**: Wyjątek jest przechwytywany przez globalny handler w adnotowanej metodzie `GlobalAppExceptionHandler.handleAppBaseException()`, która zwraca odpowiedź HTTP 500 ze zlokalizowaną wiadomością z klucza `error.jplag.scan`.
* **Odwołanie transakcji**: Tak

#### 6.3 `LanguageDictNotFoundException`
* **Miejsca zgłoszenia**: `AccountService.registerAccount()`, `AccountService.changeAccountLanguage()`, `SubjectService.createBaseSubject()`, `SubjectService.updateSubject()`, `SubjectDescriptionTranslationService.translateSubjectDescription()`
* **Klucz internacjonalizacji**: `error.language.dict.not.found`
* **Kod błędu HTTP**: 500
* **Możliwe przyczyny wystąpienia**: Próba utworzenia encji profilu z podstawowym językiem systemowym nieskutkuje zapytaniem, ponieważ domyślny język usunięto ze struktury słownika i występuje naruszenie logiczne konfiguracji systemowej.
* **Sposób obsługi**: Wyjątek jest przechwytywany przez globalny handler w adnotowanej metodzie `GlobalAppExceptionHandler.handleAppBaseException()`, która zwraca odpowiedź HTTP 500 ze zlokalizowaną wiadomością z klucza `error.language.dict.not.found`.
* **Odwołanie transakcji**: Tak

#### 6.4 `ReportFileException`
* **Miejsca zgłoszenia**: `JPlagService.runPlagiarismScanOnOrganisation()`, `JplagReportParser.parseFullTeacherReport()`
* **Klucz internacjonalizacji**: `error.report.file`
* **Kod błędu HTTP**: 500
* **Możliwe przyczyny wystąpienia**: Niespodziewany błąd operacji zapisu i usuwania fizycznych bloków danych pamięci magazynowej (brak miejsca na serwerze, zmiana w atrybutach chmod zabezpieczeń folderów podrzędnych dla generowanych archiwów z plagiatów).
* **Sposób obsługi**: Wyjątek jest przechwytywany przez globalny handler w adnotowanej metodzie `GlobalAppExceptionHandler.handleAppBaseException()`, która zwraca odpowiedź HTTP 500 ze zlokalizowaną wiadomością z klucza `error.report.file`.
* **Odwołanie transakcji**: Tak

#### Wyjątki braku połączenia z zintegrowaną aplikacją (502 Bad Gateway)

#### 7.1 `GiteaIntegrationException`
* **Miejsca zgłoszenia**: `GiteaService.getRepositoriesFromOrganization()`, `GiteaService.getRepoCollaborators()`, `GiteaService.deleteGiteaOrganization()`, `SubjectService.deleteSubject()`
* **Klucz internacjonalizacji**: `error.subject.gitea.integration`
* **Kod błędu HTTP**: 502
* **Możliwe przyczyny wystąpienia**: Serwer nie był w stanie przetworzyć żądania przez brak łączności RestClienta z zewnętrzną usługą instalacji Gitea, przerwane połączenie bądź błędy parsowania odpowiedzi serwerów.
* **Sposób obsługi**: Wyjątek jest przechwytywany przez globalny handler w adnotowanej metodzie `GlobalAppExceptionHandler.handleAppBaseException()`, która zwraca odpowiedź HTTP 502 ze zlokalizowaną wiadomością z klucza `error.subject.gitea.integration`.
* **Odwołanie transakcji**: Tak

### 7. Przykładowy kod mechanizmu formy obsługi wyjątków aplikacyjnych

```java
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import pl.lodz.p.it.ssbd2026.ssbd01.exceptions.AppBaseException;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalAppExceptionHandler {

    @ExceptionHandler(AppBaseException.class)
    public ResponseEntity<Object> handleAppBaseException(AppBaseException ex) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", ex.getStatusCode().value());
        body.put("error", ex.getReason());
        
        return new ResponseEntity<>(body, ex.getStatusCode());
    }
}
```

### 8. Wyjątki systemowe

W przypadku wyjątków bezpośrednio wywoływanych jako reakcja obronna w architekturze Spring Core (bazy JPA, adnotacje bezpieczeństwa @PreAuthorize, logiki wstrzykiwań i tranzakcji), zastosowano specjalne mapowania nadpisane we wbudowanych klasach systemowych.

#### 8.1 `AccessDeniedException`
* **Miejsca zgłoszenia**: Konteksty adnotacji `@PreAuthorize`, zgłaszany na poziomie AOP kontenera w obrębie wszystkich metod biznesowych, dla których wyznaczono role (np. `hasAuthority`).
* **Klucz internacjonalizacji**: `error.access.denied`
* **Kod błędu HTTP**: 403
* **Możliwe przyczyny wystąpienia**: Spring Security wywołuje kontekst SecurityContext dla połączenia i odkrywa brak właściwej listy GrantedAuthority wewnątrz załadowanych tokenów JWT.
* **Sposób obsługi**: Zmapowany do czytelnej struktury JSON poprzez klasę `GlobalExceptionHandler`.
* **Odwołanie transakcji**: Tak

#### 8.2 `ObjectOptimisticLockingFailureException`
* **Miejsca zgłoszenia**: Systemowa faza persystencji danych z warstwy JPA i Hibernate.
* **Klucz internacjonalizacji**: `error.concurrent.update`
* **Kod błędu HTTP**: 409
* **Możliwe przyczyny wystąpienia**: Rzucany w przypadku konfliktów wersji podczas nadpisywania rekordów. Kontener zauważył, że `@Version` pola encji bazy danych nie jest zgodne z przekazanym bytem po transakcji.
* **Sposób obsługi**: Zmapowany do czytelnej struktury JSON poprzez klasę `GlobalExceptionHandler`.
* **Odwołanie transakcji**: Tak

#### 8.3 `TransactionSystemException`
* **Miejsca zgłoszenia**: Każda wycofana transakcja aplikacyjna na warstwie bazy danych naruszająca spójność, powodująca wycofanie kontenerowe bazy (`rollback`).
* **Klucz internacjonalizacji**: `error.transaction.rollback`
* **Kod błędu HTTP**: 500
* **Możliwe przyczyny wystąpienia**: Skutek nieobsłużonego, wyjątku we wnętrzu bloku z `@Transactional` przerywający proces commit-u danych przez menadżer połączeń.
* **Sposób obsługi**: Przechwyt w module globalnym dla kontenerów chroniącym dane aplikacji pod spodem.
* **Odwołanie transakcji**: Tak.

### 9. Przykładowy kod mechanizmu formy obsługi wyjątków systemowych

```java
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalSystemExceptionHandler {

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Object> handleAccessDeniedException(AccessDeniedException ex) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.FORBIDDEN.value());
        body.put("error", "error.access.denied");

        return new ResponseEntity<>(body, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler({ObjectOptimisticLockingFailureException.class})
    public ResponseEntity<Object> handleOptimisticLockingFailure(ObjectOptimisticLockingFailureException ex) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.CONFLICT.value());
        body.put("error", "error.concurrent.update");

        return new ResponseEntity<>(body, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(TransactionSystemException.class)
    public ResponseEntity<Object> handleTransactionSystemException(TransactionSystemException ex) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        body.put("error", "error.transaction.rollback");

        return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
```

### 10. Segment opakowujący wyjątek systemowy blokady optymistycznej na własny wyjątek aplikacyjny

Zgodnie z założeniami architektury, systemowe błędy blokady optymistycznej zgłaszane przez repozytoria Spring Data JPA (`OptimisticLockingFailureException`) są przechwytywane przez dedykowany Interceptor AOP i opakowywane na aplikacyjny wyjątek `ConcurrentUpdateException`.

```java
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Component;
import pl.lodz.p.it.ssbd2026.ssbd01.exceptions.ConcurrentUpdateException;

@Aspect
@Order(Ordered.LOWEST_PRECEDENCE - 100)
@Component
public class GenericOptimisticLockHandlingInterceptor {

    @AfterThrowing(pointcut = "Pointcuts.allRepositoryMethods()", throwing = "olfe")
    public void handleOptimisticLockException(JoinPoint joinPoint, OptimisticLockingFailureException olfe) {
        throw new ConcurrentUpdateException(olfe);
    }
}
```