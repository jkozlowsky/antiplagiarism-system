
## 3.2. Problem przetwarzania współbieżnego

### Równoległa edycja

Przykładem problemu równoległej edycji danych w naszej aplikacji jest  jest edycja szablonów reguł (ustawień) przedmiotów. Akcja może być wykonana jednocześnie przez dwóch prowadzących którzy chcą zmodyfikować ten szablon.

Tabela 3.2.1. Oś czasu przeplotu żądań podczas równoległej edycji.

| Czas | Wątek A (Prowadzący 1)                                       | Wątek B (Prowadzący 2)                                                           | Stan obiektu w bazie danych                                                 |
| ---- | ------------------------------------------------------------ | -------------------------------------------------------------------------------- | --------------------------------------------------------------------------- |
| T1   | Odczyt danych z bazy danych.                                 |                                                                                  | {<br>...<br>student_ticket_count=1<br>minimum_tokens_match=10<br>}          |
| T2   |                                                              | Odczyt danych z bazy danych.                                                     | {<br>...<br>student_ticket_count=1 <br>minimum_tokens_match=10<br>}         |
| T3   | Zmiana ilości biletów dla studentów na 5 w GUI przeglądarki. |                                                                                  | {<br>...<br>student_ticket_count=1<br>minimum_tokens_match=10<br>}          |
| T4   |                                                              | Zmiana minimalnej ilości tokenów dla wykrycia płagiatu na 20 w GUI przeglądarki. | {<br>...<br>student_ticket_count=1<br>minimum_tokens_match=10<br>}          |
| T5   | Zapis danych.                                                |                                                                                  | {<br>...<br>student_ticket_count=**5**<br>minimum_tokens_match=**10**<br>}  |
| T6   |                                                              | Zapis danych.                                                                    | {<br>...<br>student_ticket_count=**1** <br>minimum_tokens_match=**20**<br>} |

W przykładzie zaprezentowanym w tabeli 3.2.1 doszło do niepożądanego zachowania - dane które zapisał prowadzący 1 dotyczące ilości biletów  zostały nadpisane przez prowadzącego 2, który mimo tego że nie zmieniał tych danych (bo zmieniał inny parameter), i tak nadpisał je danymi które odczytał na początku.
W wyniku - dane wprowadzone przez prowadzącego 1 zostały utracone. 

Dla zabezpieczenia przed takim zachowaniem zastosowano mechanizm blokady optymistycznej oparty na polu wersji. Podczas pobierania danych aplikacja serwerowa wysyła w ciele odpowiedzi skrót wyliczony na podstawie id obiektu, jego pola wersji oraz losowo wygenerowanego sekretnego ciągu znaków utrzymywanego na serwerze. Kiedy użytkownik zatwierdza zmianę danych obiektu, ten skrót jest wysyłany w nagłówku "If-Match" żądania z powrotem na serwer, a ten wylicza jeszcze jeden skrót z obecnego stanu danych w bazie danych (id oraz pola wersji) i porównuje go z tym który otrzymał w żądaniu. Jeżeli ten skrót się zgadza - aplikacja serwerowa zapisuje wprowadzone zmiany oraz zwiększa o 1 pole wersji dla tego obiektu. W przypadku jeżeli wyliczony skrót będzie niezgodny ze skrótem w odpowiedzi, serwer zwraca odpowiedź z kodem HTTP 409 Conflict i komunikatem o potrzebie odświeżenia stanu modyfikowanego obiektu przez użytkownika.

Tabela 3.2.2. Oś czasu przeplotu żądań podczas równoległej edycji zabezpieczonej mechanizmem blokady optymistycznej.

| Czas | Wątek A (Prowadzący 1)                                                                                                      | Wątek B (Prowadzący 2)                                                                                                  | Stan obiektu w bazie danych                                                              | Odpowiedż serwera                                                                     |
| ---- | --------------------------------------------------------------------------------------------------------------------------- | ----------------------------------------------------------------------------------------------------------------------- | ---------------------------------------------------------------------------------------- | ------------------------------------------------------------------------------------- |
| T1   | Odczyt danych z bazy danych.                                                                                                |                                                                                                                         | {<br>...<br>version=1<br>student_ticket_count=1<br>minimum_tokens_match=10<br>}          | {<br>...<br>versionHash=aaa<br>student_ticket_count=1<br>minimum_tokens_match=10<br>} |
| T2   |                                                                                                                             | Odczyt danych z bazy danych.                                                                                            | {<br>...<br>version=1<br>student_ticket_count=1 <br>minimum_tokens_match=10<br>}         | {<br>...<br>versionHash=aaa<br>student_ticket_count=1<br>minimum_tokens_match=10<br>} |
| T3   | Zmiana ilości biletów dla studentów na 5 w GUI przeglądarki.                                                                |                                                                                                                         | {<br>...<br>version=1<br>student_ticket_count=1<br>minimum_tokens_match=10<br>}          | -                                                                                     |
| T4   |                                                                                                                             | Zmiana minimalnej ilości tokenów dla wykrycia płagiatu na 20 w GUI przeglądarki.                                        | {<br>...<br>version=1<br>student_ticket_count=1<br>minimum_tokens_match=10<br>}          | -                                                                                     |
| T5   | Żądanie zapisu danych.<br>{<br>...<br>versionHash=aaa<br>student_ticket_count=**5**<br>minimum_tokens_match=**10**<br>}<br> |                                                                                                                         | {<br>...<br>version=2<br>student_ticket_count=**5**<br>minimum_tokens_match=**10**<br>}  | HTTP 200 OK                                                                           |
| T6   |                                                                                                                             | Żądanie zapisu danych.<br>{<br>...<br>versionHash=aaa<br>student_ticket_count=**1**<br>minimum_tokens_match=**20**<br>} | {<br>...<br>version=2<br>student_ticket_count=**5** <br>minimum_tokens_match=**10**<br>} | HTTP 409 Conflict<br>Obiekt został zmodyfikowany od czasu ostatniej aktualizacji.     |

Poniżej zaprezentowano przebieg komunikacji między klientami korzystającymi z przeglądarek i posiadającymi poziom dostępu "prowadzący" a aplikacją serwerową jak w powyższej sytuacji.

![3.2.1.png](etap3/img/3.2.1.png)

Rys. 3.2.1. Pobranie szablonu reguł przez dwóch prowadzących .

![3.2.2](etap3/img/3.2.2.png)

Rys 3.2.2. Modyfikacja danych szablonu.

![3.2.3.png](etap3/img/3.2.3.png)

Rys 3.2.3. Zapis zmodyfikowanych danych przez dwóch prowadzących do aplikacji serwerowej.

Na rysunku 3.2.3 widzimy że prowadzący który wykonał zapis jako drugi w kolejności otrzymał błąd z komunikatem o wymaganym odświeżeniu obiektu.
### Naruszenie zasad logiki biznesowej

Przykładem problemu naruszenia zasad logiki biznesowej w naszej aplikacji jest wykonanie akcji generowania raportu przez studenta. Student ma ograniczoną ilość "biletów", ustawianą przez prowadzącego, na sprawdzenie swojego kodu w systemie na rzecz plagiatu. Problem występuje gdy dwóch studentów próbują wygenerować raport dla tego samego repozytorium w tym samym czasie gdy mają tylko jeden dostępny bilet.

Tabela 3.2.3. Oś czasu przeplotu żądań podczas generowania raportów w sytuacji gdy limit wykorzystanych biletów jest równy 2.

| Czas | Wątek A (student 1)                                                        | Wątek B (student 2)                                                        | Ilość wygenerowanych raportów biletów (limit = 2) |
| ---- | -------------------------------------------------------------------------- | -------------------------------------------------------------------------- | ------------------------------------------------- |
| T1   | Odczyt danych z bazy danych.                                               |                                                                            | 1                                                 |
| T2   |                                                                            | Odczyt danych z bazy danych.                                               | 1                                                 |
| T3   | Żądanie generowania raportu. Sprawdzenie czy nie przekroczono limit 1 < 2. |                                                                            | 1                                                 |
| T4   |                                                                            | Żądanie generowania raportu. Sprawdzenie czy nie przekroczono limit 1 < 2. | 1                                                 |
| T5   | Zakończenie generowania raportu, zapis do bazy danych.                     |                                                                            | 2                                                 |
| T6   |                                                                            | Zakończenie generowania raportu, zapis do bazy danych.                     | 3                                                 |
W przykładzie zaprezentowanym w tabeli 3.2.3 doszło do niepożądanego zachowania - został przekroczony limit wygenerowanych raportów dla repozytorium. 

Dla zabezpieczenia przed takim zachowaniem wprowadzono mechanizm przetwarzania transakcyjnego oraz blokady pesymistycznej. Kiedy student wysyła żądanie generowania raportu, w aplikacji serwera rozpoczynana jest transakcja aplikacyjna. Jeżeli wewnątrz transakcji dojdzie do wyrzucenia wyjątku spowodowanego naruszeniem zasad logiki biznesowej, to wszystkie akcji związane z bazą danych zostaną odwołane i baza wróci do stanu w jakim była przed rozpoczęciem transakcji aplikacyjnej. Wewnątrz transakcji w przypadku generowania raportu został wprowadzony mechanizm blokady pesymistycznej, który powoduje, że kiedy pierwsza transakcja (TX1) utworzona żądaniem pierwszego studenta odczyta rekord repozytorium z bazy danych - ten rekord zostanie zablokowany i jeżeli inna transakcja spróbuje odczytać ten rekord to zmoże to zrobić dopiero po zakończeniu transakcji TX1. Po nałożeniu blokady pesymistycznej aplikacja sprawdzi czy nie przekroczono limit wygenerowanych repozytoriów i w zależności od tego - albo będzie kontynuować przetwarzanie (w przypadku jeżeli nie przekroczono), albo zwróci wyjątek który zostanie przekształcony w kod HTTP 409 Confilct.

Tabela 3.2.4. Oś czasu przeplotu żądań podczas generowania raportów w sytuacji gdy limit wykorzystanych biletów jest równy 2 i zastosowano mechanizmy przetwarzania transakcyjnego oraz blokady pesymistycznej.

| Czas | Wątek A (student 1)                                                                                                                                                                           | Wątek B (student 2)                                                                                                                                                               | Ilość wygenerowanych raportów biletów (limit = 2) |
| ---- | --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | ------------------------------------------------- |
| T1   | Pobranie danych dla widoku GUI.                                                                                                                                                               |                                                                                                                                                                                   | 1                                                 |
| T2   |                                                                                                                                                                                               | Pobranie danych dla widoku GUI.                                                                                                                                                   | 1                                                 |
| T3   | Żądanie generowania raportu. Nałożenie blokady pesymistycznej. Odczyt z bazy danych ilości wygenerowanych raportów i sprawdzenie czy nie przekroczono limit 1 < 2. Rozpoczęcie przetwarzania. |                                                                                                                                                                                   | 1                                                 |
| T4   |                                                                                                                                                                                               | Żądanie generowania raportu. Oczekiwanie na blokadę pesymistyczną.                                                                                                                | 1                                                 |
| T5   | Zakończenie generowania raportu, zapis do bazy danych. Zakończenie transakcji. Zdjęcie blokady pesymistycznej.                                                                                |                                                                                                                                                                                   | 2                                                 |
| T6   |                                                                                                                                                                                               | Nałożenie blokady pesymistycznej.Odczyt z bazy danych ilości wygenerowanych raportów i sprawdzenie czy nie przekroczono limit 2 < 2. Zwrócenie kodu odpowiedzi HTTP 409 Conflict. | 2                                                 |

Poniżej zaprezentowano przebieg komunikacji między klientami korzystającymi z przeglądarek i posiadającymi poziom dostępu "student" a aplikacją serwerową jak w powyższej sytuacji.

![3.2.4.png](etap3/img/3.2.4.png)

Rys. 3.2.4 Pobranie danych o repozytorium dla studetów.

![3.2.5.png](etap3/img/3.2.5.png)

Rys. 3.2.5. Pomyślne wykonanie żądania pierwszego studenta.

![3.2.1.png](etap3/img/3.2.6.png)

Rys. 3.2.6 Błąd wykonania żądania drugiego studenta.

![3.2.7.png](etap3/img/3.2.7.png)

Rys. 3.2.7. Widok po odświeżeniu strony.


### Aktualizacja danych bazowych prowadząca do aktualizacji wartości agregatu

Przykładem problemu aktualizacji danych bazowych prowadząca do aktualizacji wartości agregatu w naszej aplikacji jest Wykonanie skanowania raportów w przedmiocie przez prowadzącego.
Po wygenerowaniu raportu, jego encja zawiera informację o średnim wskaźniku plagiatu wśród wszystkich przeskanowanych repozytoriów ta wartość jest wyliczana na podstawie danych samego raportu, i nie jest wartością agregowaną. Wartość agregowana znajduje się w encji przedmiotu i jest to średnia wartość podobieństwa wyliczona ze wszystkich średnich wartości w raportach tego przedmiotu.

Tabela 3.2.5. Oś czasu przeplotu żądań w wyniku którego doszło do niespójności wartości agregowanej

| Czas | Wątek A (prowadzący 1)                                                                               | Wątek B (prowadzący 2)                                                                               | Agregowana wartość w encji subject (średnia ocen) | Wpisy w bazie danych raportów do obliczenia agregowanej wartości       |
| ---- | ---------------------------------------------------------------------------------------------------- | ---------------------------------------------------------------------------------------------------- | ------------------------------------------------- | ---------------------------------------------------------------------- |
| T1   | Odczyt danych z bazy danych.                                                                         |                                                                                                      | 20%                                               | {<br>...<br>20%<br>}                                                   |
| T2   |                                                                                                      | Odczyt danych z bazy danych.                                                                         | 20%                                               | {<br>...<br>20%<br>}                                                   |
| T3   | Żądanie generowania raportu. Odczyt                                                                  |                                                                                                      | 20%                                               | {<br>...<br>20%<br>}                                                   |
| T4   |                                                                                                      | Żądanie generowania raportu.                                                                         | 20%                                               | {<br>...<br>20%<br>}                                                   |
| T5   | Zakończenie generowania raportu, obliczenie agregowanej wartości, zapis wyniku do bazy danych. (10%) |                                                                                                      | 15%                                               | {<br>...<br>20%<br>},<br>{<br>...<br>10%<br>}                          |
| T6   |                                                                                                      | Zakończenie generowania raportu, obliczenie agregowanej wartości, zapis wyniku do bazy danych. (30%) | 25%                                               | {<br>...<br>20%<br>},<br>{<br>...<br>10%<br>},<br>{<br>...<br>30%<br>} |
W przykładzie zaprezentowanym w tabeli 3.2.5 doszło do niepożądanego zachowania - niespójności wyliczonego agregatu. 

Dla zabezpieczenia przed danym problemem zastosowano obliczenie agregowanej danej w kwerendzie SQL zamiast kodu aplikacji, przetwarzania transakcyjnego oraz mechanizm blokady optymistycznej opartej na polu wersji z możliwością powtórzenia transakcji jeżeli doszło do jednoczesnego nadpisania tej wartości. Co prawda blokada optymistyczna generuje jeszcze dodatkowy problem - jeżeli będzie obliczona wartość agregowana, to nie będzie możliwa aktualizacja danych o przedmiocie, na przykład nazwy. Ten problem wyeliminowaliśmy przeniesieniem agregowanej wartości do oddzielnej encji subject_statistics, której rekord powiązany z danym przedmiotem jest aktualizowany zamiast samego przedmiotu.

Tabela 3.2.6 Oś czasu przeplotu żądań związanych z obliczeniem wartości agregowanej z zastosowaniem mechanizmów obliczenia wartości w kwerendzie SQL, przetwarzania transakcyjnego, blokady optymistycznej oraz przeniesienia wartości agregowanej do oddzielnej encji.


| Czas | Wątek A (prowadzący 1)                                                                               | Wątek B (prowadzący 2)                                                                               | Agregowana wartość  w encji subject (średnia ocen) | Wpisy w bazie danych raportów do obliczenia agregowanej wartości       |
| ---- | ---------------------------------------------------------------------------------------------------- | ---------------------------------------------------------------------------------------------------- | -------------------------------------------------- | ---------------------------------------------------------------------- |
| T1   | Odczyt danych z bazy danych.                                                                         |                                                                                                      | {<br>...<br>wersja=1<br>średnia=20%<br>}<br>       | {<br>...<br>20%<br>}                                                   |
| T2   |                                                                                                      | Odczyt danych z bazy danych.                                                                         | {<br>...<br>wersja=1<br>średnia=20%<br>}           | {<br>...<br>20%<br>}                                                   |
| T3   | Żądanie generowania raportu.                                                                         |                                                                                                      | {<br>...<br>wersja=1<br>średnia=20%<br>}           | {<br>...<br>20%<br>}                                                   |
| T4   |                                                                                                      | Żądanie generowania raportu.                                                                         | {<br>...<br>wersja=1<br>średnia=20%<br>}           | {<br>...<br>20%<br>}                                                   |
| T5   | Zakończenie generowania raportu, obliczenie agregowanej wartości, zapis wyniku do bazy danych. (10%) |                                                                                                      | {<br>...<br>wersja=1<br>średnia=15%<br>}           | {<br>...<br>20%<br>},<br>{<br>...<br>10%<br>}                          |
| T6   |                                                                                                      | Zakończenie generowania raportu, obliczenie agregowanej wartości, zapis wyniku do bazy danych. (30%) | {<br>...<br>wersja=1<br>średnia=20%<br>}           | {<br>...<br>20%<br>},<br>{<br>...<br>10%<br>},<br>{<br>...<br>30%<br>} |

Poniżej zaprezentowano przebieg komunikacji między klientami korzystającymi z przeglądarek i posiadającymi poziom dostępu "prowadzący" a aplikacją serwerową jak w powyższej sytuacji.

![3.2.8.png](etap3/img/3.2.8.png)

Rys. 3.2.8 Pobranie danych o przedmiocie dla prowadzących.

![3.2.9.png](etap3/img/3.2.9.png)

Rys. 3.2.9 Rozpoczęcie akcji analizy raportów.

![3.2.10.png](etap3/img/3.2.10.png)

Rys. 3.2.10 Wynik analizy raportów dla prowadzącego 1.

![3.2.11.png](etap3/img/3.2.11.png)

Rys. 3.2.11 Wynik analizy raportów dla prowadzącego 2.

![3.2.12.png](etap3/img/3.2.12.png)

Rys. 3.2.10 Wynik analizy raportów dla prowadzącego 1 po odświeżeniu strony.