
## 1 Dziedzina wykorzystania 1 pkt

**Przeznaczenie aplikacji**

Aplikacja służy do automatycznego wykrywania podobieństw w kodzie źródłowym poprzez integrację z usługą Gitea oraz 
wykorzystanie silnika JPlag. Prowadzący zarządzają przedmiotami i harmonogramami, wymuszając na studentach oznaczanie 
wersji kodu odpowiednimi etykietami (tag) w systemie kontroli wersji. Studenci przesyłają rozwiązania do repozytoriów, 
a system pobiera je do analizy na podstawie zdefiniowanych harmonogramów i nazw tagów. Reguły biznesowe ograniczają 
liczbę prywatnych weryfikacji dostępnych dla studenta poprzez limitowany licznik biletów przypisany do przedmiotu. 

Zasobem, dla którego zachodzi konkurencja, jest dostęp do zapisu i aktualizacji liczników biletów studentów w bazie 
danych, które umożliwiają im przeprowadzenie ewentualnej analizy prywatnej dla swojego kodu w repozytorium. 

Aplikacja posiada charakterystykę **OLTP**, ponieważ skupia się na bieżącym przetwarzaniu transakcji użytkowników 
oraz na zapewnianiu spójności danych w czasie rzeczywistym. Choć system wykonuje operacje związane z analizą 
statystyczną poprzez wyliczanie agregatów (średnia wartość wskaźnika podobieństwa), operacje te są składową jednostkowego 
procesu weryfikacji udostępnionych repozytoriów pod względem podobieństwa przedstawionych rozwiązań. Wyniki te są danymi 
pochodnymi konkretnej transakcji, a nie wynikiem analizy trendów na zbiorach danych historycznych, jak w systemach OLAP.

Warstwa logiki biznesowej jest bezstanowa, co umożliwia horyzontalne skalowanie i delegowanie stanu do bazy danych. 
Wymiana danych między warstwą prezentacji a logiką odbywa się w formacie **JSON** poprzez interfejs **REST API**. 

Stos technologiczny dla warstwy logiki biznesowej obejmuje **język Java, framework Spring Boot oraz narzędzie Hibernate**.

**Słownik nazw obiektów w modelu danych**

Tabela 1.1.1 Słownik nazw obiektów w modelu danych.

| Identyfikator     | Opis                                                                                                                                                              | Cykl życia                                                                                                                                          | Zależności                                                                  | Ograniczenia powiązań                                                                                  | Ograniczenia liczności                                                                                                                                                                                                                                             | 
|-------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------|-----------------------------------------------------------------------------|--------------------------------------------------------------------------------------------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Account           | Obiekt przechowujący dane uwierzytelniające i podstawowe dane osobowe użytkownika, reprezentujący zalogowanego użytkownika.                                       | Od rejestracji aż po usunięcie przez Administratora.                                                                                                | Obiekt nadrzędny dla klasy AccessLevel.                                     | Relacja 1:N pomiędzy obiektami AccessLevel.                                                            | **login**: maksymalnie 50 znaków, unikalny, nie może być null. <br> **email**: unikalny, maksymalnie 50 znaków, nie może być null.<br> **passwordHash**: maksymalnie 64 znaki, nie może być null.<br> **name/surname**: maksymalnie 50 znaków, nie mogą być puste. |
| AccessLevel       | Abstrakcyjna encja bazowa, przechowująca wspólną logikę i identyfikację poziomów dostępu przypisanych do konta.                                                   | Klasa abstrakcyjna. Nie posiada swojej instancji obiektu.                                                                                           | Zależy od jednego obiektu Account.                                          | Relacja N:1 pomiędzy obiektami Account. Dziedziczenie realizowane za pomocą strategii JOINED.          | **accessLevelName**: nie może być puste.                                                                                                                                                                                                                           |
| Student           | Obiekt reprezentujący rolę, umożliwiający oddawanie prac i dostęp do wyników analiz prywatnych.                                                                   | Tworzony przy nadaniu roli studenta i usuwany wraz z Account lub odebraniem uprawnień.                                                              | Dziedziczy po abstrakcyjnej klasie AccessLevel.                             | Logiczna relacja 1:1 z Account. Relacja N:M z obiektem Repository.                                     | Brak własnych atrybutów.                                                                                                                                                                                                                                           |                         
| Teacher           | Obiekt reprezentujący rolę, pozwalający na prowadzenie przedmiotów i definiowanie reguł.                                                                          | Tworzony przy nadaniu roli prowadzącego (Teacher) i usuwany wraz z Account lub odebraniem uprawnień.                                                | Dziedziczy po abstrakcyjnej klasie AccessLevel.                             | Logiczna relacja 1:1 z Account. Relacja 1:N z obiektem SubjectAssignment.                              | Brak własnych atrybutów.                                                                                                                                                                                                                                           |
| Admin             | Obiekt reprezentujący rolę, posiadający uprawnienia do globalnego zarządzania użytkownikami oraz konfiguracją systemu.                                            | Nadawany programistycznie (pierwszy administrator) lub innego Admina i usuwany wraz z Account lub odebraniem uprawnień.                             | Dziedziczy po abstrakcyjnej klasie AccessLevel.                             | Logiczna relacja 1:1 z Account.                                                                        | Brak własnych atrybutów.                                                                                                                                                                                                                                           |                                                
| Subject           | Obiekt reprezentujący przedmiot (organizację z usługi Gitea).                                                                                                     | Tworzony przez użytkownika z nadaną rolą Teacher. Jego usunięcie skutkuje kaskadowym usunięciem powiązanych obiektów Report, Schedule i RulePreset. | Zależny od co najmniej jednego obiektu Teacher (poprzez SubjectAssignment). | Relacja 1:1 z obiektem RulePreset. Relacja 1:N z obiektem SubjectAssignment. Relacja 1:N z Repository. | **name**: maksymalnie 64 znaki, nie może być null. <br>**edition**: maksymalnie 16 znaków, nie może być null. <br>**giteaURL**: maksymalnie 255 znaków, nie może być null.<br> **archived**: nie może być null, wartość logiczna (Boolean).                        |
| SubjectAssignment | Obiekt reprezentujący bazodanową encję asocjacyjną, łącząca prowadzącego z przedmiotem i definiująca jego poziom uprawnień (Prowadzący lub Prowadzący dodatkowy). | Tworzona przy przypisaniu nauczyciela do przedmiotu, usuwana wraz z przedmiotem lub nauczycielem.                                                   | Zależna od jednego obiektu Teacher oraz jednego obiektu Subject.            | Relacja N:1 z obiektem Teacher oraz N:1 z obiektem Subject.                                            | **hasEditPermissions**: nie może być null, wartość logiczna (Boolean).                                                                                                                                                                                             |
| Repository        | Obiekt reprezentujący zdalne repozytorium kodu źródłowego w usłudze Gitea.                                                                                        | Tworzony przy podpięciu studentów do przedmiotu i usuwany wraz z obiektem Subject.                                                                  | Zależny od jednego obiektu Subject oraz wielu obiektów Student.             | Relacja N:1 z Subject. Relacja N:M z Student. Relacja N:M z Report.                                    | **repositoryName**: maksymalnie 30 znaków, nie może być null.                                                                                                                                                                                                      |
| Schedule          | Obiekt planujący automatyczne uruchomienie analizy porównawczej silnika JPlag dla konkretnego tagu.                                                               | Tworzony przez Teacher. Usuwany po wykonaniu zadania lub ręcznie.                                                                                   | Zależny od jednego obiektu RulePreset.                                      | Relacja N:1 z obiektem RulePreset.                                                                     | **tag**: maksymalnie 64 znaki, nie może być null.<br> **scheduleDateTime**: data przyszła, nie może być null.                                                                                                                                                      |
| Report            | Obiekt reprezentujący zagregowany wynik analizy podobieństwa wygenerowany przez silnik JPlag.                                                                     | Generowany po zakończeniu analizy i utrwalony do momentu usunięcia obiektu Subject.                                                                 | Zależny od jednego obiektu Subject oraz wielu obiektów Repository.          | Relacja N:1 z Subject. Relacja N:M z obiektem Repository.                                              | **pathToReport**: maksymalnie 255 znaków, nie może być null.                                                                                                                                                                                                       |
| RulePreset        | Obiekt konfigurujący restrykcje analiz (poziom raportowania, limit biletów) dla przedmiotu.                                                                       | Tworzony automatycznie przy inicjalizacji Subject.                                                                                                  | Ściśle zależny od Subject.                                                  | Relacja 1:1 z Subject. Relacja 1:N z obiektem Schedule.                                                | **raportLevel**: maksymalnie 32 znaki, nie może być null.<br> **studentTicketCount**: nieujemna liczba całkowita (Integer), nie może być null.<br> **isPreset**: wartość logiczna (Boolean), nie może być null.                                                    |                                                

**Słownik pozostałych pojęć i obiektów**

* **JPlag** — zewnętrzna biblioteka stosowana jako silnik analizy porównawczej kodu źródłowego z repozytoriów
pod względem jego powtarzalności.

* **Gitea** — zewnętrzna usługa hostingu systemów kontroli wersji Git, pełniąca funkcję
centralnego składowiska kodów źródłowych (repozytoriów) przesyłanych przez studentów.
System komunikuje się z Gitea, aby na podstawie danych z harmonogramu automatycznie pobierać
wersje projektów oznaczone odpowiednim znacznikiem (tagiem) do dalszego przetwarzania przez silnik JPlag.

* **AssistantTeacher** (Prowadzący dodatkowy) — użytkownik posiadający rolę Teacher, przypisany do konkretnego
przedmiotu (Subject) z ograniczonym zakresem uprawnień (brak przywileju modyfikacji). Użytkownik ten posiada wgląd 
w listy studentów, repozytoria oraz wygenerowane raporty, jednak nie ma uprawnień do edycji zestawu reguł (RulePreset) 
ani modyfikowania harmonogramu (Schedule).

* **SimilarityRate** (Wskaźnik podobieństwa) — procentowa wartość określająca stopień podobieństwa między porównywanymi 
kodami źródłowymi.

* **PrivateAnalysis** (Analiza prywatna) – proces weryfikacji kodu zainicjowany bezpośrednio przez studenta przed 
oficjalnym terminem oddania pracy. Liczba takich analiz jest ograniczona przez parametr studentTicketCount w zestawie 
reguł i pozwala studentowi na samodzielną kontrolę oryginalności swojego rozwiązania.

* **ReportLevel** (Poziom raportowania) – parametr określający zakres informacji zwrotnej prezentowanej studentowi po 
zakończeniu analizy.

* **AnalysisQueue** – tymczasowa lista zadań oczekujących na przetworzenie przez silnik porównawczy.
Pozwala na sprawne zarządzanie zasobami serwera, gdy wiele użytkowników wysyła żądanie wykonania analizy kodu
jednocześnie.

* **TokenizedCode** (Postać stokenizowana) — abstrakcyjna postać kodu źródłowego, wygenerowana przez silnik JPlag.
Kod zamieniany jest na ciąg symboli, co pozwala algorytmowi ignorować np. zmianę nazw zmiennych czy białe znaki.
Istnieje tylko w pamięci operacyjnej podczas analizy.
