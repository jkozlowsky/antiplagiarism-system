
## 2.3. Uwierzytelnianie i poświadczenia

Proces pozyskiwania poświadczeń w aplikacji oparty jest na mechanizmie tokenów JWT (JSON Web Token) i przebiega z wykorzystaniem protokołu HTTP.

### Proces pozyskiwania poświadczeń z perspektywy klienta

1. Użytkownik w aplikacji klienckiej SPA wprowadza swoje dane logowania (login oraz hasło) w dedykowanym formularzu.

2. Aplikacja wysyła asynchroniczne żądanie POST na endpoint backendowy odpowiedzialny za logowanie (`/api/auth/login`). Żądanie to przenosi w ciele poświadczenia w formacie JSON.

3. Backend, po pomyślnej weryfikacji poprawności hasła w bazie danych, wykorzystuje serwis JwtAuthService do wygenerowania poświadczenia.

4. Klasa JwtAuthService tworzy token JWT, wstrzykując do niego role użytkownika oraz jego identyfikator, a następnie podpisuje go przy użyciu algorytmu HMAC-SHA512 (HS512). Tworzony jest również Refresh Token.

5. Serwer zwraca wygenerowane tokeny do klienta. Od tego momentu aplikacja kliencka dołącza Access Token do nagłówka każdego żądania wymagającego autoryzacji, stosując format: Authorization: Bearer <token>

![loginForm](etap2/img/formularz_logowania.png)
_Rys. 2.3.1. Formularz logowania w interfejsie użytkownika._
![loginPayload](etap2/img/logowanie_payload.png)
_Rys. 2.3.2. Żądanie HTTP POST zawierające poświadczenia logowania (JSON)._
![token](etap2/img/token.png)
_Rys. 2.3.3. Odpowiedź serwera z wygenerowanym tokenem dostępu (Access Token)._

### Przykładowe poświadczenie, identyfikacja i autoryzacja

Format poświadczenia to ciąg znaków Base64URL podzielony na trzy części (Nagłówek, Ładunek/Payload, Sygnatura) oddzielone kropkami. Po zdekodowaniu sekcji ładunku (Payload), poświadczenie w systemie przyjmuje następującą formę jawną:

```json
{
  "roles": [
    "ADMIN"
  ],
  "sub": "kdawidtest",
  "iat": 1778191098,
  "exp": 1778192898
}
```
_Listing 2.3.1. Poświadczenia logowania (JSON)._

![decodedToken](etap2/img/jwtio.png)
_Rys. 2.3.4. Jawna struktura poświadczenia zdekodowana z formatu JWT._

Dane zawarte w poświadczeniu jednoznacznie decydują o uprawnieniach w systemie:

Identyfikacje użytkownika zapewnia roszczenie sub. W klasie JwtAuthFilter metoda extractUsername(token) odczytuje tę wartość (np. "kdawidtest"), co pozwala serwerowi zidentyfikować nadawcę żądania bez konieczności odpytywania bazy danych.

Ustalenie autoryzacji zapewnia roszczenie roles. Metoda extractRoles(token) pobiera tablicę ról z tokena, a następnie mapuje je na obiekty SimpleGrantedAuthority. Na ich podstawie budowany jest obiekt UsernamePasswordAuthenticationToken umieszczany w SecurityContextHolder, który decyduje o dostępie do zabezpieczonych endpointów.

### Mechanizm zapewniający wiarygodność poświadczeń

Wiarygodność tokenów JWT gwarantuje kryptograficzna sygnatura dodawana na końcu tokena. Serwer podpisuje ładunek za pomocą tajnego klucza (Secret Key) zakodowanego w formacie Base64. Klucz ten (wstrzykiwany do JwtAuthService ze zmiennych środowiskowych) nie jest znany aplikacji klienckiej.

Podczas każdego żądania do API, dedykowany filtr JwtAuthFilter przechwytuje nagłówek Authorization. Jeśli token jest obecny, metoda extractAllClaims z pomocą biblioteki jjwt kryptograficznie weryfikuje poprawność podpisu. Jeśli struktura tokena lub jakakolwiek rola zostałyby zmienione przez użytkownika lub atakującego, podpis przestanie się zgadzać, a serwer natychmiast odrzuci żądanie.

![tokenDev](etap2/img/token-dev.png)
_Rys. 2.3.5. Nagłówki autoryzowanego żądania HTTP z widocznym tokenem Bearer._

### Scenariusze prowadzące do braku możliwości kontynuacji konwersacji

Posiadanie kryptograficznie poprawnego tokena JWT nie gwarantuje stałego dostępu do systemu. Konwersacja zostanie przerwana w następujących przypadkach:

1. Token wygenerowany przez JwtAuthService ma z góry zdefiniowany czas życia (roszczenie exp). Jeśli filtr JwtAuthFilter wykryje, że czas ten minął, przechwycony zostaje wyjątek ExpiredJwtException, a serwer zwraca kod HTTP 401 Unauthorized z komunikatem {"error": "Token expired"}.

2. Jakakolwiek ingerencja w ciąg JWT spowoduje rzucenie wyjątku JwtException, co skutkuje odpowiedzią 401 Unauthorized i komunikatem {"error": "Invalid token format"}.

3. Ponieważ architektura API jest bezstanowa, serwer nie przechowuje aktywnych sesji w pamięci. Skuteczne wylogowanie odbywa się poprzez usunięcie tokena JWT ze struktury pamięci przeglądarki oraz unieważnienie ewentualnego Refresh Tokena po stronie bazy danych.

4. Mimo poprawnego odczytania tokena w filtrze, usługi biznesowe wywoływane w dalszych etapach (np. interceptory zdefiniowane w aplikacji) mogą rzucić wyjątkami takimi jak AccountIsBlockedException czy AccessLevelNotAssignedException, które zablokują wykonanie transakcji w bazie danych pomimo poprawnego uwierzytelnienia na poziomie HTTP.