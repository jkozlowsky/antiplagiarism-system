
## 10 Identyfikacja transakcji aplikacyjnych MOK 3 pkt


Dla każdego przypadku użycia _w ramach modułu MOK_ należy przedstawić wszystkie transakcje aplikacyjne z udziałem metod komponentów aplikacji sieciowej. W tym celu dla każdego
przypadku użycia należy wykazać metodę biznesową, która rozpoczyna każdą transakcję aplikacyjną wraz z sekwencją wszystkich wywoływanych metod biznesowych w
ramach transakcji aplikacyjnej z naniesionymi stosownymi adnotacjami (konfigurującymi propagację transakcji aplikacyjnej w strategii CMT) oraz używanymi klasami
encji JPA dla każdej wywołanej metody w granicach transakcji aplikacyjnej.

W przypadku zastosowania mechanizmu blokad pesymistycznych dla encji JPA wymagane wyszczególnienie ich lokalizacji w metodach biznesowych komponentów
wraz ze wskazaniem na jakich obiektach encji JPA oraz na jak długo jest zakładana blokada pesymistyczna.

Ponadto należy przedstawić wszystkie przypadki wykorzystywania mechanizmu blokad optymistycznych zastosowanych dla encji JPA w scenariuszach wymagających realizacji więcej niż jednej transakcji aplikacyjnej. Dla każdego scenariusza należy przedstawić:
* metodę biznesową komponentu dostępowego, która dokonuje wczytania danych w ramach scenariusza
* metodę biznesową komponentu dostępowego, która dokonuje modyfikacji danych w ramach scenariusza
* mechanizm zapewniający wiarygodność wersji encji JPA przekazywanej z warstwy prezentacji.
* jeżeli takowe istnieją - inne metody, których działanie może wywierać wpływ na mechanizm blokady optymistycznej w danym scenariuszu

W ramach prezentacji rozwiązania należy zacytować kod metod składających się na jeden, przykładowy scenariusz.

_Realizacja: Wszystkie metody biznesowe wszystkich komponentów w ramach MOK muszą być opatrzone stosownymi adnotacjami
konfigurującymi propagację transakcji aplikacyjnych.
Przedstawione rozwiązanie ustalania granic transakcji aplikacyjnych w strategii CMT i zastosowania mechanizmu blokad optymistycznych musi być zaimplementowane i
zastosowane do wszystkich klas / metod komponentów modułu MOK biorących udział w realizacji transakcji._