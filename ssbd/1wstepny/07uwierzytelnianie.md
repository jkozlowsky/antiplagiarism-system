
## 7 Model kont i uwierzytelnianie w aplikacji 1 pkt

**Model kont**

W bazie danych muszą znajdować się krotki umożliwiające przeprowadzenie procesu uwierzytelniania dla przykładowych kont użytkowników aplikacji, przy czym przynajmniej jedno z tych kont musi występować jednocześnie w wielu poziomach dostępu

**Procedury zarządzania kontami**

Zarządzanie cyklem życia konta opiera się na przejściach między jego stanami. Każda z poniższych operacji bezpośrednio modyfikuje krotki w relacyjnej bazie danych:

1. Rejestracja konta:
    * Akcja realizowana tylko i wyłącznie przez gościa. Będzie ona polegać na wypełnieniu formularza rejestracyjnego.
    * Zmiana w bazie danych: Operacja INSERT tworząca nową krotkę w tabeli Account. Pola login, email, passwordHash, name, surname są uzupełniane danymi, a pole status otrzymuje domyślnie wartość Nieaktywne. Równolegle system może wykonać INSERT do tabeli Role, przypisując domyślną rolę. Pola lastLoginFailedDate, lastLoginSuccessDate zostają null, oraz lastFailedLoginIP i lastSuccessLoginIp zostają puste. Pole languageVersion zostaje ustawione na domyślny język przeglądarki użytkownika.

2. Weryfikacja konta po rejestracji samodzielnej:
   * System weryfikuje poprawność poprzez poprawne wprowadzenie sześciocyfrowego kodu wysłanego na mail użytkownika zaraz po wysłaniu formularza rejestracyjnego. 
   * Zmiana w bazie danych: Operacja UPDATE na odpowiedniej krotce w tabeli Account (wyszukanej po unikalnym tokenie powiązanym z kontem). Pole status zmienia wartość z Nieaktywne na Aktywne.

3. Usuwanie nieaktywnych kont po upływie limitu czasu:
   * Zadanie wykonywane automatycznie przez system, czyszczące bazę z kont, które nie zostały zweryfikowane.
   * Zmiana w bazie danych: Operacja DELETE usuwająca krotki z tabeli Account oraz powiązane krotki z tabeli Role, dla których status = 'Nieaktywne' oraz data utworzenia przekracza zdefiniowany limit czasu — 10 minut.

4. Blokowanie i odblokowywanie konta:
   * Blokowanie następuje automatycznie przez system w przypadku przekroczenia limitu błędnych logowań. Odblokowanie wymaga interwencji Administratora.
   * Zmiana w bazie danych (Blokowanie konta): Operacja UPDATE na krotce w tabeli Account. Pole status zmienia się z Aktywne na Zablokowane. Zaktualizowane zostaje również pole lastLoginFailedDate i odpowiednio zwiększany jest licznik błędnych prób logowania.
   * Zmiana w bazie danych (Odblokowywanie konta): Operacja UPDATE inicjowana przez Admina. Zmiana pola status krotki w tabeli Account z powrotem na Aktywne i wyzerowanie licznika błędnych prób. Możliwe jest też trwałe usunięcie zablokowanego konta w przypadku naruszenia zasad — operacja DELETE usuwająca krotkę z tabeli Account oraz powiązane krotki z tabeli Role.

5. Przydzielanie i odbieranie poziomów dostępu:
   * Realizowane przez Administratora w panelu zarządzania kontami.
   * Zmiana w bazie danych (Przydzielanie nowych uprawnień): Wykonanie operacji INSERT nowej krotki do tabeli Role, zawierającej odpowiednią wartość roleName (np. 'Nauczyciel') oraz account_id wskazujące na wybranego użytkownika. Od tej pory w relacji jeden-do-wielu pojawia się nowy rekord.
   * Zmiana w bazie danych (Odbieranie uprawnień): Wykonanie operacji DELETE na krotce w tabeli Role, która łączy dane account_id z konkretnym roleName. Użytkownik traci tym samym uprawnienia wynikające z danej roli.