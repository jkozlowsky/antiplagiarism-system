
## 1 Dziedzina wykorzystania 1 pkt

**Przeznaczenie aplikacji**

* Krótki opis świadczonej przez aplikację funkcjonalności, należy krótko opisać reguły biznesowe określające zasady przetwarzania danych.
* Wśród przetwarzanych danych wskazać zasoby, dla których zachodzi konkurencja.
* Określenie z uzasadnieniem charakterystyki aplikacji (OLTP/OLAP).
* Sprecyzować stanowość lub bezstanowość aplikacji w warstwie logiki biznesowej i format wymiany dany pomiędzy warstwą prezentacji a warstwą logiki
biznesowej.
* Stos technologiczny dla warstwy logiki biznesowej.
* Łącznie max. 15 zdań.

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
* wraz z krótkim opisem i wyszczególnieniem odpowiadających im cech, których informacje posiadają swoją reprezentację w przyjętym modelu danych
(zawartość bazy danych). Dla obiektów opisanych w słowniku należy określić cykl życia, zidentyfikować zależności, z innymi obiektami, wymienić
ograniczenia m. in. liczności i powiązań.

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
* Słownik wszystkich pozostałych występujących w projekcie pojęć i obiektów, które nie zostały wymienione w modelu danych. 
* Uwagi szczegółowe dot. tworzenia OBU słowników:
  * Forma: tabela lub lista 
  * Przedstawiona terminologia musi być jednoznaczna i obowiązuje zarówno w dalszej części dokumentacji, nazwach tabel oraz klas w kodzie aplikacji, 
  * czy też dokumentacji programistycznej. 
  * Składnia identyfikatorów musi być zgodna ze składnią obowiązującą w języku programowania Java, wymagane ustalenie wielkości liter dla
identyfikatorów oraz brak polskich znaków
