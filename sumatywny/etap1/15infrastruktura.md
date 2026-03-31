## 1.5. Infrastruktura wdrożeniowa


![Diagram Wdrożenia](etap1/img/iw_diagram_wdrozenia.png)

Rysunek 1.5.1. Diagram perspektywy wdrożenia.

Prezentowany powyżej diagram przedstawia docelową architekturę systemu, która uwzględnia pełen przepływ danych po integracji wszystkich warstw oprogramowania.

Należy jednak zaznaczyć, że w ramach obecnego etapu projektu, kontener Serwera Aplikacyjnego nie został jeszcze zaopatrzony w kod warstwy logiki. Nginx tymczasowo serwuje statyczną stronę startową w formacie HTML bezpośrednio z kontekstu głównego (/). Serwer Nginx został skonfigurowany jako Reverse Proxy. W celu zapewnienia bezpieczeństwa, narzędzie to wymusza połączenie szyfrowane, automatycznie przekierowując wszelkie zapytania HTTP na protokół HTTPS.

Jednocześnie cała infrastruktura docelowa jest już fizycznie przygotowana na poziomie konfiguracji narzędzia Docker Compose i oczekuje na wdrożenie w kolejnym etapie prac.

Tabela 1.5.1. Oprogramowania i licencje inne niż wymagane, a które zostały wykorzystane w ramach poprawnego wdrożenia infrastruktury. 

| Oprogramowanie | Wersja | Rola w systemie                                          | Licencja           |
|----------------|--------|----------------------------------------------------------|--------------------|
| Certbot        | 4.2.0  | Generowanie certyfikatów TLS                             | Apache 2.0 License |
| Docker Engine  | 29.3.0 | Środowisko uruchomieniowe kontenerów                     | Apache 2.0 License |
| Docker Compose | 5.1.1  | Narzędzie do zarządzania wielokontenerową infrastrukturą | Apache 2.0 License |
| Nginx          | 1.29.6 | Serwer HTTP Proxy i serwer plików statycznych | 2-clause BSD License |
| PostgreSQL     | 18.3   | Relacyjny system zarządzania bazą danych | PostgreSQL License |


![Strona](etap1/img/iw_strona.png)
Rysunek 1.5.2. Zrzut ekranu z działającej domeny team-1.proj-sum.it.p.lodz.pl

![Certyfikat](etap1/img/iw_certyfikat.png)
Rysunek 1.5.3. Zrzut ekranu certyfikatu domeny team-1.proj-sum.it.p.lodz.pl.
