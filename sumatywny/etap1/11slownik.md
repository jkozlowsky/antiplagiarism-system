## 1.1. Słownik pojęć dziedzinowych

### Pojęcia występujące w modelu danych utrwalanych w ramach projektu:

**Konto (Account)** - reprezentuje zarejestrowanego użytkownika systemu. Encja ta przechowuje dane uwierzytelniające,
adres email oraz historię prób logowania.

**Rola (role)** – definicja uprawnień lub funkcji pełnionej przez użytkownika
w systemie (np. administrator, prowadzący, student).

**Przedmiot (subject)** – jednostka dydaktyczna lub projektowa, w ramach której są tworzone repozytoria
i przeprowadzane analizy kodu.

**Zestaw reguł (rule_preset)** – konfiguracja parametrów dla danego przedmiotu. Określa m.in. wymagany poziom
raportowania widoczny dla studenta (student_report_level), limit możliwych prywatnych analiz,
które student może wykonać dla własnego kodu źródłowego, zawartego w repozytorium przedmiotu,
w trakcie jednej edycji przedmiotu (student_ticket_count) oraz zbiór dopuszczalnych etykiet zadania (tagów).
Parametry te konfiguruje prowadzący.

**Etykieta zadania (tag)** - etykieta tekstowa zdefiniowana przez prowadzącego. Wymusza na studentach konkretne
oznaczanie wersji kodu w Gitea, co pozwala systemowi na precyzyjne wyodrębnienie zadania do analizy.

**Harmonogram (schedule)** – plan terminów powiązanych z konkretnym przedmiotem, określający daty
(np. terminów oddania prac) i etykiety kodu z repozytorium (tag), który ma zostać wzięty pod uwagę
w trakcie automatycznej analizy kodu źródłowego zawartego w repozytoriach.

**Repozytorium (repository)** – przestrzeń do przechowywania kodu źródłowego powiązanego z danym przedmiotem.

**Raport (report)** - wynikowy zestaw danych wygenerowany przez silnik JPlag po zakończeniu analizy porównawczej
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
