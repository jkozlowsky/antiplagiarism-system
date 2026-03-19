
## 11 Diagramy sekwencji oraz identyfikacja transakcji bazodanowych modułu MOK 2 pkt


Dla wymienionych poniżej przypadków użycia z MOK należy przedstawić diagram sekwencji obrazujący przebieg wykonania tego przypadku użycia:
* tworzenie konta o wybranym poziomie dostępu
* pobranie listy kont
* blokowanie lub odblokowanie konta
* zmiana hasła przez użytkownika
* zmiana danych konta
* dodanie poziomu dostępu do konta
* odebranie poziomu dostępu z konta

Diagram musi jako początek sekwencji uwzględniać element interfejsu użytkownika np. przycisk zapisu dla formularza dodawania konta, przycisk blokowania konta przy koncie widocznym ma liście kont itp. 

Wymagane wprowadzenie w diagramie elementu reprezentującego relacyjną bazę danych ze statycznymi strukturami, na której wykonywane są kwerendy SQL.

Przypadek użycia powinien być rozpoczynany przez aktora (lewa strona diagramu) aktywującego element GUI, następstwem czego jest wysłanie żądania HTTP do usługi REST i wywołanie metody biznesowej komponentu dostępowego kontrolera. 
Dla każdej wywołanej metody biznesowej komponentu dostępowego kontrolera należy wykazać wszystkie operacje realizowane z wykorzystaniem zarządcy encji EM (Entity Manager), dla każdej z tych operacji należy wymienić kompletny zbiór encji
JPA (dla każdej encji wymagane podanie nazwy jej klasy). Jeżeli przypadek użycia obejmuje tworzenie lub usuwanie obiektu encji, powinno być to zaznaczone na linii życia obiektu. 

Diagram sekwencji powinien na końcu (prawa strona diagramu) uwzględniać zbiór wykonanych kwerend SQL (SELECT, INSERT, UPDATE, DELETE)
na zawartości bazy danych, dla każdej kwerendy wymagane podanie kwalifikowanej nazwy tabeli bazodanowej (poprzedzonej nazwą jej schematu), na której kwerenda SQL będzie wykonywana.

Diagram powinien jednoznacznie określać rodzaj operacji wykonywanych z wykorzystaniem zarządcy encji EM na obiektach encji JPA (zalecane przyjęcie nazewnictwa
operacji względem operacji podstawowych z CRUD):
* w przypadku, gdy operacjom podlegają także inne obiekty encji (poprzez operacje kaskadowe, wyliczanie agregatów, operacje na obiektach powiązanych,
wczytanie danych słownikowych itp.) należy to jawnie uwzględnić na diagramie,
* należy jednak przy tym pamiętać, że sam fakt związku między obiektami encji nie powoduje automatycznego przeniesienia operacji między nimi (np. leniwe
ładowanie).

Diagram sekwencji powinien uwzględniać jedynie scenariusze pozytywne, gdzie w trakcie przetwarzania nie występują żadne błędy.

Na istniejących diagramach sekwencji należy wyszczególnić zakres każdej transakcji bazodanowej (zgodnie z wymogami ACID) poprzez zakreślenie grupy operacji
wykonywanych na obiektach encji. Każdą tak zaznaczonej transakcji należy dodatkowo oznaczyć:
* „RW” jeżeli transakcja jest nietrywialna
* „RO” jeżeli transakcja jest trywialna
* nazwą poziomu izolacji dobranego dla transakcji, poziom izolacji dla każdej transakcji powinien być najniższy dopuszczalny, zapewniający zachowanie
spójności danych

Szczególną uwagę należy zwrócić na przypadki użycia modyfikacji istniejących obiektów encji z użyciem formularza. Jakkolwiek z punktu widzenia użytkownika jest to
jeden przypadek użycia, z technicznego punktu widzenia mamy tu do czynienia z dwiema niezależnymi sekwencjami (wczytanie danych do formularza, zapisanie danych
z formularza). Powinno być to odwzorowane w sekwencji, jak również w identyfikacji transakcji. Alternatywnego scenariusza wynikającego z naruszenia kontroli
przepływu danych (np. próba zapisu danych bez ich uprzedniego wczytania) nie należy uwzględniać na diagramie.
Podobnie w przypadku, gdy przygotowanie formularza wymaga np. wczytania danych słownikowych, należy to uwzględnić na diagramie jako odrębną transakcję.
