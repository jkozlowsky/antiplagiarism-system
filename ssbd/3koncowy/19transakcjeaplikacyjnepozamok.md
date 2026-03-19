
## 19 Identyfikacja transakcji aplikacyjnych w pozostałych modułach 3 pkt


Dla każdego przypadku użycia _w ramach modułów innych niż MOK_ należy przedstawić wszystkie transakcje aplikacyjne z udziałem metod komponentów aplikacji sieciowej. 

Zawartość rozdziału należy utworzyć zachowując wszystkie zasady opisane w analogicznym rozdziale projektu szczegółowego. 

Dodatkowo wymagane jest zaprezentowanie mechanizmu ponawiania odwołanych transakcji aplikacyjnych także w przypadku, gdy odwołanie transakcji nastąpiło na skutek zdarzenia innego niż wystąpienie wyjątku. W ramach prezentacji rozwiązania należy zacytować fragment kodu wybranego (przykładowego) komponentu obrazujący zastosowanie rozwiązania.

_Realizacja: Wszystkie metody biznesowe wszystkich komponentów (we wszystkich modułach funkcjonalnych aplikacji) muszą być opatrzone stosownymi adnotacjami
konfigurującymi propagację transakcji aplikacyjnych.
Przedstawione rozwiązanie ustalania granic transakcji aplikacyjnych w strategii CMT i zastosowania mechanizmu blokad optymistycznych musi być zaimplementowane i
zastosowane do wszystkich klas / metod komponentów biorących udział w realizacji transakcji._