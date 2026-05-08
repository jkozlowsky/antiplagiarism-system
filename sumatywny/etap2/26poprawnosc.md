
## 2.6. Poprawność danych i komunikaty błędów

### Format danych.
Komunikacja pomiędzy klientem a instancją serwera aplikacyjnego przebiega za pomocą interfejsu REST API, 
wykorzystując protokół HTTPS. Zarówno żądania klienta, jak i odpowiedzi serwera wykorzystują w swoim ciele format JSON.

<pre>
    <code>
        {
        "name":"Janek",
        "surname":"Kozłowski"
        }
    </code>
</pre>
Listing 2.6.1 Przykład żądania klienta. Żądanie PATCH na punkt końcowy (/api/account/update-myself). 
<pre>
    <code>
        {
        "account": {
            "id":"d2000aaa-346c-4a9d-8442-9cec67c010ab",
            "version":11,
            "versionHash":null,
            "login":"jkozlowsky",
            "password":null,
            "isVerified":true,
            "isBlocked":false,
            "name":"Janek",
            "surname":"Kozłowski",
            "email":"email@gmail.com",
            "lastLoginSuccessDateTime":"2026-05-07T23:33:28.091563"
            },
            "accessLevels":[{
                "accessLevelName":"STUDENT",
                "active":true,
                "id":"9c0c103c-9729-4852-b1fd-65bdec29bc7e",
                "version":0
                },
                {
                "accessLevelName":"ADMIN",
                "active":true,
                "id":"b312b7ae-159d-4978-a654-8ee86e7b4ac9",
                "version":0}]
        }
    </code>
</pre>
Listing 2.6.2 Przykład odpowiedzi serwera na żądanie z listingu 2.6.1 zakończone statusem 200 OK. 
### Zasady poprawności danych opisujących konto klienta.
#### Poprawność wewnętrzna.
Poprawność wewnętrzna dotyczy struktury i formatu poszczególnych pól obiektu przesyłanego przez klienta, 
niezależnie od stanu pozostałych danych w systemie. Reguły te są deklarowane jako adnotacje Bean Validation 
bezpośrednio na polach klas DTO, a ich weryfikacja jest wyzwalana przez adnotację @Validated na parametrze kontrolera.

Tabela 2.6.1 Ograniczenia obowiązujące dla klasy RegisterAccountDTO, stosowanej przy rejestracji nowego konta w systemie.

| Pole     | Ograniczenia                                                                                                                                                                                                                                           |
|----------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| name     | Nie może być puste (@NotBlank), musi mieć długość między długość między 3–32 znakami (@Size), musi zaczynać się od litery, może zawierać litery, spacje, myślniki i apostrofy (@Pattern: ^[\p{L}][\p{L}\s\-']+$)                                       | 
| surname  | Te same zasady jak dla pola name.                                                                                                                                                                                                                      |   
| email    | Nie może być pusty (@NotBlank) i musi spełniać format adresu e-mail (@Email).                                                                                                                                                                          |      
| login    | Nie może być puste (@NotBlank), musi mieć długość między 3–32 znakami (@Size) i musi składać się wyłącznie z liter i cyfr (@Pattern: ^[a-zA-Z0-9]+$).                                                                                                  |      
| password | Nie może być puste (@NotBlank), musi mieć długość między 12–64 znaki (@Size), musi zawierać co najmniej jedną wielką literę i jeden znak specjalny ze zbioru `!@#$%^&*(),.?":{} (@Pattern({regexp = "^(?=.*[A-Z])(?=.*[!@#$%^&*(),.?\":{} I<>]).*$"})) |       

Naruszenie któregokolwiek z powyższych ograniczeń skutkuje rzuceniem wyjątku MethodArgumentNotValidException, 
który jest przechwytywany przez metodę handleMethodArgumentNotValid() w klasie GlobalExceptionHandler.

#### Poprawność zewnętrzna.
Poprawność zewnętrzna dotyczy unikalności wartości atrybutu w całym zbiorze danych. 
Encja Account deklaruje dwa unikalne indeksy na poziomie schematu bazy danych:
<pre>
    <code>
        public static final String UNIQUE_LOGIN_INDEX_NAME = "account_login_key";
        public static final String UNIQUE_EMAIL_INDEX_NAME = "account_email_key";
    </code>
</pre>
Listing 2.6.3 Indeksy zadeklarowane w encji Account.

Próba zapisania konta z loginem lub adresem e-mail już istniejącym w systemie powoduje rzucenie przez bazę danych 
wyjątku DataIntegrityViolationException. Wyjątek ten jest przechwytywany przez aspekt 
AccountConstraintViolationsHandlingInterceptor, który analizuje treść komunikatu o błędzie i przekształca 
go w odpowiedni wyjątek aplikacyjny AccountConstraintViolationException, dostarczając klientowi 
czytelny komunikat o przyczynie konfliktu.
<pre>
    <code>
        {
        "name":"Jan",
        "surname":"Kozlowski",
        "login":"jkozlowsky",
        "email":"251562@edu.p.lodz.pl",
        "password":"zaq123WSX.xx",
        "confirmPassword":"zaq123WSX.xx",
        "termsAccepted":true
        }
    </code>
</pre>
Listing 2.6.4 Ciało żądanie rejestracji nowego użytkownika w systemie z loginem już istniejącym w bazie danych.

<pre>
    <code>
Konto z podanym loginem już istnieje.
    </code>
</pre>
Listing 2.6.4 Ciało odpowiedzi serwera dla rejestracji nowego użytkownika w systemie z loginem już istniejącym w bazie danych (409 Conflict).

Aplikacja dostarcza klientowi informację o wyniku przetwarzania żądania w jeden z dwóch ustandaryzowanych formatów, 
obsługiwanych centralnie przez klasę GlobalExceptionHandler.

Pierwszym z nich jest łańcuch znaków, który jest stosowany dla błędów aplikacyjnych dziedziczących po klasie AppBaseException.
i błędów bezpieczeństwa. Treść jest tłumaczona przez mechanizm internacjonalizacji na język przypisany do konta użytkownika.

Drugim z nich jest obiekt ValidationErrorResponse. Jest on wykorzystywany przy błędach walidacji Bean Validation.
Zawiera on listę naruszeń, które wskazują konkretne pole, dla którego zaszła nieprawidłowość.

<pre>
    <code>
        {
        "violations":[
            {
            "name":"name",
            "message":"wielkość musi należeć do zakresu od 3 do 32"
            },
            {
            "name":"surname",
            "message":"wielkość musi należeć do zakresu od 3 do 32"
            }]
        }
    </code>
</pre>
Listing 2.6.4 Struktura obiektu ValidationErrorResponse zwracanego przy błędach walidacji. 
#### 1. Nieprawidłowy format danych.
![Rysunek 2.6.1](etap2/img/InvalidDataFormat.png)
Rysunek 2.6.1 Postman, Żądanie w formacie XML.

Wysłanie żądania z ciałem w formacie XML oraz nagłówkiem Content-Type: text/xml powoduje odrzucenie żądania przed 
próbą deserializacji. Serwer zwraca kod 415, ponieważ dany punkt końcowy akceptuje wyłącznie dane w formacie JSON.

![Rysunek 2.6.2](etap2/img/InvalidDataFormat2.png)
Rysunek 2.6.2 Postman, Żądanie w nieprawidłowym formacie JSON.

Wysłanie żądania z ciałem w formacie niezgodnym z JSON z nagłówkiem Content-Type: application/json 
powoduje rzucenie wyjątku HttpMessageNotReadableException przez bibliotekę Jackson podczas próby deserializacji.
Serwer zwraca kod 400.

#### 2. Niespełniona zasada poprawności wewnętrznej.
<pre>
<code>
$ curl -X POST https://team-1.proj-sum.it.p.lodz.pl/api/account/register \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Jan",
    "surname": "Kowalski",
    "email": "jan.kowalski@example.com",
    "login": "jan kowalski",
    "password": "Za_Krotkie1"
  }'
{
    "violations":[
        {"name":"password","message":"size must be between 12 and 64"},
        {
            "name":"login",
            "message":"Login can only contain letters and numbers."
        },
        {
            "name":"password",
            "message":"The password must contain at least one uppercase letter and one special character (!@#$%^&*(),.?\":{}|<>)."
        }
    ]
}
</code>
</pre>
Listing 2.6.5 curl, Wysłanie żądania niespełniającego reguł wewnętrznych. W odpowiedzi otrzymano kod 400.
#### 3. Niespełniona zasada poprawności zewnętrznej.
<pre>
<code>
$ curl -X POST https://team-1.proj-sum.it.p.lodz.pl/api/account/register \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Anna",
    "surname": "Nowak",
    "email": "anna.nowak@example.com",
    "login": "jkowalski",
    "password": "Tajne@Haslo123"
  }'

Account with the given login already exists.
</code>
</pre>
Listing 2.6.6 curl, Wysłanie żądania niespełniającego reguł zewnętrznych. W odpowiedzi otrzymano kod 409.
#### 4. Niedostarczenie poświadczeń wymaganych do autoryzacji.
<pre>
    <code>
$ curl -X GET https://team-1.proj-sum.it.p.lodz.pl/api/account/d2000aaa-346c-4a9d-8442-9cec67c010ab
{
    "error": "Unauthorized", 
    "message": "Brak lub niepoprawny token autoryzacyjny."
}
    </code>
</pre>
Listing 2.6.7 curl, Żądanie na chronioną ścieżkę bez poświadczeń.

Wysłanie żądania na chroniony punkt końcowy bez nagłówka Authorization powoduje, że filtr JwtAuthFilter
nie wykrywa tokenu. Serwer blokuje dostęp dla użytkownika nieuwierzytelnionego i wywołuje zarejestrowany 
w SecurityConfig handler authenticationEntryPoint, który zwraca kod 401.
#### 5. Dostarczenie poświadczeń niepozwalających na autoryzację.
<pre>
    <code>
$ curl -X GET https://team-1.proj-sum.it.p.lodz.pl/api/account/d2000aaa-346c-4a9d-8442-9cec67c010ab -H "Authorization: Bearer eyJhbGciOiJIUzUxMiJ9.eyJyb2xlcyI6WyJTVFVERU5UIl0sInN1YiI6ImFub3dhayIsImlhdCI6MTc3ODIwNTI4NSwiZXhwIjoxNzc4MjA3MDg1fQ.14N7osNBb3Tu7FhNuBLuLGS33BJ2w44ENY7sfzfwR4gH-JBUgLJmG_758KRfUTpcXP-_0Pg-3hX57Ho1yi8TkQ"

No access: Access Denied
    </code>
</pre>
Listing 2.6.8 curl, Żądanie na chronioną ścieżkę z nieodpowiednimi poświadczeniami.

Wysłanie żądania z tokenem JWT należącym do użytkownika nieposiadającego wymaganych uprawnień powoduje zwrócenie kodu 403.
#### 6. Dostarczenie poświadczeń zmanipulowanych poza kontekstem serwera.
<pre>
    <code>
$ curl -X GET https://team-1.proj-sum.it.p.lodz.pl/api/account/d2000aaa-346c-4a9d-8442-9cec67c010ab -H "Authorization: Bearer eyJhbGciOiJIUzUxMiJ9.eyJyb2xlcyI6WyJTVFVERU5UIl0sInN1YiI6ImFub3dhayIsImlhdCI6MTc3ODIwNTI4NSwiZXhwIjoxNzc4MjA3MDg1fQ.14N7osNBb3Tu7FhNuBLuLGS33BJ2w44ENY7sfzfwR4gH-JBUgLJmG_758KRfUTpcXP-_0Pg-3hX57Ho1yi8TkO"
{
    "error": "Invalid token format"
}
    </code>
</pre>
Listing 2.6.9 curl, Żądanie z poświadczeniami zmanipulowanymi poza kontekstem serwera. Niezgodny podpis JWT.
#### 7. Nieprawidłowy URL.
<pre>
<code>
$ curl -X GET https://team-1.proj-sum.it.p.lodz.pl/api/nieistniejaca/sciezka -H "Authorization: Bearer eyJhbGciOiJIUzUxMiJ9.eyJyb2xlcyI6WyJBRE1JTiJdLCJzdWIiOiJhZG1pbiIsImlhdCI6MTc3ODIwNjgyMywiZXhwIjoxNzc4MjA4NjIzfQ.vD27RLBnXoQE4t_iT9QVDa8joLF9JgrzN6AGjI62U-Zm2k-KXV9zuEg0aRGY4yx3piaf7KyWPKet3sHZ4Itdeg"
{
    "timestamp":"2026-05-08T02:21:04.072Z",
    "status":404,
    "error":"Not Found",
    "path":"/api/nieistniejaca/sciezka"
}
</code>
</pre>
Listing 2.6.10 curl, Wysłanie żądania na nieistniejący adres URL. 
#### 8. Nieprzewidziany błąd.

Nieprzewidziany błąd jest obsługiwany przez metodę handleAllUncaughtException() w klasie GlobalExceptionHandler, 
która przechwytuje wszystkie wyjątki RuntimeException nieobsłużone przez pozostałe handlery. 
Serwer zwraca kod 500 z opisem wyjątku w ciele odpowiedzi.

W celu uzyskania takiego błędu wyłączono kontener z bazą danych dla lokalnej instancji systemu.

![Rysunek 2.6.3](etap2/img/Internal.png)
Rysunek 2.6.3 Postman, Nieprzewidziany błąd.

