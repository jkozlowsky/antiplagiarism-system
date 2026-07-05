## 7. Model kont i uwierzytelnianie w aplikacji 1 pkt

**Model kont**

System opiera się na centralnej tabeli `Account` (przechowującej dane uwierzytelniające i personalne) oraz tabeli
`AccessLevel` (przechowującej poziomy dostępu). Ponieważ encje dziedziczą po `AbstractEntity`, wykorzystują
identyfikatory typu UUID. Jeden użytkownik może posiadać wiele poziomów dostępu, co zostało zaprezentowane na poniższym
przykładzie, gdzie użytkownik Jan Kowalski posiada jednocześnie uprawnienia Nauczyciela i Administratora. Przykładowe hasło użytkowników w poniższym przykładzie to: B0rnt0f!ght.

**Tabela 1.7.1. Przykładowe dane bazodanowe dla tabeli `Account`.**

| id                                   | login     | email                 | password_hash                                                               | name | surname  | language_version | is_blocked |
| :----------------------------------- | :-------- | :-------------------- | :-------------------------------------------------------------------------- | :--- | :------- | :--------------- | :--------- |
| 1666fd2f-2ffc-4634-9f18-c31f9adf3302 | jkowalski | j.kowalski@p.lodz.pl  | \$2a\$08\$OzuLgJt8<br>GiUAw6xZE9.8v.l/<br>9I2DUtF8fhYKrnuUHAxP<br>.pFEsGmde | Jan  | Kowalski | PL               | false      |
| 1666fd2f-2ffc-4634-9f18-c31f9adf3303 | anowak    | a.nowak@edu.p.lodz.pl | \$2a\$08\$OzuLgJt8<br>GiUAw6xZE9.8v.l/<br>9I2DUtF8fhYKrnuUHAxP<br>.pFEsGmde | Anna | Nowak    | PL               | false      |
| 1666fd2f-2ffc-4634-9f18-c31f9adf3304 | admin     | admin@ssbd.edu.pl     | \$2a\$08\$OzuLgJt8<br>GiUAw6xZE9.8v.l/<br>9I2DUtF8fhYKrnuUHAxP<br>.pFEsGmde | Adam | Root     | PL               | false      |


**Tabela 1.7.2. Przykładowe dane bazodanowe dla tabeli `access_level`.**

| id                                   | account_id (FK)                      | access_level_id (FK)                 |
| :----------------------------------- | :----------------------------------- | ------------------------------------ |
| 1666fd2f-2ffc-4634-9f18-c31f9adf3306 | 1666fd2f-2ffc-4634-9f18-c31f9adf3302 | a4987efc-2395-445d-86e0-b24c1e2ef4c1 |
| 1666fd2f-2ffc-4634-9f18-c31f9adf3307 | 1666fd2f-2ffc-4634-9f18-c31f9adf3302 | 822ae20e-80c4-439c-b4e9-dd629c505197 |
| 1666fd2f-2ffc-4634-9f18-c31f9adf3308 | 1666fd2f-2ffc-4634-9f18-c31f9adf3303 | 925e219e-78c8-4951-92c3-783a8077cf3c |
| 1666fd2f-2ffc-4634-9f18-c31f9adf3309 | 1666fd2f-2ffc-4634-9f18-c31f9adf3304 | a4987efc-2395-445d-86e0-b24c1e2ef4c1 |

**Tabela 1.7.3. Dane w tabeli słownikowej `access_level_dict`.**

| id                                   | access_level_name |
| ------------------------------------ | :---------------- |
| a4987efc-2395-445d-86e0-b24c1e2ef4c1 | Admin             |
| 822ae20e-80c4-439c-b4e9-dd629c505197 | Teacher           |
| 925e219e-78c8-4951-92c3-783a8077cf3c | Student           |


---

**Procedury zarządzania kontami**

Zarządzanie cyklem życia konta opiera się na przejściach między jego stanami. Każda z poniższych operacji bezpośrednio
modyfikuje krotki w relacyjnej bazie danych:

1. **Rejestracja konta:**
    * Akcja realizowana tylko i wyłącznie przez gościa. Polega na wypełnieniu formularza rejestracyjnego: imię,
      nazwisko, adres email, login, hasło.
    * **Zmiana w bazie danych:** Operacja `INSERT` tworząca nową krotkę w tabeli `Account`. Pola `id` (generowane UUID),
      `version` (początkowo wartość 0/1), `login`, `email`, `passwordHash`, `name`, `surname` są uzupełniane danymi, a pole `is_blocked`
      otrzymuje domyślnie wartość `true`. Równolegle system może wykonać `INSERT` do tabeli `AccessLevel` (oraz
      odpowiednich tabel dziedziczących np. `Student`, `Prowadzący`), przypisując domyślny poziom dostępu (student lub prowadzący w zależności od danych osoby, która loguje się do systemu). Pola dat logowań (`lastLoginSuccessDate`, `lastLoginFailedDate`, `lastSuccessfulLoginIP`, `lastFailedLoginIP`) pozostają
      puste (NULL), a `languageVersion` ustawia się na domyślny język przeglądarki użytkownika.

2. **Weryfikacja konta po rejestracji samodzielnej:**
    * System weryfikuje poprawność poprzez poprawne wprowadzenie sześciocyfrowego kodu wysłanego na mail użytkownika
      zaraz po wysłaniu formularza rejestracyjnego.
    * **Zmiana w bazie danych:** Operacja `UPDATE` na odpowiedniej krotce w tabeli `Account` (wyszukanej po powiązanym z
      kontem kodzie/tokenie). Pole `is_blocked` zmienia wartość z `true` na `false`. Zwiększana o 1 jest wartość pola
      `version`.

3. **Usuwanie nieaktywnych kont po upływie limitu czasu:**
    * Zadanie wykonywane automatycznie przez system w tle, czyszczące bazę z kont, które nie zostały zweryfikowane.
    * **Zmiana w bazie danych:** Operacja `DELETE` usuwająca powiązane krotki z tabeli `AccessLevel` (w tym tabele
      dziedziczące), a następnie krotki z tabeli `Account`, dla których `is_blocked` wynosi `true` i minął zdefiniowany
      limit czasu (24 godziny od zarejestrowania konta).

4. **Blokowanie i odblokowywanie konta:**
    * Blokowanie może nastąpić **automatycznie** (w przypadku przekroczenia limitu błędnych logowań) lub **ręcznie**
      poprzez jawną akcję Administratora. Odblokowanie zawsze wymaga
      interwencji Administratora.
    * **Zmiana w bazie danych (Blokowanie konta):** Operacja `UPDATE` na krotce w tabeli `Account`. Pole `is_blocked` zmienia
      się z `false` na `true`. W przypadku automatycznej blokady konta aktualizowane jest pole `lastLoginFailedDate`, z odpowiednim komunikatem w dzienniku zdarzeń.
    * **Zmiana w bazie danych (Odblokowywanie konta):** Operacja `UPDATE` inicjowana przez Admina. Pole `is_blocked` zmienia
      się z powrotem na `false`.

5. **Przydzielanie i odbieranie poziomów dostępu:**
    * Realizowane przez Administratora w panelu zarządzania.
    * **Zmiana w bazie danych (Dołącz poziom dostępu):** Wykonanie operacji `INSERT` nowej krotki do tabeli
      `AccessLevel` (oraz odpowiedniej tabeli dziedziczącej, np. `Teacher`), zawierającej odpowiednią wartość
      `accessLevelName` oraz powiązanie z `id` wybranego użytkownika (`account_id`).
    * **Zmiana w bazie danych (Odłącz poziom dostępu):** Wykonanie operacji `DELETE` na krotce w tabeli
      `AccessLevel`, łączącej danego użytkownika z konkretnym poziomem dostępu. Użytkownik natychmiast traci uprawnienia
      z nim związane.