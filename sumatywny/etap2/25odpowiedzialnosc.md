
## 2.5. Problem odpowiedzialności i zdarzeń niepożądanych


### Dwa główne przypadki użycia, które są dostępne bez uwierzytelnienia (dla użytkowników anonimowych):


* **Rejestracja kont użytkowników** – Atakujący może masowo wysyłać żądania pod endpoint `/account/register`, używając fałszywych lub losowych adresów e-mail oraz danych osobowych. Ponieważ akcja ta jest dostępna publicznie, atakujący pozostaje anonimowy. Skutkiem jest niepotrzebne obciążenie bazy danych (tabela **account**), zużycie zasobów serwera pocztowego (masowa wysyłka maili aktywacyjnych) oraz potencjalne wyczerpanie puli dostępnych połączeń.


* **Żądanie resetowania hasła** – Atakujący może cyklicznie wywoływać publiczny endpoint `/auth/reset/request` dla istniejących w bazie lub losowych adresów e-mail. Powoduje to generowanie i zapisywanie w bazie jednorazowych tokenów (tabela **one_time_token**) oraz spamowanie skrzynek pocztowych użytkowników wiadomościami z linkiem resetującym.


### Rozwiązania ograniczające nadużycia (dla powyższych przypadków):


* **Automatyczne usuwanie niezweryfikowanych kont (AccountScheduler)** – System cyklicznie uruchamia metodę `processInactiveAccounts()`, która wyszukuje konta niezweryfikowane (pole `isVerified = false`). Konto, które nie zostanie aktywowane w określonym czasie od momentu utworzenia (**24 godziny**), jest bezpowrotnie usuwane z bazy danych. Dodatkowo mechanizm ten po upływie połowy tego czasu wysyła przypomnienie o aktywacji, dbając o autentycznych użytkowników.


* **Zarządzanie wygasłymi żetonami jednorazowymi (TokenScheduler)** – Zarówno tokeny służące do resetowania hasła, jak i do dwuetapowej weryfikacji (2FA) czy zmiany adresu e-mail, mają krótki czas życia (np. 30 minut dla resetu hasła zdefiniowane w `OneTimeTokenConsts.EXPIRATION_TIME_MINUTES`). Metoda `DeleteExpiredTokens()` uruchamia się cyklicznie i fizycznie usuwa z bazy danych wszystkie rekordy z tabeli **one_time_token**, których czas ważności (z pola `expiresAt`) minął.


![Zrzut ekranu z bazy danych - tokeny](etap2/img/2.5.1.png)
Rysunek 2.5.1. Widok tabeli **one_time_token** w bazie danych, prezentujący mechanizm zarządzania żetonami. Widoczne są dwa kody autoryzacji dwuetapowej (2FA) wygenerowane dla tego samego użytkownika (cyber123). Starszy z nich został automatycznie i prewencyjnie oznaczony jako zużyty (`is_used = true`) w momencie wygenerowania nowego żądania, co uniemożliwia jego ponowne wykorzystanie.


* **Unieważnianie poprzednich żądań resetu hasła** – W klasie **PasswordResetService** przy inicjowaniu nowego żądania resetu hasła, system wyszukuje wszystkie dotychczasowe, nieużyte tokeny przypisane do danego adresu e-mail i automatycznie oznacza je jako zużyte (`isUsed = true`). Ogranicza to możliwość kumulowania aktywnych tokenów przez jednego atakującego.


### Identyfikacja użytkowników w innych przypadkach


W przypadku operacji wymagających uwierzytelnienia (wykonywanych przez zalogowanych użytkowników), system implementuje mechanizmy pozwalające ustalić odpowiedzialność za wprowadzane zmiany:


* **Audytowanie encji bazodanowych** (JPA Auditing):
Każda encja w systemie (np. `Account`, `Subject`, `Report`) dziedziczy po klasie bazowej `AbstractEntity`. Klasa ta wykorzystuje mechanizm `@EntityListeners(AuditingEntityListener.class)`, który automatycznie uzupełnia metadane przy każdej operacji zapisu i modyfikacji w bazie. Zapisywane są:
  * Czas utworzenia (`createdAt`) i modyfikacji (`updatedAt`).
  * Tożsamość użytkownika wykonującego akcję: referencje do konta twórcy (`@CreatedBy createdBy`) oraz konta ostatnio modyfikującego (`@LastModifiedBy modifiedBy`).
  * Dzięki temu każda zmiana danych biznesowych jest trwale przypisana do konkretnego konta w systemie.


* **Rejestrowanie zdarzeń i parametrów** (AOP Logging Interceptors):
Aktywność użytkownika jest rejestrowana na podstawie szczegółowych zapisów w dzienniku zdarzeń (logach). Za ich tworzenie odpowiada Programowanie Zorientowane Aspektowo (AOP), zaimplementowane u nas w projekcie.
  *  `MethodCallLoggingInterceptor` przechwytuje wywołania metod oznaczonych adnotacją `@MethodCallLogged` lub metod z klas oznaczonych tak samo. W logach zapisywana jest sygnatura metody, przekazane argumenty, wartości zwracane, ewentualne wyjątki oraz login zalogowanego użytkownika, z którego kontekstu (Spring Security `SecurityContextHolder`) pochodzi wywołanie. Dla zachowania bezpieczeństwa interceptor filtruje i maskuje argumenty wrażliwe (np. hasła).
  * `TransactionLoggingInterceptor` monitoruje cykl życia transakcji bazodanowych, co pozwala na śledzenie, czy zmiany zainicjowane przez użytkownika zostały faktycznie zatwierdzone (**COMMITTED**), czy odrzucone (**ROLLED_BACK**).


![Zrzut ekranu z dziennika zdarzeń](etap2/img/2.5.2.png)
Rysunek 2.5.2. Przykładowy wykaz wpisów w dzienniku zdarzeń generowany przez intercepotory AOP. Zarejestrowano tu akcję zmiany hasła zainicjowaną przez zalogowanego użytkownika (rbartczak05). Widoczna jest skuteczna filtracja wrażliwych argumentów wejściowych (hasła zastąpione ciągiem `*** UKRYTE ***`) oraz śledzenie statusu synchronizacji transakcji bazodanowej aż do momentu jej pomyślnego zatwierdzenia (**COMMITTED**).


* **Śledzenie adresów IP i blokady prewencyjne**:
Podczas każdej próby logowania (zarówno udanej, jak i nieudanej), w encji `Account` zapisywany jest **adres IP** nadawcy (`lastSuccessfulLoginIp`, `lastFailedLoginIp`) oraz data zdarzenia. Ponadto trzykrotne wprowadzenie błędnego hasła skutkuje automatyczną blokadą konta (`isBlocked = true`) i wysłaniem powiadomienia e-mail (Zabezpieczenie przed włamaniem na konto metodą Brute Force).

  
![Zrzut ekranu z bazy danych - zablokowane konto](etap2/img/2.5.3.png)
Rysunek 2.5.3. Widok rekordu w tabeli account ilustrujący działanie mechanizmu zapobiegającego atakom typu Brute Force. Zauważalny jest użytkownik cyber123, dla którego system odnotował trzy błędne próby autoryzacji (wartość 3 w kolumnie `failed_attempts`). Doprowadziło to do natychmiastowej, prewencyjnej blokady profilu (zmiana flagi `is_blocked` na `true`) oraz zapisania dokładnego czasu wystąpienia incydentu.