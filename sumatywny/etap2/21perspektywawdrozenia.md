
## 2.1. Szczegółowy diagram perspektywy wdrożenia

### Konteksty przetwarzania danych

Prezentowany powyżej diagram przedstawia szczegółową architekturę wdrożeniową systemu, opartą na środowisku kontenerowym Docker. Zgodnie z wymaganiami projektowymi, na schemacie wyraźnie zaznaczono granicę pomiędzy dwoma głównymi kontekstami przetwarzania danych:

#### Infrastruktura Klienta 
Obejmuje zasoby uruchamiane i wykonywane w systemie operacyjnym kontrolowanym przez użytkownika końcowego. Składa się z urządzenia klienta oraz działającej na nim przeglądarki internetowej. To właśnie w tym środowisku uruchomieniowym (po stronie klienta) wykonywana jest pobrana instancja aplikacji jednostronicowej (SPA), stanowiąca interfejs użytkownika.

#### Infrastruktura Serwera
Obejmuje środowisko wirtualizacji (oVirt) oraz maszynę wirtualną naszego zespołu. Wewnątrz środowiska Docker Engine znajdują się kontenery oraz wyodrębniono odizolowane wirtualne podsieci komunikacyjne.

### Warstwy systemu

W architekturze docelowej zdefiniowano następujące warstwy systemu oraz odpowiadające im kontenery:

#### Warstwa serwera pośredniczącego
Oparta na kontenerze z serwerem Nginx pełniącym rolę Reverse Proxy. Jest to jedyny publiczny punkt wejścia do systemu, nasłuchujący zapytań z sieci Internet (HTTP na porcie 80 oraz HTTPS na porcie 443). Przy czym należy zaznaczyć, że wszelki ruch na port 80 jest od razu przekierowywany na port 443. Warstwa ta odpowiada za ruch szyfrowany i bezpieczne kierowanie żądań do odpowiednich sieci wewnętrznych Dockera na podstawie ścieżki w adresie URL.

#### Warstwa prezentacji
Składa się z kontenera pełniącego rolę serwera plików statycznych (Nginx). Jego wyłącznym zadaniem jest serwowanie zbudowanych zasobów aplikacji do przeglądarki użytkownika za pośrednictwem sieci proxy-frontend. Warstwa ta nie realizuje logiki biznesowej i nie ma dostępu do bazy danych.

#### Warstwa serwera aplikacyjnego 
Kontener uruchamiający zbudowany artefakt aplikacji (plik .jar) w technologii Spring Boot (Java). Odbiera on żądania API z warstwy pośredniczącej przez sieć proxy-backend. Dodatkowo warstwa ta komunikuje się z zewnętrzną infrastrukturą (serwerem SMTP Brevo po porcie 587/TLS) w celu wysyłki powiadomień e-mail.

#### Warstwa składowania danych
Kontener z relacyjnym systemem zarządzania bazą danych (PostgreSQL 18.3). Znajduje się w izolowanej sieci backend-db, do której dostęp ma wyłącznie serwer aplikacyjny, komunikujący się za pomocą protokołu JDBC na porcie 5432.

### Warstwy zaprojektowane wewnątrz warstwy logiki biznesowej
Aby zagwarantować przejrzystość, skalowalność i zgodność ze wzorcami projektowymi, wewnątrz implementacji serwera aplikacyjnego wyodrębniono trzy kluczowe warstwy logiczne (zaznaczone wewnątrz artefaktu .jar na diagramie):

#### Warstwa Kontrolerów
Stanowi bezpośredni interfejs komunikacyjny backendu. Jej zadaniem jest przechwytywanie żądań HTTP przychodzących z warstwy serwera pośredniczącego, walidacja formatu danych wejściowych, wywoływanie odpowiednich akcji w warstwie serwisowej oraz formatowanie i zwracanie odpowiedzi (w formacie JSON) wraz z odpowiednimi kodami statusu HTTP.

#### Warstwa Serwisów
Jest to rdzeń decyzyjny aplikacji. Hermetyzuje kluczowe reguły biznesowe systemu (m.in. zarządzanie modułami MOK, logikę uwierzytelniania i autoryzacji). Warstwa ta koordynuje przepływ informacji, weryfikuje poprawność operacji z punktu widzenia biznesowego, zarządza transakcjami w bazie danych i przetwarza dane przed ich utrwaleniem.

#### Warstwa Repozytoriów
Odpowiada za fizyczną interakcję i persystencję danych w relacyjnej bazie PostgreSQL. Wykorzystuje technologię ORM (Spring Data JPA/Hibernate), co pozwala na płynne mapowanie obiektów domenowych języka Java (encji) na struktury tabelaryczne w bazie, maskując złożoność bezpośrednich zapytań SQL przed wyższymi warstwami aplikacji.
