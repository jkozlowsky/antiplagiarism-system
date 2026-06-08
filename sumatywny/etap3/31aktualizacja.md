
## 3.1. Aktualizacja projektu funkcjonalności

- Z tabeli Repository usunięto pole ticketCount. Z tego powodu dokonano refaktoryzacji obecnego kodu i zastosowano blokadę pesymistyczną przy sprawdzaniu własnego kodu przez studenta.

### 1.1 Słownik pojęć dziedzinowych

W trakcie implementacji słownik uległ następującym zmianom:

**Profil Prowadzącego (Teacher)** - Zrezygnowano z binarnej flagi `has_modify_privilege` na rzecz jawnej enumeracji
`subject_role` przyjmującej wartości `OWNER`, `EDITOR` lub `VIEWER`.

**Przedmiot (Subject)** — pole opisu (`subject_description`) zostało wyodrębnione do osobnej encji tłumaczeń, 
umożliwiając przechowywanie opisu w wielu wersjach językowych. Zagregowany wskaźnik podobieństwa przeniesiono 
do encji`SubjectStatistics`.

**Zestaw reguł (~~Rule~~ RulePreset)** — dodano dwa parametry konfiguracyjne silnika JPlag:
minimalną długość dopasowania tokenów `minimum_tokens_match`, która domyślnie przyjmuje wartość 20
oraz flagę normalizacji kodu przed tokenizacją `normalization`.

**Harmonogram (Schedule)** — dodano flagi oznaczające stan wykonania: `is_executed`oraz `has_failed`, pozwalające 
odróżnić pozycje oczekujące od obsłużonych i błędnych.

**Raport (Report)** — dodano etykietę zadania `tag`, liczbę przeskanowanych repozytoriów `scanned_repositories` 
oraz flagę `is_student_report` odróżniającą raporty analizy prywatnej od raportów prowadzącego.

Dodatkowo wprowadzono nowe pojęcia występujące w modelu danych utrwalanych w ramach projektu:

**Historia haseł (PasswordHistory)** — encja przechowująca skróty poprzednich haseł konta, uniemożliwiająca ponowne 
użycie hasła już wcześniej stosowanego.

**Jednorazowy token (OneTimeTokenEnt)** — token o określonym czasie ważności, wykorzystywany do jednorazowych operacji 
wymagających potwierdzenia jak np. reset hasła lub aktywacja konta. Posiada typ `token_type` pozwalający odróżnić tokeny 
służące do różnych operacji.

**Token cofnięcia adresu e-mail (EmailRevertTokenEnt)** — token umożliwiający anulowanie zmiany adresu e-mail. 
Przechowuje poprzedni adres e-mail konta i wygasa po określonym czasie.

**Statystyki przedmiotu (SubjectStatistics)** — encja przechowująca zagregowany wskaźnik podobieństwa dla przedmiotu, 
wyodrębniona z tabeli `Subject`.

**Tłumaczenie opisu przedmiotu (SubjectDescriptionTranslation)** — encja zastępująca pole `subject_description` 
z encji `Subject`, umożliwiająca przechowywanie dynamicznych tłumaczeń opisu przedmiotu w różnych wersjach językowych.

**Rola w przedmiocie (TeacherSubjectRole)** — enumeracja `OWNER`, `EDITOR` lub `VIEWER` zastępująca flagę `has_modify_privilege`.

**Słownik języków (LanguageDict)** — encja słownikowa przechowująca dostępne wersje językowe interfejsu i treści systemu.

**Słownik poziomów dostępu (AccessLevelDict)** — encja słownikowa definiująca dostępne role kont w systemie.

**Słownik poziomów raportowania (ReportLevelDict)** — encja słownikowa definiująca możliwe poziomy szczegółowości raportu 
prezentowanego studentowi.

### 1.2 Diagram modelu

Diagram modelu dziedzinowego uległ zmianie z powodu dodania nowych klas encyjnych takich jak:
- LanguageDict
- AccessLevelDict
- ReportLevelDict
- PasswordHistory
- OneTimeTokenEnt
- EmailRevertTokenEnt
- SubjectStatistics
- SubjectDescriptionTranslation

Usunięto tabelę Tag oraz zmieniono nazwę tabeli Rule na RulePreset.
Dodano nowe pola do istniejących klas.

![Diagram uml](etap3/img/Domain_UML_final.png)

Rysunek 3.1.1. Diagram UML wersji finałowej.

### 1.4. Diagram(y) stanów (przejść)

Wskazany element nie uległ zmianie.

## 1.5. Infrastruktura wdrożeniowa

Element uległ zmianie w trakcie implementacji systemu. Pierwotna architektura została rozbudowana o nowe komponenty, co pociągnęło za sobą zmiany w diagramie wdrożenia, opisie topologii sieci oraz wykazie wykorzystanego oprogramowania:

![Diagram infrastruktury](etap3/img/diagram_1.4.png)

Rysunek 3.1.2 Diagram infrastruktury projektowej

Aktualizacja diagramu rysunku 1.5.1 została przedstawiona na rysunku 3.1.1. Do infrastruktury kontenerowej dodano nowe węzły obsługujące system kontroli wersji. Składają się na nie: kontener usługi gitea oraz izolowany kontener gitea-db. Nginx nadal pełni rolę jedynego publicznego punktu wejścia, ale obecnie przekierowuje zapytania nie tylko do aplikacji głównej, lecz również do serwera Gitea.

Reorganizacja wirtualnych podsieci: Zrezygnowano z pierwotnego podziału na dwie strefy (siec_frontend i siec_backend). Zamiast tego zaimplementowano bardziej szczegółową izolację opartą na czterech sieciach: proxy-frontend, proxy-backend, backend-db oraz gitea-net. Oddzielono tym samym ruch z warstwy pośredniczącej (proxy-backend) od warstwy składowania danych – główna baza korzysta z sieci backend-db, natomiast baza danych dla systemu wersji z sieci gitea-net.

Tabela 3.1.1 Dodatkowe oprogramowanie wykorzystane w systemie

| Oprogramowanie | Wersja            | Rola w systemie                                       | Licencja           |
|----------------|-------------------|-------------------------------------------------------|--------------------|
| Gitea          | 1.25.2 - rootless | Lokalny system kontroli kodu źródłowego               | MIT License        |
| PostgreSQL     | 14                | Relacyjny system zarządzania bazą danych usługi Gitea | PostgreSQL License |

Do tabeli zrealizowanych technologii (Tabela 1.5.1) doszły nowe pozycje wykorzystywane w obecnym wdrożeniu (Tabela 3.1.1). Oprogramowanie Gitea (w wersji 1.25.2-rootless) oraz dodatkowa instancja relacyjnej bazy danych PostgreSQL (w wersji 14), która służy wyłącznie do obsługi usługi repozytoriów kodu. Pozostałe wersje oprogramowania bazowego nie uległy zmianie.
