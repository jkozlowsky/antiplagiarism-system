
## 15 Zabezpieczenie transmisji HTTP 1 pkt

Przedstawić konfigurację aplikacji (fragmenty deskryptora) lub serwera pośredniczącego (proxy) wymuszającą korzystanie z szyfrowanego protokołu HTTPS w
komunikacji między przeglądarką WWW a serwerem. 

_Prezentowana konfiguracja musi być w całości zrealizowana w aplikacji._

Weryfikacja i zabezpieczenie transmisji w aplikacji zostały zrealizowane z wykorzystaniem wzorca architektonicznego **SSL Termination Proxy**. Za szyfrowanie ruchu pomiędzy przeglądarką klienta a systemem odpowiada serwer pośredniczący Nginx jako Reverse Proxy. Natomiast komunikacja w sieci wewnętrznej (od Nginx do kontenerów aplikacji) odbywa się po HTTP, co odciąża aplikację z narzutu kryptograficznego.

Wymuszenie korzystania z protokołu HTTPS, przy komunikacji między przeglądarką WWW a serwerem, zrealizowano poprzez konfigurację bloku serwera na porcie 80, który odpowiada statusem `301 Moved Permanently`, przekierowując cały ruch na port 443:

```bash
server {
listen 80;
server_name team-1.proj-sum.it.p.lodz.pl;
return 301 https://$host$request_uri;
}
```
_Listing 2.15.1 Fragment pliku nginx.conf odpowiedzialny za przekierowanie_

Konfiguracja bezpiecznego połączenia HTTPS (port 443) wykorzystuje darmowe certyfikaty urzędu Let's Encrypt (łańcuch certyfikatów w pliku fullchain.pem oraz klucz prywatny privkey.pem). Skonfigurowano obsługę wyłącznie bezpiecznych protokołów TLSv1.2 oraz TLSv1.3, odrzucając przestarzałe i podatne na ataki wersje SSL/TLS. Dodatkowo włączono obsługę protokołu HTTP/2 (http2 on;).

```bash
server {
        listen 443 ssl;
        http2 on;
        server_name team-1.proj-sum.it.p.lodz.pl;

        ssl_certificate /etc/letsencrypt/live/team-1.proj-sum.it.p.lodz.pl/fullchain.pem;
        ssl_certificate_key /etc/letsencrypt/live/team-1.proj-sum.it.p.lodz.pl/privkey.pem;

        ssl_protocols TLSv1.2 TLSv1.3;
        ssl_prefer_server_ciphers on;

        location / {
            proxy_pass http://frontend:80;
            proxy_set_header Host $host;
            proxy_set_header X-Real-IP $remote_addr;
            proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
            proxy_set_header X-Forwarded-Proto $scheme;
        }


        location /api/ {
            proxy_pass http://backend:8081;

            proxy_set_header Host $host;
            proxy_set_header X-Real-IP $remote_addr;
            proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
            proxy_set_header X-Forwarded-Proto $scheme;
        }
    }
```
_Listing 2.15.2 Fragment pliku nginx.conf z widocznymi certyfikatami oraz kierowaniem ruchem_

Aby aplikacja serwerowa była świadoma, że pierwotny ruch odbywa się po HTTPS (co jest niezbędne m.in. do poprawnego działania polityki CORS), w Nginx zastosowano nagłówki przekazujące `X-Forwarded-Proto $scheme;`. Po stronie aplikacji serwerowej, w pliku application.properties, włączono obsługę tych nagłówków wpisem `server.forward-headers-strategy=framework`.
