
## 3.2. Problem przetwarzania współbieżnego
Należy przedstawić przykładowe scenariusze, w których na skutek przetwarzania współbieżnego żądań pochodzących od wielu klientów mogłoby dojść do utrwalenia
danych nieaktualnych lub niespójnych z zasadami logiki aplikacji.

Scenariusze powinny dotyczyć trzech przypadków, po jednym:
* tzw. równoległa edycja (nieodwracalne nadpisanie danych aktualnych danymi nieaktualnymi)
* naruszenie zasad logiki biznesowej (np. przekroczenie limitu lub niedozwolona zmiana stanu obiektu)
* aktualizacja danych bazowych prowadząca do aktualizacji wartości agregatu

Zalecana forma: przedstawienie na osi czasu przeplotu wykonania dwóch wątków wraz ze zmianami wprowadzanymi w zbiorze danych aplikacji przez każdy z wątków i
wyraźnym zaznaczeniem, czym końcowy stan danych różni się od oczekiwanego.

Dla każdego z przypadków scharakteryzować mechanizm (lub kombinację mechanizmów), który został zastosowany w aplikacji w celu wyeliminowania przedstawionych
wcześniej problemów. Uzasadnić, w jaki sposób zastosowany mechanizm spełni swoje zadanie. Zalecana forma: powtórzenie ilustracji przeplotów na osi czasu z naniesieniem informacji
odnośnie dodatkowych operacji (warunki, zmiany danych) wykonywanych przez zastosowany mechanizm i nowego końcowego stanu danych.

Dla każdego z przypadków zademonstrować przebieg komunikacji między klientami a kontekstem serwera odtwarzający zaproponowany wcześniej scenariusz i
wykazujący, że zastosowany mechanizm spełnił swoje zadanie. Zadbać o to, aby możliwe było śledzenie odrębnych konwersacji poszczególnych klientów z kontekstem
serwera.

W przypadku aktualizacji agregatu wskazać mechanizm, który gwarantuje odwołanie aktualizacji danych bazowych w przypadku błędu aktualizacji agregatu.

_Wskazówka: w przypadku aplikacji internetowej, która obsługuje żądania klienta, do przeplotu może dojść w dwóch skalach czasu: naprzemienne wykonywanie żądań
(przeplot żądań) lub współbieżne wykonanie żądań (przeplot instrukcji). Zaproponowane rozwiązania muszą uwzględniać obie skale, natomiast demonstracje mogą
ograniczać się do przeplotu żądań._