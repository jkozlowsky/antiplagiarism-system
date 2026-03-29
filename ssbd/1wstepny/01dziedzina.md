
## 1 Dziedzina wykorzystania 1 pkt

**Przeznaczenie aplikacji**

* Krótki opis świadczonej przez aplikację funkcjonalności, należy krótko opisać reguły biznesowe określające zasady przetwarzania danych.
* Wśród przetwarzanych danych wskazać zasoby, dla których zachodzi konkurencja.
* Określenie z uzasadnieniem charakterystyki aplikacji (OLTP/OLAP).
* Sprecyzować stanowość lub bezstanowość aplikacji w warstwie logiki biznesowej i format wymiany dany pomiędzy warstwą prezentacji a warstwą logiki
biznesowej.
* Stos technologiczny dla warstwy logiki biznesowej.
* Łącznie max. 15 zdań.

Apliakcja służy do automatycznego wykrywania podobieństw w kodzie źródłowym poprzez integrację z usługą Gitea oraz 
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

**Słownik pozostałych pojęć i obiektów**
* Słownik wszystkich pozostałych występujących w projekcie pojęć i obiektów, które nie zostały wymienione w modelu danych. 
* Uwagi szczegółowe dot. tworzenia OBU słowników:
  * Forma: tabela lub lista 
  * Przedstawiona terminologia musi być jednoznaczna i obowiązuje zarówno w dalszej części dokumentacji, nazwach tabel oraz klas w kodzie aplikacji, 
  * czy też dokumentacji programistycznej. 
  * Składnia identyfikatorów musi być zgodna ze składnią obowiązującą w języku programowania Java, wymagane ustalenie wielkości liter dla
identyfikatorów oraz brak polskich znaków
