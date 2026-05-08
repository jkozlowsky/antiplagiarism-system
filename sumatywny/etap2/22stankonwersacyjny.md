Danylo
## 2.2. Stan konwersacyjny

Za przechowywanie stanu konwersacyjnego systemu odpowiada warstwa klienta, ponieważ serwer stosuje architekturę bezstanową.

Przebieg komunikacji dla interaktywnej modyfikacji danych wygląda następująco:

1. Pobranie stanu: Klient wysyła żądanie `GET` do serwera z identyfikatorem obiektu. Serwer zwraca aktualny stan obiektu z bazy danych w postaci struktury `AccountWithAccessLevelsDTO`, która zawiera m.in. obecne dane oraz znacznik wersji (np. `versionHash`).
![status_odpowiedzi_get](/etap2/img/status_odpowiedz_get.png)
Rys. 2.2.1. Wysłane żądanie get i otrzymano kod statusu 200 OK.

![cialo_odpowiedz_get](/etap2/img/cialo_odpowiedz_get.png)
Rys. 2.2.2. Ciało odpowiedzi zawierające pole `versionHash`.

2. Utrzymanie stanu konwersacji: Klient zapisuje otrzymane dane w lokalnym stanie komponentu (np. `nameValue`, `surnameValue`). Przez cały czas trwania edycji komunikacja z serwerem nie zachodzi. Zmiany aplikowane są wyłącznie w lokalnej pamięci operacyjnej klienta.

3. Zapis): Po zatwierdzeniu formularza przez użytkownika, klient wysyła żądanie mutujące `PATCH` do serwera. Żądanie to nie wysyła całego obiektu, a jedynie DTO aktualizujące `AccountUpdateDTO` zawierające tylko `name` i `surname` oraz znacznik `versionHash` w nagłówku żądania `If-Match` uwiarygadniający, na jakiej wersji danych pracował użytkownik.

![cialo_zadanie_patch](/etap2/img/cialo_zadanie_patch.png)
Rys. 2.2.3. Ciało żądania PATCH zmieniające nazwisko konta.

![naglowki_zadanie_patch](/etap2/img/naglowki_zadanie_patch.png)
Rys. 2.2.4. Nagłówek "If-Match" przesyłany w żądaniu zawierający odebrany wcześniej skrót wersji.

4. Weryfikacja: Serwer weryfikuje żądanie, aplikuje zmiany na trwałe w bazie danych i zwraca zaktualizowany obiekt lub kod sukcesu.
![cialo_odpowiedz_patch](/etap2/img/cialo_odpowiedz_patch.png)
Rys. 2.2.5. Odpowiedż na żądanie PATCH, ze zmienionym nazwiskiem oraz nowym skrótem wersji.

Mechanizm chroniący przed zamierzoną lub niezamierzoną zmianą identyfikatora lub innych pól niemodyfikowalnych (np. login) opiera się na separacji modeli przez wzorzec DTO, w którym serwer do operacji edycji nie przyjmuje pełnego modelu bazodanowego, lecz dedykowany obiekt wejściowy (taki jak `AccountUpdateDTO`). Obiekt ten jest pzrekształcany na obiekt `AccountDTO` na serwerze który chociaż i posiada inne pola takie jak id lub login, ale te pola są oznaczone adnotacją `@Null` z wybraną grupą weryfikacyjną `OnUpdate`, wówczas metoda kontrolera obsługująca takie żądanie w parametrach przyjmuje obiekt `AccountDTO` z adnotacją `@Validated(OnUpdate.class)` dlatego żadne niepożądane dane nie są brane pod uwagę dla edycji oprócz potrzebnych nam (w przypadku powyżej) `name` i `surname`.

Przy próbie ustawienia niepoprawnego atrybutu w żądaniu do edycji - serwer zwraca status odpowiedzi Bad Request i w ciale odpowiedzi opisuje na czym polegał problem.
![](/etap2/img/zadanie_odpowiedz_400.png)
Rys. 2.2.6. Żądanie z próbą ustawienia atrybutu id oraz odpowiedz serwera.