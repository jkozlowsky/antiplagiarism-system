
## 21 Zgłaszane wyjątki 4 pkt


Należy zaprezentować w formie listy lub tabeli wszystkie wyjątki aplikacyjne reprezentujące błędy specyficzne dla logiki biznesowej. 

Dla każdego z takich wyjątków należy określić co najmniej:
* typ i miejsce/miejsca zgłoszenia wyjątku (nazwy komponentów i ich metod biznesowych, w których może zostać wyjątek zgłoszony),
* możliwe przyczyny wystąpienia wyjątku (np. niespełnianie założonych asercji przez parametry metody; obsługa innego wyjątku, w szczególności związanego z działaniem mechanizmu blokad optymistycznych - w takim przypadku należy krótko scharakteryzować okoliczności zgłaszania tego wyjątku),
* stwierdzenie, czy wyjątek jest obsługiwany w warstwie logiki biznesowej (w takim przypadku należy podać klasę komponentu i metodę zawierającą stosowny segment obsługi wyjątku) czy też zostaje zamieniony na odpowiednią odpowiedź HTTP, w takim wypadku należy podać kod błędu i komunikat zwrócony w odpowiedzi
* stwierdzenie, czy wystąpienie wyjątku prowadzi do odwołania aktualnie prowadzonej transakcji aplikacyjnej.

Należy zaprezentować przykładowy kod zapewniający wygenerowanie odpowiedzi HTTP usługi REST z odpowiednim kodem błędu i komunikatem, jako formę obsługi wyjątku wyjątku aplikacyjnego.

W taki sam sposób należy zaprezentować obsługę wyjątków systemowych (zgłaszanych przez kontener) dodając kod mechanizmu odpowiedzialnego za wygenerowanie odpowiedzi HTTP jako formy obsługi, wraz z przykładowym segmentem przepakowującym taki wyjątek, jeżeli taki został zastosowany. W tym przypadku lista wyjątków musi obejmować co najmniej:
* wyjątek/wyjątki związane z kontrolą dostępu do wywoływania metod biznesowych komponentów,
* wyjątek/wyjątki związane z brakiem zatwierdzenia transakcji aplikacyjnych (np. wykonanie transakcji aplikacyjnej przewidzianej do odwołania),

W przypadku, gdy obsługa takich wyjątków jest całkowicie zautomatyzowana przez kontener, zamiast kodu obsługi należy pokrótce opisać sposób obsługi i zaprezentować ewentualne ustawienia konfiguracji związane z nim. 

_Realizacja: wszystkie klasy planowanych wyjątków aplikacyjnych muszą być zadeklarowane w oddzielnym pakiecie klas.
Należy zwrócić uwagę na to, że wyjątkiem może zakończyć się operacja zestawiania / rozłączania związku między obiektami encji.
Prezentowane rozwiązania muszą być zaimplementowane przynajmniej w module MOK._