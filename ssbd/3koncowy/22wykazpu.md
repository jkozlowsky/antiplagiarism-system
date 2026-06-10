
## 22 Wykaz akcji dostępnych z interfejsu użytkownika 4 pkt

W rozdziale należy wymienić, w formie tabeli lub listy, wszystkie przypadki użycia dostępne jako akcje dla użytkownika końcowego aplikacji internetowej.

Dla każdego przypadku należy przedstawić:
* kolejny numer przypadku w numeracji jednolitej od numeru 1 (np. poprzez użycie listy numerowanej)
* używane w projekcie oznaczenie przypadku (np. MOK.3)
* role mające prawo do uruchamiania akcji dla tego przypadku zgodnie z konfiguracją dostępu w warstwie prezentacji
* kontekst URI oraz typ metody HTTP dla wywołania usługi sieciowej REST wraz z wartością liczbową statusu odpowiedzi (kod odpowiedzi HTTP) dla poszczególnych scenariuszy przypadku użycia (dla każdego ze scenariuszy błędu należy również podać klucz internacjonalizacji).
* sekwencję nazw wywoływanych metod oraz obiektów (w tym wywoływane metody akcji, wywołania usług, wywołania metod komponentów).
* wszystkie możliwe zmiany widoku z interfejsu użytkownika w ramach wykonywania przypadku użycia - w formie zrzutów ekranu uwidaczniających kolejne zmiany interfejsu użytkownika dla polskiej wersji językowej.

Alternatywną formą prezentacji może być opis interfejsu aplikacji sieciowej zgodny z OpenAPI, przekształcony do postaci czytelnej i uzupełniony o informacje, których nie można udostępnić przez OpenAPI, tj:
* sekwencję nazw wywoływanych metod oraz obiektów (w tym wywoływane metody akcji, wywołania usług, wywołania metod komponentów).
* wszystkie możliwe zmiany widoku z interfejsu użytkownika w ramach wykonywania przypadku użycia - w formie zrzutów ekranu uwidaczniających kolejne zmiany interfejsu użytkownika dla polskiej wersji językowej.

W takim przypadku opis może być zapisany w języku angielskim, o ile ten język został użyty w projekcie do tworzenia dokumentacji kodu źródłowego.

### MOK.6 - Dołącz poziom dostępu do konta

**Role uprawnione:** Administrator (`ADMIN`)

```json
    "/account/{id}/access-levels/{accessLevelName}/grant": {
      "post": {
        "tags": [
          "Konta"
        ],
        "summary": "Przyznaj poziom dostępu",
        "description": "Nadaje wybranemu kontu konkretny poziom dostępu. Wymaga nagłówka If-Match.",
        "operationId": "grantAccessLevel",
        "parameters": [
          {
            "name": "id",
            "in": "path",
            "required": true,
            "schema": {
              "type": "string",
              "format": "uuid"
            }
          },
          {
            "name": "accessLevelName",
            "in": "path",
            "required": true,
            "schema": {
              "type": "string"
            }
          },
          {
            "name": "If-Match",
            "in": "header",
            "required": true,
            "schema": {
              "type": "string"
            }
          }
        ],
        "responses": {
          "400": {
            "description": "Błąd walidacji wejścia",
            "content": {
              "application/json": {
                "schema": {
                  "$ref": "#/components/schemas/ValidationErrorResponse"
                }
              }
            }
          },
          "200": {
            "description": "Poziom dostępu został pomyślnie nadany",
            "content": {
              "application/json": {
                "schema": {
                  "$ref": "#/components/schemas/AccountWithAccessLevelsDTO"
                }
              }
            }
          },
          "404": {
            "description": "Brak zasobu",
            "content": {
              "text/plain": {
                "example": "Konto lub poziom dostępu nie istnieje."
              }
            }
          },
          "409": {
            "description": "Konflikt",
            "content": {
              "text/plain": {
                "example": "Konto posiada już ten poziom dostępu."
              }
            }
          },
          "401": {
            "description": "Brak autoryzacji (nieprawidłowy token JWT)",
            "content": {
              "application/json": {
                "example": {
                  "error": "Unauthorized",
                  "message": "Brak lub niepoprawny token autoryzacyjny."
                }
              }
            }
          },
          "403": {
            "description": "Odmowa dostępu (brak roli)",
            "content": {
              "text/plain": {
                "example": "No access: Access Denied"
              }
            }
          },
          "500": {
            "description": "Wewnętrzny błąd serwera",
            "content": {
              "text/plain": {
                "example": "Exception: Wystąpił nieoczekiwany błąd serwera"
              }
            }
          },
          "503": {
            "description": "Niedostępność usługi bazy danych",
            "content": {
              "text/plain": {
                "example": "Service is temporarily unavailable. Please try again later."
              }
            }
          }
        }
      }
    }
```

**Sekwencja wywołań:**
1. AccountService.grantAccessLevel(UUID, String, String)  (pl.lodz.p.it.ssbd2026.ssbd01.mok.services)
2. AccountRepository.findById(UUID)  (pl.lodz.p.it.ssbd2026.ssbd01.mok.repositories)
3. AccountNotFoundException.AccountNotFoundException()  (pl.lodz.p.it.ssbd2026.ssbd01.exceptions)
4. AppBaseException.AppBaseException(HttpStatusCode, String)  (pl.lodz.p.it.ssbd2026.ssbd01.exceptions)
5. MokRefreshRepository.refresh(AbstractEntity)  (pl.lodz.p.it.ssbd2026.ssbd01.mok.repositories)
6. AbstractEntity.getVersion()(3 usages)  (pl.lodz.p.it.ssbd2026.ssbd01.entities)
7. VersionHashService.hashObjectVersion(UUID, Integer)(3 usages)  (pl.lodz.p.it.ssbd2026.ssbd01.mok.services)
8. VersionHashService.getStringFromByteArray(byte[])  (pl.lodz.p.it.ssbd2026.ssbd01.mok.services)
9. HashEncryptionException.HashEncryptionException(String, Throwable)  (pl.lodz.p.it.ssbd2026.ssbd01.exceptions)
10. ConcurrentUpdateException.ConcurrentUpdateException()  (pl.lodz.p.it.ssbd2026.ssbd01.exceptions)
11. AppBaseException.AppBaseException(HttpStatusCode, String)  (pl.lodz.p.it.ssbd2026.ssbd01.exceptions)
12. AccessLevelDictRepository.findByAccessLevelName(String)  (pl.lodz.p.it.ssbd2026.ssbd01.mok.repositories)
13. AccessLevelDictNotFoundException.AccessLevelDictNotFoundException()(2 usages)  (pl.lodz.p.it.ssbd2026.ssbd01.exceptions.accesslevel)
14. AppBaseException.AppBaseException(HttpStatusCode, String)  (pl.lodz.p.it.ssbd2026.ssbd01.exceptions)
15. Account.getAccessLevelList()(2 usages)  (pl.lodz.p.it.ssbd2026.ssbd01.entities)
16. AccessLevel.getAccessLevelDict()(2 usages)  (pl.lodz.p.it.ssbd2026.ssbd01.entities)
17. AccessLevelDict.getId()(2 usages)  (pl.lodz.p.it.ssbd2026.ssbd01.entities)
18. AccessLevel.getActive()  (pl.lodz.p.it.ssbd2026.ssbd01.entities)
19. AccessLevelAlreadyAssignedException.AccessLevelAlreadyAssignedException()  (pl.lodz.p.it.ssbd2026.ssbd01.exceptions.accesslevel)
20. AppBaseException.AppBaseException(HttpStatusCode, String)  (pl.lodz.p.it.ssbd2026.ssbd01.exceptions)
21. AccessLevel.setActive(Boolean)(2 usages)  (pl.lodz.p.it.ssbd2026.ssbd01.entities)
22. JpaRepository.saveAndFlush(S)(2 usages)  (org.springframework.data.jpa.repository)
23. SimpleJpaRepository.saveAndFlush(S)  (org.springframework.data.jpa.repository.support)
24. Account.getEmail()(2 usages)  (pl.lodz.p.it.ssbd2026.ssbd01.entities)
25. Account.getLogin()(2 usages)  (pl.lodz.p.it.ssbd2026.ssbd01.entities)
26. Account.getLanguageVersion()(2 usages)  (pl.lodz.p.it.ssbd2026.ssbd01.entities)
27. LanguageDict.getLanguage()(2 usages)  (pl.lodz.p.it.ssbd2026.ssbd01.entities)
28. EmailService.sendAccessLevelGrantedInfo(String, String, String, String)(2 usages)  (pl.lodz.p.it.ssbd2026.ssbd01.mok.services)
29. Translator.translateForUser(String, String, Object...)(2 usages)  (pl.lodz.p.it.ssbd2026.ssbd01.config.i18n)
30. EmailService.sendEmail(String, String, String)  (pl.lodz.p.it.ssbd2026.ssbd01.mok.services)
31. PostCommitTransactionManager.register(Runnable)(2 usages)  (pl.lodz.p.it.ssbd2026.ssbd01.common.utils)
32. AbstractEntity.getId()(2 usages)  (pl.lodz.p.it.ssbd2026.ssbd01.entities)
33. AccountMapper.toAccountWithAccessLevelsAndVersionHashDTO(Account, String)(2 usages)  (pl.lodz.p.it.ssbd2026.ssbd01.dto.converters)
34. Admin.Admin()  (pl.lodz.p.it.ssbd2026.ssbd01.entities)
35. AccessLevel.AccessLevel()  (pl.lodz.p.it.ssbd2026.ssbd01.entities)
36. Student.Student()  (pl.lodz.p.it.ssbd2026.ssbd01.entities)
37. AccessLevel.AccessLevel()  (pl.lodz.p.it.ssbd2026.ssbd01.entities)
38. Teacher.Teacher()  (pl.lodz.p.it.ssbd2026.ssbd01.entities)
39. AccessLevel.AccessLevel()  (pl.lodz.p.it.ssbd2026.ssbd01.entities)
40. AccessLevel.setAccessLevelDict(AccessLevelDict)  (pl.lodz.p.it.ssbd2026.ssbd01.entities)
41. AccessLevel.setAccount(Account)  (pl.lodz.p.it.ssbd2026.ssbd01.entities)

**Fragmenty graficznego interfejsu użytkownika:**

TODO (rysunki):

![Rys. 22.6.1](img/mok6_account_details.png)
*Rys. 22.6.1. Fragment graficznego interfejsu użytkownika przedstawiający widok szczegółów konta z dostępnymi poziomami dostępu.*

![Rys. 22.6.2](img/mok6_grant_success.png)
*Rys. 22.6.2. Fragment graficznego interfejsu użytkownika przedstawiający widok po pomyślnym nadaniu poziomu dostępu.*

![Rys. 22.6.3](img/mok6_grant_already_assigned.png)
*Rys. 22.6.3. Fragment graficznego interfejsu użytkownika przedstawiający błąd przy próbie nadania poziomu dostępu, który jest już przypisany do konta.*

### MOK.7 - Odłącz poziom dostępu od konta

```json
    "/account/{id}/access-levels/{accessLevelName}/revoke": {
      "post": {
        "tags": [
          "Konta"
        ],
        "summary": "Odbierz poziom dostępu",
        "description": "Odbiera wybranemu kontu konkretny poziom dostępu (rolę). Wymaga nagłówka If-Match.",
        "operationId": "revokeAccessLevel",
        "parameters": [
          {
            "name": "id",
            "in": "path",
            "required": true,
            "schema": {
              "type": "string",
              "format": "uuid"
            }
          },
          {
            "name": "accessLevelName",
            "in": "path",
            "required": true,
            "schema": {
              "type": "string"
            }
          },
          {
            "name": "If-Match",
            "in": "header",
            "required": true,
            "schema": {
              "type": "string"
            }
          }
        ],
        "responses": {
          "400": {
            "description": "Błąd walidacji wejścia",
            "content": {
              "application/json": {
                "schema": {
                  "$ref": "#/components/schemas/ValidationErrorResponse"
                }
              }
            }
          },
          "200": {
            "description": "Poziom dostępu został pomyślnie odebrany",
            "content": {
              "application/json": {
                "schema": {
                  "$ref": "#/components/schemas/AccountWithAccessLevelsDTO"
                }
              }
            }
          },
          "404": {
            "description": "Brak zasobu",
            "content": {
              "text/plain": {
                "example": "Konto lub poziom dostępu nie istnieje."
              }
            }
          },
          "409": {
            "description": "Konflikt",
            "content": {
              "text/plain": {
                "example": "Konto nie posiadało tego poziomu dostępu."
              }
            }
          },
          "401": {
            "description": "Brak autoryzacji (nieprawidłowy token JWT)",
            "content": {
              "application/json": {
                "example": {
                  "error": "Unauthorized",
                  "message": "Brak lub niepoprawny token autoryzacyjny."
                }
              }
            }
          },
          "403": {
            "description": "Odmowa dostępu (brak roli)",
            "content": {
              "text/plain": {
                "example": "No access: Access Denied"
              }
            }
          },
          "500": {
            "description": "Wewnętrzny błąd serwera",
            "content": {
              "text/plain": {
                "example": "Exception: Wystąpił nieoczekiwany błąd serwera"
              }
            }
          },
          "503": {
            "description": "Niedostępność usługi bazy danych",
            "content": {
              "text/plain": {
                "example": "Service is temporarily unavailable. Please try again later."
              }
            }
          }
        }
      }
    }
```

**Sekwencja wywołań:**
1. AccountService.revokeAccessLevel(UUID, String, String)  (pl.lodz.p.it.ssbd2026.ssbd01.mok.services)
2. AccountRepository.findById(UUID)  (pl.lodz.p.it.ssbd2026.ssbd01.mok.repositories)
3. AccountNotFoundException.AccountNotFoundException()  (pl.lodz.p.it.ssbd2026.ssbd01.exceptions)
4. MokRefreshRepository.refresh(AbstractEntity)  (pl.lodz.p.it.ssbd2026.ssbd01.mok.repositories)
5. AbstractEntity.getVersion()  (pl.lodz.p.it.ssbd2026.ssbd01.entities)
6. VersionHashService.hashObjectVersion(UUID, Integer)  (pl.lodz.p.it.ssbd2026.ssbd01.mok.services)
7. ConcurrentUpdateException.ConcurrentUpdateException()  (pl.lodz.p.it.ssbd2026.ssbd01.exceptions)
8. AccessLevelDictRepository.findByAccessLevelName(String)  (pl.lodz.p.it.ssbd2026.ssbd01.mok.repositories)
9. AccessLevelDictNotFoundException.AccessLevelDictNotFoundException()  (pl.lodz.p.it.ssbd2026.ssbd01.exceptions.accesslevel)
10. Account.getAccessLevelList()  (pl.lodz.p.it.ssbd2026.ssbd01.entities)
11. AccessLevel.getAccessLevelDict()(2 usages)  (pl.lodz.p.it.ssbd2026.ssbd01.entities)
12. AccessLevelDict.getId()(2 usages)  (pl.lodz.p.it.ssbd2026.ssbd01.entities)
13. AccessLevelNotAssignedException.AccessLevelNotAssignedException()  (pl.lodz.p.it.ssbd2026.ssbd01.exceptions.accesslevel)
14. AccessLevel.getActive()  (pl.lodz.p.it.ssbd2026.ssbd01.entities)
15. AccessLevelAlreadyRevokedException.AccessLevelAlreadyRevokedException()  (pl.lodz.p.it.ssbd2026.ssbd01.exceptions.accesslevel)
16. AccessLevel.setActive(Boolean)  (pl.lodz.p.it.ssbd2026.ssbd01.entities)
17. JpaRepository.saveAndFlush(S)  (org.springframework.data.jpa.repository)
18. Account.getEmail()  (pl.lodz.p.it.ssbd2026.ssbd01.entities)
19. Account.getLogin()  (pl.lodz.p.it.ssbd2026.ssbd01.entities)
20. Account.getLanguageVersion()  (pl.lodz.p.it.ssbd2026.ssbd01.entities)
21. LanguageDict.getLanguage()  (pl.lodz.p.it.ssbd2026.ssbd01.entities)
22. EmailService.sendAccessLevelRevokedInfo(String, String, String, String)  (pl.lodz.p.it.ssbd2026.ssbd01.mok.services)
23. PostCommitTransactionManager.register(Runnable)  (pl.lodz.p.it.ssbd2026.ssbd01.common.utils)
24. AccountService.getAccountWithAccessLevels(UUID)  (pl.lodz.p.it.ssbd2026.ssbd01.mok.services)
25. AccountRepository.findById(UUID)  (pl.lodz.p.it.ssbd2026.ssbd01.mok.repositories)
26. AccountNotFoundException.AccountNotFoundException()  (pl.lodz.p.it.ssbd2026.ssbd01.exceptions)
27. Account.getAccessLevelList()  (pl.lodz.p.it.ssbd2026.ssbd01.entities)
28. AccessLevel.getAccessLevelDict()(2 usages)  (pl.lodz.p.it.ssbd2026.ssbd01.entities)
29. AccessLevelDict.getAccessLevelName()  (pl.lodz.p.it.ssbd2026.ssbd01.entities)
30. AbstractEntity.getId()  (pl.lodz.p.it.ssbd2026.ssbd01.entities)
31. AbstractEntity.getVersion()  (pl.lodz.p.it.ssbd2026.ssbd01.entities)
32. VersionHashService.hashObjectVersion(UUID, Integer)  (pl.lodz.p.it.ssbd2026.ssbd01.mok.services)
33. VersionHashService.getStringFromByteArray(byte[])  (pl.lodz.p.it.ssbd2026.ssbd01.mok.services)
34. HashEncryptionException.HashEncryptionException(String, Throwable)  (pl.lodz.p.it.ssbd2026.ssbd01.exceptions)
35. AccountMapper.toAccountWithAccessLevelsAndVersionHashDTO(Account, String)  (pl.lodz.p.it.ssbd2026.ssbd01.dto.converters)

**Fragmenty graficznego interfejsu użytkownika:**

TODO (rysunki):

![Rys. 22.7.1](img/mok7_account_details.png)
*Rys. 22.7.1. Fragment graficznego interfejsu użytkownika przedstawiający widok szczegółów konta z aktywnymi poziomami dostępu.*

![Rys. 22.7.2](img/mok7_revoke_success.png)
*Rys. 22.7.2. Fragment graficznego interfejsu użytkownika przedstawiający widok po pomyślnym odebraniu poziomu dostępu.*

![Rys. 22.7.3](img/mok7_revoke_already_revoked.png)
*Rys. 22.7.3. Fragment graficznego interfejsu użytkownika przedstawiający błąd przy próbie odebrania poziomu dostępu, który jest już nieaktywny.*

### MOK.15 - Wyświetl listę użytkowników

**Role uprawnione:** Administrator (`ADMIN`)

```json
    "/account": {
      "get": {
        "tags": [
          "Konta"
        ],
        "summary": "Pobierz wszystkie konta",
        "description": "Pobiera stronicowaną listę wszystkich kont, opcjonalnie filtrowaną po frazie.",
        "operationId": "getAllAccounts",
        "parameters": [
          {
            "name": "page",
            "in": "query",
            "required": false,
            "schema": {
              "type": "integer",
              "format": "int32"
            }
          },
          {
            "name": "size",
            "in": "query",
            "required": false,
            "schema": {
              "type": "integer",
              "format": "int32"
            }
          },
          {
            "name": "sortBy",
            "in": "query",
            "required": false,
            "schema": {
              "type": "string"
            }
          },
          {
            "name": "sortDesc",
            "in": "query",
            "required": false,
            "schema": {
              "type": "boolean"
            }
          },
          {
            "name": "phrase",
            "in": "query",
            "required": false,
            "schema": {
              "type": "string"
            }
          },
          {
            "name": "pagedResourcesAssembler",
            "in": "query",
            "required": true,
            "schema": {
              "$ref": "#/components/schemas/PagedResourcesAssemblerAccountWithAccessLevelsDTO"
            }
          }
        ],
        "responses": {
          "400": {
            "description": "Błąd walidacji wejścia",
            "content": {
              "application/json": {
                "schema": {
                  "$ref": "#/components/schemas/ValidationErrorResponse"
                }
              }
            }
          },
          "200": {
            "description": "Poprawnie pobrano stronicowaną listę kont z hiperłączami",
            "content": {
              "application/json": {
                "schema": {
                  "$ref": "#/components/schemas/PagedModelEntityModelAccountWithAccessLevelsDTO"
                }
              }
            }
          },
          "404": {
            "description": "Nie znaleziono zasobu",
            "content": {
              "text/plain": {
                "example": "Nie znaleziono konta administratora wykonującego zapytanie."
              }
            }
          },
          "409": {
            "description": "Błąd blokady optymistycznej",
            "content": {
              "text/plain": {
                "example": "Konto zostało zmodyfikowane w międzyczasie, spróbuj ponownie."
              }
            }
          },
          "401": {
            "description": "Brak autoryzacji (nieprawidłowy token JWT)",
            "content": {
              "application/json": {
                "example": {
                  "error": "Unauthorized",
                  "message": "Brak lub niepoprawny token autoryzacyjny."
                }
              }
            }
          },
          "403": {
            "description": "Odmowa dostępu (brak roli)",
            "content": {
              "text/plain": {
                "example": "No access: Access Denied"
              }
            }
          },
          "500": {
            "description": "Wewnętrzny błąd serwera",
            "content": {
              "text/plain": {
                "example": "Exception: Wystąpił nieoczekiwany błąd serwera"
              }
            }
          },
          "503": {
            "description": "Niedostępność usługi bazy danych",
            "content": {
              "text/plain": {
                "example": "Service is temporarily unavailable. Please try again later."
              }
            }
          }
        }
      }
    }
```

**Sekwencja wywołań:**
1. AccountService.getAllAccounts(Integer, Integer, String, Boolean, String)  (pl.lodz.p.it.ssbd2026.ssbd01.mok.services)
2. AccountRepository.findByLogin(String)  (pl.lodz.p.it.ssbd2026.ssbd01.mok.repositories)
3. AccountNotFoundException.AccountNotFoundException()  (pl.lodz.p.it.ssbd2026.ssbd01.exceptions)
4. AccountService.updatePreferencesIfProvided(Account, Integer, String, Boolean)  (pl.lodz.p.it.ssbd2026.ssbd01.mok.services)
5. AccountService.createPageable(Integer, Account)  (pl.lodz.p.it.ssbd2026.ssbd01.mok.services)
6. AccountRepository.findAll(Pageable)  (pl.lodz.p.it.ssbd2026.ssbd01.mok.repositories)
7. AccountService.searchAccounts(String, Integer, Integer, String, Boolean, String)  (pl.lodz.p.it.ssbd2026.ssbd01.mok.services)
8. AccountRepository.findByLogin(String)  (pl.lodz.p.it.ssbd2026.ssbd01.mok.repositories)
9. AccountNotFoundException.AccountNotFoundException()  (pl.lodz.p.it.ssbd2026.ssbd01.exceptions)
10. AccountService.updatePreferencesIfProvided(Account, Integer, String, Boolean)  (pl.lodz.p.it.ssbd2026.ssbd01.mok.services)
11. AccountService.createPageable(Integer, Account)  (pl.lodz.p.it.ssbd2026.ssbd01.mok.services)
12. AccountRepository.findByNameContainingIgnoreCaseOrSurnameContainingIgnoreCase(String, String, Pageable)  (pl.lodz.p.it.ssbd2026.ssbd01.mok.repositories)
13. AbstractEntity.getId()  (pl.lodz.p.it.ssbd2026.ssbd01.entities)
14. AbstractEntity.getVersion()  (pl.lodz.p.it.ssbd2026.ssbd01.entities)
15. VersionHashService.hashObjectVersion(UUID, Integer)  (pl.lodz.p.it.ssbd2026.ssbd01.mok.services)
16. VersionHashService.getStringFromByteArray(byte[])  (pl.lodz.p.it.ssbd2026.ssbd01.mok.services)
17. HashEncryptionException.HashEncryptionException(String, Throwable)  (pl.lodz.p.it.ssbd2026.ssbd01.exceptions)
18. AccountMapper.toAccountWithAccessLevelsAndVersionHashDTO(Account, String)  (pl.lodz.p.it.ssbd2026.ssbd01.dto.converters)

**Fragmenty graficznego interfejsu użytkownika:**

TODO (rysunki):

![Rys. 22.15.1](img/mok15_accounts_list.png)
*Rys. 22.15.1. Fragment graficznego interfejsu użytkownika przedstawiający widok listy wszystkich użytkowników.*

![Rys. 22.15.2](img/mok15_accounts_search.png)
*Rys. 22.15.2. Fragment graficznego interfejsu użytkownika przedstawiający widok listy użytkowników z zastosowanym filtrowaniem po frazie.*

![Rys. 22.15.3](img/mok15_accounts_sorted.png)
*Rys. 22.15.3. Fragment graficznego interfejsu użytkownika przedstawiający widok listy użytkowników z zastosowanym sortowaniem.*

### MPP.10 - Przeglądanie reguł

**Role uprawnione:** Nauczyciel (`TEACHER`)

```json
    "/rules/templates": {
      "get": {
        "tags": [
          "rule-controller"
        ],
        "summary": "Pobierz listę szablonów reguł",
        "description": "Pobiera listę wszystkich dostępnych szablonów reguł.",
        "operationId": "getRulePresetsTemplates",
        "responses": {
          "400": {
            "description": "Błąd walidacji wejścia",
            "content": {
              "application/json": {
                "schema": {
                  "$ref": "#/components/schemas/ValidationErrorResponse"
                }
              }
            }
          },
          "200": {
            "description": "Lista szablonów pobrana pomyślnie",
            "content": {
              "application/json": {
                "schema": {
                  "type": "array",
                  "items": {
                    "$ref": "#/components/schemas/RulePresetDTO"
                  }
                }
              }
            }
          },
          "401": {
            "description": "Brak autoryzacji (nieprawidłowy token JWT)",
            "content": {
              "application/json": {
                "example": {
                  "error": "Unauthorized",
                  "message": "Brak lub niepoprawny token autoryzacyjny."
                }
              }
            }
          },
          "403": {
            "description": "Odmowa dostępu (brak roli)",
            "content": {
              "text/plain": {
                "example": "No access: Access Denied"
              }
            }
          },
          "500": {
            "description": "Wewnętrzny błąd serwera",
            "content": {
              "text/plain": {
                "example": "Exception: Wystąpił nieoczekiwany błąd serwera"
              }
            }
          },
          "503": {
            "description": "Niedostępność usługi bazy danych",
            "content": {
              "text/plain": {
                "example": "Service is temporarily unavailable. Please try again later."
              }
            }
          }
        }
      }
    }
```

**Sekwencja wywołań:**
1. RuleService.getRulePresetsTemplates()  (pl.lodz.p.it.ssbd2026.ssbd01.mpp.services)
2. RuleRepository.findAllByIsPreset(Boolean)  (pl.lodz.p.it.ssbd2026.ssbd01.mpp.repositories)
3. RuleMapper.toRuleDTO(RulePreset)  (pl.lodz.p.it.ssbd2026.ssbd01.dto.converters)
4. RulePresetDTO.id()  (pl.lodz.p.it.ssbd2026.ssbd01.dto.mpp)
5. RulePresetDTO.raportLevelName()  (pl.lodz.p.it.ssbd2026.ssbd01.dto.mpp)
6. RulePresetDTO.studentTicketCount()  (pl.lodz.p.it.ssbd2026.ssbd01.dto.mpp)
7. RulePresetDTO.minimumTokensMatch()  (pl.lodz.p.it.ssbd2026.ssbd01.dto.mpp)
8. RulePresetDTO.enableNormalization()  (pl.lodz.p.it.ssbd2026.ssbd01.dto.mpp)
9. RulePresetDTO.version()  (pl.lodz.p.it.ssbd2026.ssbd01.dto.mpp)
10. AbstractEntity.getId()  (pl.lodz.p.it.ssbd2026.ssbd01.entities)
11. AbstractEntity.getVersion()  (pl.lodz.p.it.ssbd2026.ssbd01.entities)
12. VersionHashService.hashObjectVersion(UUID, Integer)  (pl.lodz.p.it.ssbd2026.ssbd01.mok.services)
13. VersionHashService.getStringFromByteArray(byte[])  (pl.lodz.p.it.ssbd2026.ssbd01.mok.services)
14. HashEncryptionException.HashEncryptionException(String, Throwable)  (pl.lodz.p.it.ssbd2026.ssbd01.exceptions)
15. RulePresetDTO.RulePresetDTO(UUID, String, Integer, Integer, Boolean, Integer, String)  (pl.lodz.p.it.ssbd2026.ssbd01.dto.mpp)

**Fragmenty graficznego interfejsu użytkownika:**

![Rys. 22.10.1](img/mpp10_rules_list.png)
*Rys. 22.10.1. Fragment graficznego interfejsu użytkownika przedstawiający widok listy szablonów reguł.*

### MPP.11 - Tworzenie reguł

**Role uprawnione:** Nauczyciel (`TEACHER`)

```json
    "/rules/templates": {
      "post": {
        "tags": [
          "rule-controller"
        ],
        "summary": "Utworzenie predefiniowanego szablonu reguł",
        "description": "Tworzy nowy, globalny szablon konfiguracji reguł, który może być później wykorzystany jako podstawa dla przedmiotów.",
        "operationId": "createRulePresetTemplate",
        "requestBody": {
          "content": {
            "application/json": {
              "schema": {
                "$ref": "#/components/schemas/CreateRulePresetDTO"
              }
            }
          },
          "required": true
        },
        "responses": {
          "400": {
            "description": "Błąd walidacji wejścia",
            "content": {
              "application/json": {
                "schema": {
                  "$ref": "#/components/schemas/ValidationErrorResponse"
                }
              }
            }
          },
          "200": {
            "description": "Szablon reguł został pomyślnie utworzony"
          },
          "404": {
            "description": "Nie znaleziono słownika dla poziomu raportów"
          },
          "401": {
            "description": "Brak autoryzacji (nieprawidłowy token JWT)",
            "content": {
              "application/json": {
                "example": {
                  "error": "Unauthorized",
                  "message": "Brak lub niepoprawny token autoryzacyjny."
                }
              }
            }
          },
          "403": {
            "description": "Odmowa dostępu (brak roli)",
            "content": {
              "text/plain": {
                "example": "No access: Access Denied"
              }
            }
          },
          "500": {
            "description": "Wewnętrzny błąd serwera",
            "content": {
              "text/plain": {
                "example": "Exception: Wystąpił nieoczekiwany błąd serwera"
              }
            }
          },
          "503": {
            "description": "Niedostępność usługi bazy danych",
            "content": {
              "text/plain": {
                "example": "Service is temporarily unavailable. Please try again later."
              }
            }
          }
        }
      }
    }
  }
```

**Sekwencja wywołań:**

1. RuleService.createRulePreset(CreateRulePresetDTO)  (pl.lodz.p.it.ssbd2026.ssbd01.mpp.services)
2. RulePreset.RulePreset()  (pl.lodz.p.it.ssbd2026.ssbd01.entities)
3. AbstractEntity.AbstractEntity()  (pl.lodz.p.it.ssbd2026.ssbd01.entities)
4. RulePreset.setIsPreset(Boolean)  (pl.lodz.p.it.ssbd2026.ssbd01.entities)
5. RulePreset.setSubject(Subject)  (pl.lodz.p.it.ssbd2026.ssbd01.entities)
6. CreateRulePresetDTO.raportLevelName()(2 usages)  (pl.lodz.p.it.ssbd2026.ssbd01.dto.mpp)
7. CreateRulePresetDTO.studentTicketCount()(2 usages)  (pl.lodz.p.it.ssbd2026.ssbd01.dto.mpp)
8. CreateRulePresetDTO.minimumTokensMatch()(2 usages)  (pl.lodz.p.it.ssbd2026.ssbd01.dto.mpp)
9. CreateRulePresetDTO.enableNormalization()(2 usages)  (pl.lodz.p.it.ssbd2026.ssbd01.dto.mpp)
10. RulePresetInvalidPartametersException.RulePresetInvalidPartametersException()  (pl.lodz.p.it.ssbd2026.ssbd01.exceptions.rulePreset)
11. AppBaseException.AppBaseException(HttpStatusCode, String)  (pl.lodz.p.it.ssbd2026.ssbd01.exceptions)
12. RaportLevelDictRepository.findByName(String)  (pl.lodz.p.it.ssbd2026.ssbd01.mpp.repositories)
13. RaportLevelDictNotFoundException.RaportLevelDictNotFoundException()  (pl.lodz.p.it.ssbd2026.ssbd01.exceptions.raportLevel)
14. AppBaseException.AppBaseException(HttpStatusCode, String)  (pl.lodz.p.it.ssbd2026.ssbd01.exceptions)
15. RulePreset.setRaportLevel(ReportLevelDict)  (pl.lodz.p.it.ssbd2026.ssbd01.entities)
16. RulePreset.setStudentTicketCount(Integer)  (pl.lodz.p.it.ssbd2026.ssbd01.entities)
17. RulePreset.setMinimumTokensMatch(Integer)  (pl.lodz.p.it.ssbd2026.ssbd01.entities)
18. RulePreset.setEnableNormalization(Boolean)  (pl.lodz.p.it.ssbd2026.ssbd01.entities)

**Fragmenty graficznego interfejsu użytkownika:**

![Rys. 22.11.1](img/mpp11_create_rule_form.png)
*Rys. 22.11.1. Fragment graficznego interfejsu użytkownika przedstawiający formularz tworzenia nowego szablonu reguł.*

![Rys. 22.11.2](img/mpp11_create_rule_success.png)
*Rys. 22.11.2. Fragment graficznego interfejsu użytkownika przedstawiający widok po pomyślnym utworzeniu szablonu reguł.*

![Rys. 22.11.3](img/mpp11_create_rule_invalid.png)
*Rys. 22.11.3. Fragment graficznego interfejsu użytkownika przedstawiający błąd walidacji przy niepoprawnych danych formularza tworzenia szablonu reguł.*

### MPP.12 - Edycja reguł

**Role uprawnione:** Nauczyciel (`TEACHER`)

```json
    "/rules/{ruleId}": {
      "put": {
        "tags": [
          "rule-controller"
        ],
        "summary": "Aktualizuj regułę",
        "description": "Modyfikuje istniejącą regułę na podstawie przekazanego ID oraz nowych danych. Wymaga uprawnień nauczyciela.",
        "operationId": "updateRulePreset",
        "parameters": [
          {
            "name": "ruleId",
            "in": "path",
            "required": true,
            "schema": {
              "type": "string",
              "format": "uuid"
            }
          },
          {
            "name": "If-Match",
            "in": "header",
            "required": true,
            "schema": {
              "type": "string"
            }
          }
        ],
        "requestBody": {
          "content": {
            "application/json": {
              "schema": {
                "$ref": "#/components/schemas/UpdateRulePresetDTO"
              }
            }
          },
          "required": true
        },
        "responses": {
          "400": {
            "description": "Błąd walidacji wejścia",
            "content": {
              "application/json": {
                "schema": {
                  "$ref": "#/components/schemas/ValidationErrorResponse"
                }
              }
            }
          },
          "200": {
            "description": "Reguła zaktualizowana pomyślnie"
          },
          "404": {
            "description": "Nie znaleziono reguły o podanym ID lub wskazany poziom raportu nie istnieje",
            "content": {
              "text/plain": {

              }
            }
          },
          "409": {
            "description": "Dane zostały zmodyfikowane przez innego użytkownika",
            "content": {
              "text/plain": {

              }
            }
          },
          "401": {
            "description": "Brak autoryzacji (nieprawidłowy token JWT)",
            "content": {
              "application/json": {
                "example": {
                  "error": "Unauthorized",
                  "message": "Brak lub niepoprawny token autoryzacyjny."
                }
              }
            }
          },
          "403": {
            "description": "Odmowa dostępu (brak roli)",
            "content": {
              "text/plain": {
                "example": "No access: Access Denied"
              }
            }
          },
          "500": {
            "description": "Wewnętrzny błąd serwera",
            "content": {
              "text/plain": {
                "example": "Exception: Wystąpił nieoczekiwany błąd serwera"
              }
            }
          },
          "503": {
            "description": "Niedostępność usługi bazy danych",
            "content": {
              "text/plain": {
                "example": "Service is temporarily unavailable. Please try again later."
              }
            }
          }
        }
      }
    }
```

**Sekwencja wywołań:**

1. RuleService.updateRulePreset(UUID, UpdateRulePresetDTO, String)  (pl.lodz.p.it.ssbd2026.ssbd01.mpp.services)
2. RulePresetNotFoundException.RulePresetNotFoundException()  (pl.lodz.p.it.ssbd2026.ssbd01.exceptions.rulePreset)
3. AppBaseException.AppBaseException(HttpStatusCode, String)  (pl.lodz.p.it.ssbd2026.ssbd01.exceptions)
4. AbstractEntity.getVersion()  (pl.lodz.p.it.ssbd2026.ssbd01.entities)
5. VersionHashService.hashObjectVersion(UUID, Integer)  (pl.lodz.p.it.ssbd2026.ssbd01.mok.services)
6. VersionHashService.getStringFromByteArray(byte[])  (pl.lodz.p.it.ssbd2026.ssbd01.mok.services)
7. HashEncryptionException.HashEncryptionException(String, Throwable)  (pl.lodz.p.it.ssbd2026.ssbd01.exceptions)
8. ConcurrentUpdateException.ConcurrentUpdateException()  (pl.lodz.p.it.ssbd2026.ssbd01.exceptions)
9. AppBaseException.AppBaseException(HttpStatusCode, String)  (pl.lodz.p.it.ssbd2026.ssbd01.exceptions)
10. UpdateRulePresetDTO.raportLevelName()  (pl.lodz.p.it.ssbd2026.ssbd01.dto.mpp)
11. RaportLevelDictRepository.findByName(String)  (pl.lodz.p.it.ssbd2026.ssbd01.mpp.repositories)
12. RaportLevelDictNotFoundException.RaportLevelDictNotFoundException()  (pl.lodz.p.it.ssbd2026.ssbd01.exceptions.raportLevel)
13. AppBaseException.AppBaseException(HttpStatusCode, String)  (pl.lodz.p.it.ssbd2026.ssbd01.exceptions)
14. UpdateRulePresetDTO.studentTicketCount()  (pl.lodz.p.it.ssbd2026.ssbd01.dto.mpp)
15. RulePreset.setStudentTicketCount(Integer)  (pl.lodz.p.it.ssbd2026.ssbd01.entities)
16. UpdateRulePresetDTO.minimumTokensMatch()  (pl.lodz.p.it.ssbd2026.ssbd01.dto.mpp)
17. RulePreset.setMinimumTokensMatch(Integer)  (pl.lodz.p.it.ssbd2026.ssbd01.entities)
18. UpdateRulePresetDTO.enableNormalization()  (pl.lodz.p.it.ssbd2026.ssbd01.dto.mpp)
19. RulePreset.setEnableNormalization(Boolean)  (pl.lodz.p.it.ssbd2026.ssbd01.entities)
20. RulePreset.setRaportLevel(ReportLevelDict)  (pl.lodz.p.it.ssbd2026.ssbd01.entities)
21. JpaRepository.saveAndFlush(S)  (org.springframework.data.jpa.repository)
22. SimpleJpaRepository.saveAndFlush(S)  (org.springframework.data.jpa.repository.support)

**Fragmenty graficznego interfejsu użytkownika:**

![Rys. 22.12.1](img/mpp12_edit_rule_form.png)
*Rys. 22.12.1. Fragment graficznego interfejsu użytkownika przedstawiający formularz edycji szablonu reguł.*

![Rys. 22.12.2](img/mpp12_edit_rule_success.png)
*Rys. 22.12.2. Fragment graficznego interfejsu użytkownika przedstawiający widok po pomyślnej aktualizacji szablonu reguł.*

![Rys. 22.12.3](img/mpp12_edit_rule_invalid.png)
*Rys. 22.12.3. Fragment graficznego interfejsu użytkownika przedstawiający błąd walidacji przy niepoprawnych danych formularza edycji szablonu reguł.*

### MPP.17 - Usuwanie harmonogramu

**Role uprawnione:** Nauczyciel (`TEACHER`)

```json
      "delete": {
        "tags": [
          "schedule-controller"
        ],
        "summary": "Usuwanie harmonogramu",
        "description": "Prowadzący posiadający uprawnienia edycji może usunąć istniejący harmonogram.",
        "operationId": "deleteSchedule",
        "parameters": [
          {
            "name": "scheduleId",
            "in": "path",
            "required": true,
            "schema": {
              "type": "string",
              "format": "uuid"
            }
          },
          {
            "name": "If-Match",
            "in": "header",
            "required": true,
            "schema": {
              "type": "string"
            }
          }
        ],
        "responses": {
          "400": {
            "description": "Błąd walidacji wejścia",
            "content": {
              "application/json": {
                "schema": {
                  "$ref": "#/components/schemas/ValidationErrorResponse"
                }
              }
            }
          },
          "204": {
            "description": "Harmonogram został pomyślnie usunięty"
          },
          "401": {
            "description": "Brak autoryzacji (nieprawidłowy token JWT)",
            "content": {
              "application/json": {
                "example": {
                  "error": "Unauthorized",
                  "message": "Brak lub niepoprawny token autoryzacyjny."
                }
              }
            }
          },
          "403": {
            "description": "Odmowa dostępu (brak roli)",
            "content": {
              "text/plain": {
                "example": "No access: Access Denied"
              }
            }
          },
          "404": {
            "description": "Nie znaleziono harmonogramu",
            "content": {
              "text/plain": {

              }
            }
          },
          "409": {
            "description": "Błąd wersji",
            "content": {
              "text/plain": {

              }
            }
          },
          "500": {
            "description": "Wewnętrzny błąd serwera",
            "content": {
              "text/plain": {
                "example": "Exception: Wystąpił nieoczekiwany błąd serwera"
              }
            }
          },
          "503": {
            "description": "Niedostępność usługi bazy danych",
            "content": {
              "text/plain": {
                "example": "Service is temporarily unavailable. Please try again later."
              }
            }
          }
        }
      }
```

**Sekwencja wywołań:**
1. ScheduleService.deleteSchedule(UUID, String, String)  (pl.lodz.p.it.ssbd2026.ssbd01.mpp.services)
2. ScheduleNotFoundException.ScheduleNotFoundException()  (pl.lodz.p.it.ssbd2026.ssbd01.exceptions)
3. AppBaseException.AppBaseException(HttpStatusCode, String)  (pl.lodz.p.it.ssbd2026.ssbd01.exceptions)
4. AbstractEntity.getVersion()  (pl.lodz.p.it.ssbd2026.ssbd01.entities)
5. VersionHashService.hashObjectVersion(UUID, Integer)  (pl.lodz.p.it.ssbd2026.ssbd01.mok.services)
6. VersionHashService.getStringFromByteArray(byte[])  (pl.lodz.p.it.ssbd2026.ssbd01.mok.services)
7. HashEncryptionException.HashEncryptionException(String, Throwable)  (pl.lodz.p.it.ssbd2026.ssbd01.exceptions)
8. ConcurrentUpdateException.ConcurrentUpdateException()  (pl.lodz.p.it.ssbd2026.ssbd01.exceptions)
9. AppBaseException.AppBaseException(HttpStatusCode, String)  (pl.lodz.p.it.ssbd2026.ssbd01.exceptions)
10. Schedule.getRulePreset()  (pl.lodz.p.it.ssbd2026.ssbd01.entities)
11. RulePreset.getSubject()  (pl.lodz.p.it.ssbd2026.ssbd01.entities)
12. SubjectAssignmentRepository.hasEditPermissions(Subject, String)  (pl.lodz.p.it.ssbd2026.ssbd01.mpp.repositories)
13. NoPermissionToEditScheduleException.NoPermissionToEditScheduleException()  (pl.lodz.p.it.ssbd2026.ssbd01.exceptions)
14. AppBaseException.AppBaseException(HttpStatusCode, String)  (pl.lodz.p.it.ssbd2026.ssbd01.exceptions)

**Fragmenty graficznego interfejsu użytkownika:**

![Rys. 22.17.1](img/mpp17_schedule_list.png)
*Rys. 22.17.1. Fragment graficznego interfejsu użytkownika przedstawiający listę harmonogramów z opcją usunięcia.*

![Rys. 22.17.2](img/mpp17_delete_confirm.png)
*Rys. 22.17.2. Fragment graficznego interfejsu użytkownika przedstawiający dialog potwierdzenia usunięcia harmonogramu.*

![Rys. 22.17.3](img/mpp17_delete_success.png)
*Rys. 22.17.3. Fragment graficznego interfejsu użytkownika przedstawiający widok po pomyślnym usunięciu harmonogramu.*