
## 8 Diagram UML klas komponentów aplikacji sieciowej 4 pkt


Poprawny diagram klas UML dla komponentów Spring (klas i interfejsów) stanowiących implementację aplikacji sieciowej, oddzielny dla każdego modułu
funkcjonalnego z wykazaną przynależnością klas do pakietów.

Diagram musi eksponować:
* zależności występujące pomiędzy poszczególnymi klasami i interfejsami,
* rodzaj komponentu wraz z określeniem cyklu jego życia (jeżeli jest on inny niż domyślny tzn. Singleton),
* metody biznesowe komponentów,
* ew. wyjątki weryfikowalne deklarowane przez poszczególne metody.

_Wymagane jest także zrealizowanie i udostępnienie w repozytorium kodu źródłowego następujących składowych projektu:_
* _komponenty modułu MOK - pełna implementacja,_
* _komponenty pozostałych modułów - zdefiniowane API, tzn. deklaracje klas i interfejsów, deklaracje metod w interfejsach, "puste" (np. rzucające wyjątek UnsupportedOperationException) implementacje metod w komponentach_
