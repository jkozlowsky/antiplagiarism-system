## 1.5. Infrastruktura wdrożeniowa


![Diagram Wdrożenia](etap1/img/iw_diagram_wdrozenia.png)

Rysunek 1.5.1. Diagram perspektywy wdrożenia.

Prezentowany powyżej diagram przedstawia architekturę wdrożeniową systemu, opartą na środowisku kontenerowym Docker. Jedynym publicznym punktem wejścia do aplikacji jest serwer Nginx, pełniący rolę Reverse Proxy. W celu zagwarantowania bezpieczeństwa ruchu sieciowego, narzędzie to wymusza połączenie szyfrowane (TLS), automatycznie przekierowując wszelkie nieszyfrowane zapytania HTTP na protokół HTTPS.

Środowisko zostało podzielone na dwie niezależne wirtualne podsieci: strefę dostępową (siec_frontend) oraz strefę wewnętrzną (siec_backend). Dzięki temu serwer bazy danych jest całkowicie odizolowany od ruchu z zewnątrz, a jedynym łącznikiem między obiema strefami pozostaje serwer aplikacyjny Tomcat.

Cała zaprezentowana infrastruktura docelowa jest już fizycznie zdefiniowana w konfiguracji narzędzia Docker Compose i oczekuje na wdrożenie kodu warstwy logiki w kolejnym etapie prac.

Tabela 1.5.1. Oprogramowania i licencje inne niż wymagane, a które zostały wykorzystane w ramach poprawnego wdrożenia infrastruktury. 

| Oprogramowanie | Wersja | Rola w systemie                                          | Licencja           |
|----------------|--------|----------------------------------------------------------|--------------------|
| Certbot        | 4.2.0  | Generowanie certyfikatów TLS                             | Apache 2.0 License |
| Docker Engine  | 29.3.0 | Środowisko uruchomieniowe kontenerów                     | Apache 2.0 License |
| Docker Compose | 5.1.1  | Narzędzie do zarządzania wielokontenerową infrastrukturą | Apache 2.0 License |
| Nginx          | 1.29.6 | Serwer HTTP Proxy i serwer plików statycznych            | 2-clause BSD License |
| PostgreSQL     | 18.3   | Relacyjny system zarządzania bazą danych                 | PostgreSQL License |
| OpenJDK     | 25     | Środowisko uruchomieniowe dla warstwy logiki             | GNU GPL v2 with Classpath Exception |
| Spring Boot     | 4.0.1   | Szkielet aplikacyjny warstwy logiki                 | Apache 2.0 License |


![Strona](etap1/img/iw_strona.png)
Rysunek 1.5.2. Zrzut ekranu z działającej domeny team-1.proj-sum.it.p.lodz.pl

![Certyfikat](etap1/img/iw_certyfikat.png)
Rysunek 1.5.3. Zrzut ekranu certyfikatu domeny team-1.proj-sum.it.p.lodz.pl.

![HTTP301](etap1/img/iw_http301.png)
Rysunek 1.5.4. Zrzut ekranu z narzędzi deweloperskich przeglądarki (zakładka Network) obrazujący automatyczne przekierowanie żądania.

Serwer Nginx poprawnie przechwycił próbę połączenia jawnym tekstem i zwrócił kod odpowiedzi 301 Moved Permanently, wymuszając w nagłówku Location ponowne połączenie z wykorzystaniem szyfrowanego protokołu HTTPS.

![HTTP200](etap1/img/iw_http200.png)
Rysunek 1.5.5. Zrzut ekranu z narzędzi deweloperskich przeglądarki stanowiący kontynuację poprzedniego żądania.

Przedstawia on poprawne nawiązanie bezpiecznego połączenia (HTTPS) z serwerem docelowym, zakończone zwróceniem żądanych zasobów statycznych (kod odpowiedzi 200 OK).