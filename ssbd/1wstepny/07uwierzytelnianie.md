## 7. Model kont i uwierzytelnianie w aplikacji 1 pkt

**Model kont**

System opiera się na centralnej tabeli `Account` (przechowującej dane uwierzytelniające i personalne) oraz tabeli
`AccessLevel` (przechowującej poziomy dostępu). Ponieważ encje dziedziczą po `AbstractEntity`, wykorzystują
identyfikatory typu UUID. Jeden użytkownik może posiadać wiele poziomów dostępu, co zostało zaprezentowane na poniższym
przykładzie, gdzie użytkownik **Jan Kowalski posiada jednocześnie uprawnienia Nauczyciela i Administratora**. Przykładowe hasło użytkowników w poniższym przykładzie to: B0rnt0f!ght.

**Tabela: `Account`**

| id (UUID) | version | login       | email                     | passwordHash                                                    | name  | surname    | languageVersion | status*     |
|:----------|:--------|:------------|:--------------------------|:----------------------------------------------------------------|:------|:-----------|:----------------|:------------|
| 1         | 1       | jkowalski   | j.kowalski@p.lodz.pl      | \$2a\$08\$OzuLgJt8GiUAw6xZE9.8v.l/9I2DUtF8fhYKrnuUHAxP.pFEsGmde | Jan   | Kowalski   | PL              | Aktywne     |
| 2         | 1       | anowak      | a.nowak@edu.p.lodz.pl     | \$2a\$08\$OzuLgJt8GiUAw6xZE9.8v.l/9I2DUtF8fhYKrnuUHAxP.pFEsGmde | Anna  | Nowak      | EN              | Aktywne     |
| 3         | 0       | ppawlowski  | p.pawlowski@edu.p.lodz.pl | \$2a\$08\$OzuLgJt8GiUAw6xZE9.8v.l/9I2DUtF8fhYKrnuUHAxP.pFEsGmde | Piotr | Pawłowski  | PL              | Nieaktywne  |
| 4         | 2       | awisniewski | a.wisniewski@p.lodz.pl    | \$2a\$08\$OzuLgJt8GiUAw6xZE9.8v.l/9I2DUtF8fhYKrnuUHAxP.pFEsGmde | Adam  | Wiśniewski | PL              | Zablokowane |

**Tabela: `AccessLevel`**

| id (UUID) | account_id (FK) | accessLevelName |
|:----------|:----------------|:----------------|
| 1         | 1               | Admin           |
| 2         | 1               | Teacher         |
| 3         | 2               | Student         |
| 4         | 3               | Student         |
| 5         | 4               | Teacher         |

---

**Procedury zarządzania kontami**

Zarządzanie cyklem życia konta opiera się na przejściach między jego stanami. Każda z poniższych operacji bezpośrednio
modyfikuje krotki w relacyjnej bazie danych:

1. **Rejestracja konta (MOK.1):**
    * Akcja realizowana tylko i wyłącznie przez gościa. Polega na wypełnieniu formularza rejestracyjnego: imię,
      nazwisko, adres email, login, hasło.
    * **Zmiana w bazie danych:** Operacja `INSERT` tworząca nową krotkę w tabeli `Account`. Pola `id` (generowane UUID),
      `version` (początkowo wartość 0/1), `login`, `email`, `passwordHash`, `name`, `surname` są uzupełniane danymi, a pole status
      otrzymuje domyślnie wartość *Nieaktywne*. Równolegle system może wykonać `INSERT` do tabeli `AccessLevel` (oraz
      odpowiednich tabel dziedziczących np. `Student`), przypisując domyślny poziom dostępu. Pola dat logowań (`lastLoginSuccessDate`, `lastLoginFailedDate`, `lastSuccessfulLoginIP`, `lastFailedLoginIP`) pozostają
      puste (NULL), a `languageVersion` ustawia się na domyślny język przeglądarki użytkownika.

2. **Weryfikacja konta po rejestracji samodzielnej:**
    * System weryfikuje poprawność poprzez poprawne wprowadzenie sześciocyfrowego kodu wysłanego na mail użytkownika
      zaraz po wysłaniu formularza rejestracyjnego.
    * **Zmiana w bazie danych:** Operacja `UPDATE` na odpowiedniej krotce w tabeli `Account` (wyszukanej po powiązanym z
      kontem kodzie/tokenie). Pole status zmienia wartość z *Nieaktywne* na *Aktywne*. Inkrementowana jest wartość pola
      `version`.

3. **Usuwanie nieaktywnych kont po upływie limitu czasu:**
    * Zadanie wykonywane automatycznie przez system w tle, czyszczące bazę z kont, które nie zostały zweryfikowane.
    * **Zmiana w bazie danych:** Operacja `DELETE` usuwająca powiązane krotki z tabeli `AccessLevel` (w tym tabele
      dziedziczące), a następnie krotki z tabeli `Account`, dla których status wynosi *Nieaktywne* i minął zdefiniowany
      limit czasu (10 minut od utworzenia konta).

4. **Blokowanie i odblokowywanie konta:**
    * Blokowanie może nastąpić **automatycznie** (w przypadku przekroczenia limitu błędnych logowań) lub **ręcznie**
      poprzez jawną akcję Administratora. Odblokowanie zawsze wymaga
      interwencji Administratora.
    * **Zmiana w bazie danych (Blokowanie konta):** Operacja `UPDATE` na krotce w tabeli `Account`. Pole status zmienia
      się z *Aktywne* na *Zablokowane*. Aktualizowane jest pole `lastLoginFailedDate`.
    * **Zmiana w bazie danych (Odblokowywanie konta):** Operacja `UPDATE` inicjowana przez Admina. Pole status zmienia
      się z powrotem na *Aktywne*. Możliwe jest też usunięcie naruszającego zasady konta – operacja `DELETE` usuwająca
      wpierw krotki z tabeli `AccessLevel`, a następnie krotkę z tabeli `Account`.

5. **Przydzielanie i odbieranie poziomów dostępu:**
    * Realizowane przez Administratora w panelu zarządzania.
    * **Zmiana w bazie danych (Dołącz poziom dostępu):** Wykonanie operacji `INSERT` nowej krotki do tabeli
      `AccessLevel` (oraz odpowiedniej tabeli dziedziczącej, np. `Teacher`), zawierającej odpowiednią wartość
      `accessLevelName` oraz powiązanie z `id` wybranego użytkownika (`account_id`).
    * **Zmiana w bazie danych (Odłącz poziom dostępu):** Wykonanie operacji `DELETE` na krotce w tabeli
      `AccessLevel`, łączącej danego użytkownika z konkretnym poziomem dostępu. Użytkownik natychmiast traci uprawnienia
      z nim związane.