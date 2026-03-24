## 1.1. Słownik pojęć dziedzinowych

### Pojęcia występujące w modelu danych utrwalanych w ramach projektu:

**Konto (Account)** - centralna encja reprezentująca tożsamość użytkownika. 
Przechowuje dane uwierzytelniające, adres e-mail oraz historię prób logowania. 
Konto stanowi bazę, do której mogą być przypisane opcjonalne profile (Student, Nauczyciel, Administrator), 
co umożliwia jednej osobie pełnienie wielu funkcji w systemie jednocześnie.

**Profil Administratora (Admin)** - rozszerzenie konta o uprawnienia globalne, odpowiedzialne za zarządzanie 
kontami i rolami.

**Profil Studenta (Student)** - rozszerzenie konta, które umożliwia dostarczenie kodu źródłowego do repozytoriów 
oznaczonego odpowiednią Etykietą zadania. Posiada uprawnienia do podglądu własnych raportów 
(zgodnie z poziomem raportowania) oraz wykonywania analiz prywatnych.

**Profil Nauczyciela (Teacher)** - rozszerzenie konta pozwalające sprawować opiekę merytoryczną nad przedmiotem. 
Zakres jego uprawnień jest definiowany na poziomie powiązania z konkretnym przedmiotem (tabela teacher_subject) 
za pomocą flagi przywileju modyfikacji (has_modify_privilege). W zależności od wartości tej flagi użytkownik ten może 
mieć dostęp do edycji zestawu reguł, ustawiania dopuszczalnych etykiet zadań oraz ustalania harmonogramu przedmiotu.
W przeciwnym razie użytkownik będzie miał jedynie dostęp do wglądu w wygenerowane raporty.

**Przedmiot (Subject)** – jednostka dydaktyczna lub projektowa, w ramach której są tworzone repozytoria
i przeprowadzane analizy kodu.

**Zestaw reguł (Rule)** – konfiguracja parametrów dla danego przedmiotu. Określa m.in. wymagany poziom
raportowania widoczny dla studenta (student_report_level), limit możliwych prywatnych analiz,
które student może wykonać dla własnego kodu źródłowego, zawartego w repozytorium przedmiotu,
w trakcie jednej edycji przedmiotu (student_ticket_count) oraz zbiór dopuszczalnych etykiet zadania (tagów).
Parametry te konfiguruje prowadzący.

**Etykieta zadania (Tag)** - unikalna etykieta tekstowa zdefiniowana przez prowadzącego. Wymusza na studentach oznaczanie 
konkretnych wersji kodu w usłudze Gitea, co pozwala systemowi na precyzyjne pobranie odpowiedniej wersji projektu 
do analizy.

**Harmonogram (Schedule)** – plan terminów powiązanych z konkretnym przedmiotem, określający daty
(np. terminów oddania prac) i etykiety kodu z repozytorium (tag), który ma zostać wzięty pod uwagę
w trakcie automatycznej analizy kodu źródłowego zawartego w repozytoriach.

**Repozytorium (Repository)** – przestrzeń do przechowywania kodu źródłowego powiązanego z danym przedmiotem.

**Raport (Report)** - wynikowy zestaw danych wygenerowany przez silnik JPlag po zakończeniu analizy porównawczej
kodów źródłowych z wybranych repozytoriów. Raport zawiera zagregowane statystyki podobieństwa dla całego przedmiotu,
rankingi najbardziej zbliżonych par projektów oraz szczegółową wizualizację identycznych fragmentów kodu.

### Pojęcia odpowiadające danym ulotnym przetwarzanym przez system:

**Kolejka analiz** – tymczasowa lista zadań oczekujących na przetworzenie przez silnik porównawczy.
Pozwala na sprawne zarządzanie zasobami serwera, gdy wiele użytkowników wysyła żądanie wykonania analizy kodu
jednocześnie.

**Postać stokenizowana** — abstrakcyjna postać kodu źródłowego, wygenerowana przez silnik JPlag.
Kod zamieniany jest na ciąg symboli, co pozwala algorytmowi ignorować np. zmianę nazw zmiennych czy białe znaki.
Istnieje tylko w pamięci operacyjnej podczas analizy.

### Pozostałe pojęcia:

**JPlag** — zewnętrzna biblioteka stosowana jako silnik analizy porównawczej kodu źródłowego z repozytoriów
pod względem jego powtarzalności.

**Gitea** — zewnętrzna usługa hostingu systemów kontroli wersji Git, pełniąca funkcję
centralnego składowiska kodów źródłowych (repozytoriów) przesyłanych przez studentów.
System komunikuje się z Gitea, aby na podstawie danych z harmonogramu automatycznie pobierać
wersje projektów oznaczone odpowiednim znacznikiem (tagiem) do dalszego przetwarzania przez silnik JPlag.

**Wskaźnik podobieństwa** — procentowa wartość określająca stopień podobieństwa między porównywanymi kodami źródłowymi.

**Analiza prywatna** – proces weryfikacji kodu zainicjowany bezpośrednio przez studenta przed oficjalnym terminem 
oddania pracy. Liczba takich analiz jest ograniczona przez parametr z kolumny student_ticket_count w zestawie reguł 
i pozwala studentowi na samodzielną kontrolę oryginalności swojego rozwiązania.

**Poziom raportowania** – parametr określający zakres informacji zwrotnej prezentowanej studentowi po zakończeniu 
analizy.
