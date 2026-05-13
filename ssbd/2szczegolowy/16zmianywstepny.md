
## 16 Zmiany - projekt wstępny

W rozdziale tym należy wymienić zmiany wprowadzone do sprawozdania wstępnego, zwięźle je charakteryzując.

### Zmiany: Rozdział 2 Moduły funkcjonalne, aktorzy, przypadki użycia

1. Dodano przypadek użycia MOK.15 - Pobierz konta użytkowników w systemie.

2. Dodano przypadek użycia MOK.16 - Wyświetl szczegóły swojego konta.

3. Poprawiono diagramy przypadków użycia dla aktorów:
- Administrator, 
- Prowadzący, 
- Prowadzący dodatkowy.

### Zmiany: Rozdział 3 Struktury relacyjnej bazy danych

Względem etapu wstępnego, w rozdziale 3 wprowadzono następujące modyfikacje:

1. Zrezygnowano z wykorzystywania wbudowanych funkcji silnika PostgreSQL (takich jak gen_random_uuid() czy NOW()) jako domyślnych wartości w schemacie strukturalnym (DDL). Generowanie identyfikatorów oraz znaczników czasu przeniesiono na warstwę ORM (Hibernate), co udokumentowano w sekcji decyzji architektonicznych.

2. Uwzględniono zmiany pochodzące z nowszych migracji bazy danych (do wersji V13). Rozszerzono tabelę account o kolumny obsługujące proces weryfikacji e-mail (m.in. is_verified, activation_token) oraz dodano flagę is_active do tabeli access_level.

3. Uzupełniono dokumentację o zupełnie nowe tabele: one_time_token (odpowiadającą za autoryzację 2FA i reset hasła) oraz email_revert_token (obsługującą wycofywanie zmiany adresu e-mail).

4. Uporządkowano sposób prezentacji tabel, wydzielając klucze obce oraz indeksy do dedykowanych sekcji pod każdą z tabel, co znacząco poprawiło czytelność dokumentacji.

5. Zaprezentowane skrypty DDL i DML zostały zaktualizowane tak, aby odzwierciedlały ostateczny, skonsolidowany stan bazy po przejściu wszystkich migracji narzędzia Flyway. Dodano również uzasadnienie dla użycia bloku programistycznego podczas startowego zasilania bazy (Seed).

### Zmiany: Rozdział 5 Objekty encji

1. Dodano encje OneTimeToken, EmailRevertTokenEnt oraz zaktualizowano encję Account na liście encji oraz w macierzy związków. 
2. Poprawiono związek Account -> LanguageDict na Mutable.


### Zmiany: Rozdział 6 Diagram encji
1. Dodano encje OneTimeToken, EmailRevertTokenEnt oraz zaktualizowano encję Account na diagramie encji. 