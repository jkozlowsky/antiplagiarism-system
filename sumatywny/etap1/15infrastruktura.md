## 1.5. Infrastruktura wdrożeniowa


![Diagram Wdrożenia](img/iw_diagram_wdrozenia.png)

Rysunek 1.5.1. Diagram perspektywy wdrożenia. 

Na maszynie wirtualnej skonfigurowano usługę firewalld (wykazać?), która dopuscza ruch z zewnątrz tylko na portach HTTP i HTTPS.  

Wewnątrz systemu operacyjnego skonfigurowano środowisko konteneryzacji (Docker Engine), w którym uruchomiono odpowiednie usługi, zapewniając ich izolację oraz poprawne działanie aplikacji.

Tabela 1.5.1. Oprogramowania i licencje inne niż wymagane, a które zostały wykorzystane w ramach poprawnego wdrożenia infrastruktury. 

| Oprogramowanie | Wersja | Rola w systemie              | Licencja           |
|----------------|--------|------------------------------|--------------------|
| Certbot        | 4.2.0  | Generowanie certyfikatów TLS | Apache 2.0 License |


![Strona](img/iw_strona.png)
Rysunek 1.5.2. Zrzut ekranu z działającej domeny team-1.proj-sum.it.p.lodz.pl

Serwer Nginx został skonfigurowany jako Reverse Proxy. W celu zapewnienia bezpieczeństwa, narzędzie to wymusza połączenie szyfrowane, automatycznie przekierowując wszelkie zapytania HTTP na protokół HTTPS. 

![Certyfikat](img/iw_certyfikat.png)
Rysunek 1.5.3. Zrzut ekranu certyfikatu domeny team-1.proj-sum.it.p.lodz.pl.
