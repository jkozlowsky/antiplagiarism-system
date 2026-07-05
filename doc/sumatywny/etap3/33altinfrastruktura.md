## 3.3. Alternatywna infrastruktura wdrożeniowa

Zaprojektowany system opiera się na pełnej konteneryzacji z wykorzystaniem Dockera oraz automatyzacji wdrożeń (GitLab CI/CD). Architektura ta uniezależnia aplikację od konkretnego sprzętu czy dostawcy, co pozwala na bezproblemowe przeniesienie jej na dowolną infrastrukturę chmurową wspierającą systemy z rodziny Linux. Przyjęto założenie, że Zamawiający nie dysponuje własnym zapleczem sprzętowym, w związku z czym zaproponowano wykorzystanie wirtualnych serwerów prywatnych (VPS) od zewnętrznych dostawców.

### Analiza zapotrzebowania na zasoby

Z analizy obecnego środowiska wdrożeniowego na platformie oVirt wynika, że przydzielone zasoby są drastycznie przeszacowane w stosunku do realnych potrzeb aplikacji.

Obecnie dysponujemy maszyną wirtualną posiadającą 8 vCPU, 60 GiB RAM i 152 GiB przestrzeni dyskowej.

Pomiary wydajności na serwerze produkcyjnym wykazały, że:
* Sumaryczne zużycie pamięci RAM przez wszystkie pracujące kontenery (Nginx, React, Spring Boot, PostgreSQL, Gitea) wynosi **poniżej 1 GB**.
* Całkowite obciążenie pamięci RAM na poziomie systemu operacyjnego wynosi ok. **2,5 GB**.
* Użycie procesora w trybie standardowej pracy wynosi ułamki procenta (ponad 99% czasu bezczynności CPU).
* Faktyczne zajętość przestrzeni dyskowej przez system operacyjny, obrazy kontenerów oraz wolumeny z danymi wynosi ok. **16 GB**.

Dla zapewnienia płynnego działania aplikacji oraz zachowania bezpiecznego bufora pamięci na potrzeby procesów budowania CI/CD (uruchamianie testów, budowanie obrazów Docker, przechowywanie dzienników zdarzeń), rekomendowane minimalne parametry innej budżetowej maszyny to:
* **Procesor:** 2 do 4 vCPU
* **Pamięć RAM:** 4 GB – 8 GB
* **Dysk:** 40 GB – 80 GB SSD/NVMe

### Szacunkowe koszty utrzymania infrastruktury (stan na czerwiec 2026)

Poniższe zestawienie bazuje na publicznych, standardowych cennikach popularnych dostawców infrastruktury chmurowej, z uwzględnieniem obniżonych wymagań sprzętowych. Podane ceny są szacunkowe (netto).

| Dostawca | Usługa / Pakiet   | Parametry (vCPU / RAM / Dysk) | Szacunkowy koszt miesięczny (netto) |
| :--- |:------------------|:------------------------------|:------------------------------------|
| **Hetzner Online** | Cloud VPS (CX23)  | 2 vCPU / 4 GB / 40 GB NVMe    | ok. 5,52 EUR (~24 PLN)              |
| **Hetzner Online** | Cloud VPS (CPX33) | 4 vCPU / 8 GB / 80 GB NVMe    | ok. 8,60 EUR (~36,50 PLN)           |
| **OVHcloud** | VPS Essential     | 4 vCPU / 8 GB / 75 GB NVMe    | ok. 28,00 PLN                        |
| **DigitalOcean** | Basic Droplet     | 2 vCPU / 4 GB / 80 GB SSD     | ok. 24,00 USD (~95 PLN)             |

### Koszt domeny internetowej

Do prawidłowego funkcjonowania systemu w warunkach produkcyjnych (w tym bezpiecznego szyfrowania ruchu z wykorzystaniem certyfikatów SSL Let's Encrypt, prawidłowego działania polityki CORS oraz obsługi żądań Git) niezbędne jest posiadanie i utrzymanie domeny internetowej.

Koszty rynkowe domen u najpopularniejszych rejestratorów:
* **Koszt rejestracji (1. rok):** Zazwyczaj od 2 PLN do 20 PLN netto (często w ramach promocji).
* **Koszt odnowienia (kolejne lata):** Średnio od 60 PLN do 200 PLN netto rocznie (co przekłada się na około 5 PLN – 17 PLN miesięcznie).


### Rekomendacja
Biorąc pod uwagę doskonały stosunek ceny do oferowanych możliwości oraz niskie wymagania wdrożeniowe skonteneryzowanej aplikacji, zdecydowanie najbardziej opłacalnym i rekomendowanym rozwiązaniem jest wybór usługi **VPS Essential od OVHcloud**.

Połączenie usługi **OVHcloud** z utrzymaniem standardowej domeny internetowej (z końcówką `.pl`) zagwarantuje Zamawiającemu w pełni stabilne środowisko produkcyjne i pozwoli bez problemu zamknąć całkowite miesięczne koszty infrastruktury w przedziale **ok. 33 PLN – 45 PLN netto** (wliczając już w to uśredniony, miesięczny koszt odnowienia domeny w kolejnych latach).