
## 2.4. Bezpieczeństwo przesyłanych danych

Architektura wdrożonego systemu opiera się na komunikacji pomiędzy środowiskiem klienta (przeglądarką internetową) a serwerem. Taki model transmisji eksponuje przesyłane informacje na szereg wektorów ataku. Wyróżniono trzy główne zagrożenia dla bezpieczeństwa danych w systemie:

### Fałszowanie tożsamości
Próba podstawienia nieautoryzowanej, podobnej do oryginalnej domeny stony, co ma na celu symulowanie poprawnego działania aplikacji i wyłudzanie od użytkowników danych poufnych.

### Utrata poufności 
Nieautoryzowany dostęp osób trzecich do przesyłanych komunikatów w sieci. Atakujący w ten sposób mógłby uzyskać dostęp do danych poufnych klienta, co mogłoby skutkować nawet przejęciem konta użytkownika.

### Naruszenie integralności
Ryzyko celowej manipulacji zawartością przesyłanych pakietów. Zmiany parametrów żądania lub odpowiedzi serwera stanowią naruszenie reguł logiki biznesowej i mogą prowadzić do niepożądanego zachowania systemu.

***Zastosowane rozwiązanie***

Aby wyeliminować opisane zagrożenia, we wdrożeniu systemu zastosowano rozwiązanie oparte na protokole HTTPS z wykorzystaniem serwera Nginx jako Reverse Proxy oraz punktu terminacji połączeń TLS (SSL Termination). Dodatkowo system wykorzystuje architekturę izolowanych sieci wirtualnych (Docker Network), która uniemożliwia bezpośredni dostęp z zewnątrz do usług aplikacyjnych i bazy danych.

Poniżej przedstawiono, w jaki sposób poszczególne składowe eliminują zdefiniowane zagrożenia:

***Eliminowanie poszczególnych zagrożeń:***

### Zabezpieczenia przed fałszowaniem wiadomości
Skonteneryzowana usługa Nginx została skonfigurowana tak, aby prezentować klientowi certyfikat wystawiony dla naszej domeny (tj. ```team-1.proj-sum.it.p.lodz.pl```). Został on wygenerowany i zautomatyzowany z wykorzystaniem narzędzia Certbot. Podczas nawiązywania połączenia klient przeprowadza proces weryfikacji kryptograficznej tego certyfikatu. Sprawdza jego ważność, pole nazwy domeny oraz poprawność podpisu cyfrowego w oparciu o łańcuch certyfikacji. Wdrożony certyfikat opiera się na kryptografii krzywych eliptycznych (ECDSA), dlatego klient weryfikuje ścieżkę zaufania aż do głównego urzędu certyfikacji, którym w tym przypadku jest ISRG Root X2, za pośrednictwem certyfikatu pośredniego E8. Poprawna weryfikacja daje klientowi gwarancję, że komunikuje się z autentycznym serwerem systemu.

![wyświetlenie certyfikatu](etap2/img/bezpieczenstwoDanych1.png)

Rys. 2.4.1. Widok ogólny w przeglądarce potwierdzający nawiązanie bezpiecznego połączenia z serwerem oraz ogólne informacje o certyfikacie.

![łańcuch E8](etap2/img/bezpieczenstwoDanych2.png)

Rys. 2.4.2. Szczegóły certyfikatu potwierdzające łańcuch zaufania (certyfikat pośredni E8 oraz główny urząd ISRG Root X2).

![łańcuch domenowy](etap2/img/bezpieczenstwoDanych3.png)

Rys. 2.4.3. Szczegóły certyfikatu potwierdzające łańcuch zaufania (certyfikat team-1.proj-sum.it.p.lodz.pl).

### Zabezpieczenia przed utratą poufności
Konfiguracja serwera Nginx wymusza wykorzystywanie wyłącznie nowoczesnych standardów bezpieczeństwa (dyrektywa ```ssl_protocols TLSv1.2 TLSv1.3;```) oraz automatycznie przekierowuje wszelkie niezabezpieczone żądania HTTP na bezpieczny port 443 (przekierowanie kodem 307). W trakcie fazy uzgadniania połączenia (TLS Handshake), serwer i klient wyliczają unikalny dla danej sesji klucz symetryczny. Od tego momentu wszystkie przesyłane dane są kryptograficznie zaszyfrowane. Nawet w przypadku ich przechwycenia przez osobę trzecią, zawartość pakietów pozostaje całkowicie nieczytelna i bezużyteczna dla atakującego.

![redirect.png](etap2/img/bezpieczenstwoDanych4.png)

Rys. 2.4.4. Dziennik sieciowy z narzędzi deweloperskich prezentujący automatyczne wymuszenie szyfrowania (przekierowanie ruchu HTTP na HTTPS).


### Zabezpieczenia przed naruszeniem integralności
Zastosowanie w konfiguracji Nginx preferencji silnych zestawów szyfrów (dyrektywa ```ssl_prefer_server_ciphers on;```) gwarantuje wykorzystywanie algorytmów kryptograficznych (np. AES-GCM, wprowadzony m.in. w TLS 1.3). Mechanizmy te realizują uwierzytelnione szyfrowanie, polegające na obliczaniu unikalnego kodu autoryzacji dla każdego przesyłanego bloku informacji. Dzięki temu odbiorca może jednoznacznie zweryfikować, czy pakiet nie został zmieniony w locie. Jakakolwiek modyfikacja bitów w trakcie transmisji powoduje błąd walidacji matematycznej, co natychmiast skutkuje odrzuceniem zmanipulowanych danych i zerwaniem połączenia sieciowego.

![zakladka networdl](etap2/img/bezpieczenstwoDanych5.png)

Rys. 2.4.5. Szczegóły techniczne sesji TLS potwierdzające zastosowanie protokołu TLS 1.3 oraz algorytmu uwierzytelnionego szyfrowania AES-256-GCM.