
## 1 Dziedzina wykorzystania 1 pkt

**Przeznaczenie aplikacji**

Aplikacja służy do automatycznego wykrywania podobieństw w kodzie źródłowym poprzez integrację z usługą Gitea oraz 
wykorzystanie silnika JPlag. Nauczyciele zarządzają przedmiotami i harmonogramami, wymuszając na studentach oznaczanie 
wersji kodu odpowiednimi etykietami (tag) w systemie kontroli wersji. Studenci przesyłają rozwiązania do repozytoriów, 
a system pobiera je do analizy na podstawie zdefiniowanych harmonogramów i nazw tagów. Reguły biznesowe ograniczają 
liczbę prywatnych weryfikacji dostępnych dla studenta poprzez limitowany licznik biletów przypisany do przedmiotu. 

Zasobem, dla którego zachodzi konkurencja, jest moc obliczeniowa procesora podczas intensywnych analiz porównawczych 
oraz dostęp do zapisu liczników biletów w bazie danych. 

Aplikacja posiada charakterystykę **OLTP**, gdyż koncentruje się na bieżącej obsłudze transakcyjnych operacji wielu 
użytkowników, zarządzaniu rekordami w czasie rzeczywistym i zapewnieniu spójności danych atomowych. System nie 
realizuje analiz statystycznych (agregaty, mediany, średnie itd.) na dużych zbiorach danych historycznych, 
co wyklucza charakterystykę OLAP. 

Warstwa logiki biznesowej jest bezstanowa, co umożliwia horyzontalne skalowanie i delegowanie stanu do bazy danych. 
Wymiana danych między warstwą prezentacji a logiką odbywa się w formacie **JSON** poprzez interfejs **REST API**. 

Stos technologiczny dla warstwy logiki biznesowej obejmuje **język Java, framework Spring Boot oraz narzędzie Hibernate**.

**Słownik nazw obiektów w modelu danych**

Tabela 1.1.1 Słownik nazw obiektów w modelu danych.

| Identyfikator | Opis                                                                                                                        | Cykl życia                                                                                                                                    | Zależności                                                          | Ograniczenia liczności                                        | Ograniczenia powiązań                                                       | 
|---------------|-----------------------------------------------------------------------------------------------------------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------|---------------------------------------------------------------------|---------------------------------------------------------------|-----------------------------------------------------------------------------|
| Account       | Obiekt przechowujący dane uwierzytelniające i podstawowe dane osobowe użytkownika, reprezentujący zalogowanego użytkownika. | Od rejestracji aż po usunięcie przez Administratora.                                                                                          | Obiekt nadrzędny dla klasy Role                                     | Relacja 1:N pomiędzy obiektami Role.                          | Unikalny login oraz email.                                                  |
| Role          | Abstrakcyjna klasa bazowa definiująca nazwę profilu (roleName).                                                             | Tworzona i przypisywana do Account w momencie nadawania funkcji i usuwana wraz z nim lub odebraniem uprawnień.                                | Zależna od Account. Stanowi bazę dla klas: Student, Teacher, Admin. | Relacja N:1 pomiędzy obiektem Account.                        | Klasa nieinstancjalna. Wymaga rozszerzenia przez jedną z ról szczegółowych. |
| Student       | Obiekt reprezentujący rolę, umożliwiający oddawanie prac i dostęp do wyników analiz prywatnych.                             | Tworzony przy nadaniu roli studenta i usuwany wraz z Account lub odebraniem uprawnień.                                                        | Zależny od jednego obiektu Account (dziedziczy po klasie Role).     | Maksymalnie jeden obiekt tej klasy na jeden obiekt Account.   | Powiązany z wieloma obiektami Repository. (1:N)                             |                         
| Teacher       | Obiekt reprezentujący rolę, pozwalający na prowadzenie przedmiotów i definiowanie reguł.                                    | Tworzony przy nadaniu roli nauczyciela (Teacher) i usuwany wraz z Account lub odebraniem uprawnień.                                           | Zależny od jednego obiektu Account (dziedziczy po klasie Role).     | Maksymalnie jeden obiekt tej klasy na jeden obiekt Account.   | Powiązany z listą obiektów Subject. (1:N)                                   |
| Admin         | Obiekt reprezentujący rolę, posiadający uprawnienia do globalnego zarządzania użytkownikami oraz konfiguracją systemu.      | Nadawany programistycznie (pierwszy administrator) lub innego Admina i usuwany wraz z Account lub odebraniem uprawnień.                       | Zależny od jednego obiektu Account (dziedziczy po klasie Role).     | Maksymalnie jeden obiekt tej klasy na jeden obiekt Account.   | Brak bezpośrednich powiązań z innymi obiektami.                             |                                                
| Subject       | Obiekt reprezentująca przedmiot (organizację z usługi Gitea).                                                               | Tworzony przez użytkownika z nadaną rolą Teacher. Jego usunięcie skutkuje kaskadowym usunięciem powiązanych obiektów Report, Schedule i Rule. | Zależny od co najmniej jednego obiektu Teacher.                     | Posiada dokładnie jeden obiekt Rule i wiele obiektów Teacher. | Nazwa name musi być unikalna w obrębie danego year                          |
| Repository    | Obiekt reprezentujący zdalne repozytorium kodu źródłowego w usłudze Gitea.                                                  | Tworzony przy podpięciu studentów do przedmiotu i usuwany wraz z obiektem Subject.                                                            | Zależny od jednego obiektu Subject oraz wielu obiektów Student.     | Relacja N:M pomiędzy Student a Repository.                    | Musi wskazywać na istniejący obiekt Subject.                                |
| Schedule      | Obiekt planujący automatyczne uruchomienie ananlizy porównawczej silnika JPlag dla konkretnego tagu.                        | Tworzony przez Teacher. Usuwany po wykonaniu zadania lub ręcznie.                                                                             | Zależny od jednego obiektu Subject oraz obiektu Tag.                | Relacja 1:1 z konkretnym obiektem Tag w danym czasie.         | Data scheduledDateTime musi być datą przyszłą.                              |
| Report        | Obiekt reprezentujący zagregowany wynik analizy podobieństwa wygenerowany przez silnik JPlag.                               | Generowany po zakończeniu analizy i utrwalony do momentu usunięcia obiektu Subject.                                                           | Zależny od jednego obiektu Subject oraz wielu obiektów Repository.  | Relacja N raportów dla jednego Subject (N:1).                 | Powiązany z konkretnym przebiegiem Schedule.                                |
| Tag           | Obiekt reprezentujący etykietę wersji kodu (np. numer zadania), zdefiniowaną przez prowadzącego dla danego zadania.         | Definiowany przez użytkownika z nadaną rolą Teacher w ramach obiektu Rule.                                                                    | Zależny od obiektu Rule oraz Subject.                               | Wiele tagów dla jednego Subject (N:1).                        | Nazwa tagName unikalna w skali obiektu Subject.                             |
| Rule          | Obiekt konfigurujący restrykcje analiz (poziom raportowania, limit biletów) dla przedmiotu.                                 | Tworzony automatycznie przy inicjalizacji Subject.                                                                                            | Ściśle zależny od Subject.                                          | Relacja 1:1 z obiektem Subject.                               | Atrybut studentTicketCount musi być wartością nieujemną.                    |                                                

**Słownik pozostałych pojęć i obiektów**

* **JPlag** — zewnętrzna biblioteka stosowana jako silnik analizy porównawczej kodu źródłowego z repozytoriów
pod względem jego powtarzalności.

* **Gitea** — zewnętrzna usługa hostingu systemów kontroli wersji Git, pełniąca funkcję
centralnego składowiska kodów źródłowych (repozytoriów) przesyłanych przez studentów.
System komunikuje się z Gitea, aby na podstawie danych z harmonogramu automatycznie pobierać
wersje projektów oznaczone odpowiednim znacznikiem (tagiem) do dalszego przetwarzania przez silnik JPlag.

* **Wskaźnik podobieństwa** — procentowa wartość określająca stopień podobieństwa między porównywanymi kodami 
źródłowymi.

* **Analiza prywatna** – proces weryfikacji kodu zainicjowany bezpośrednio przez studenta przed oficjalnym terminem
oddania pracy. Liczba takich analiz jest ograniczona przez parametr studentTicketCount w zestawie reguł
i pozwala studentowi na samodzielną kontrolę oryginalności swojego rozwiązania.

* **similarityRate** – parametr określający zakres informacji zwrotnej prezentowanej studentowi po zakończeniu
analizy.

* **AnalysisQueue** – tymczasowa lista zadań oczekujących na przetworzenie przez silnik porównawczy.
Pozwala na sprawne zarządzanie zasobami serwera, gdy wiele użytkowników wysyła żądanie wykonania analizy kodu
jednocześnie.

* **Postać stokenizowana** — abstrakcyjna postać kodu źródłowego, wygenerowana przez silnik JPlag.
Kod zamieniany jest na ciąg symboli, co pozwala algorytmowi ignorować np. zmianę nazw zmiennych czy białe znaki.
Istnieje tylko w pamięci operacyjnej podczas analizy.
