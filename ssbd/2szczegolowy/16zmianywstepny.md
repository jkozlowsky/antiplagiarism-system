
## 16 Zmiany - projekt wstępny

W rozdziale tym należy wymienić zmiany wprowadzone do sprawozdania wstępnego, zwięźle je charakteryzując.

### Zmiany: Rozdział 3 Struktury relacyjnej bazy danych

Względem etapu wstępnego, w rozdziale 3 wprowadzono następujące modyfikacje:

1. Zrezygnowano z wykorzystywania wbudowanych funkcji silnika PostgreSQL (takich jak gen_random_uuid() czy NOW()) jako domyślnych wartości w schemacie strukturalnym (DDL). Generowanie identyfikatorów oraz znaczników czasu przeniesiono na warstwę ORM (Hibernate), co udokumentowano w sekcji decyzji architektonicznych.

2. Uwzględniono zmiany pochodzące z nowszych migracji bazy danych (do wersji V13). Rozszerzono tabelę account o kolumny obsługujące proces weryfikacji e-mail (m.in. is_verified, activation_token) oraz dodano flagę is_active do tabeli access_level.

3. Uzupełniono dokumentację o zupełnie nowe tabele: one_time_token (odpowiadającą za autoryzację 2FA i reset hasła) oraz email_revert_token (obsługującą wycofywanie zmiany adresu e-mail).

4. Uporządkowano sposób prezentacji tabel, wydzielając klucze obce oraz indeksy do dedykowanych sekcji pod każdą z tabel, co znacząco poprawiło czytelność dokumentacji.

5. Zaprezentowane skrypty DDL i DML zostały zaktualizowane tak, aby odzwierciedlały ostateczny, skonsolidowany stan bazy po przejściu wszystkich migracji narzędzia Flyway. Dodano również uzasadnienie dla użycia bloku programistycznego podczas startowego zasilania bazy (Seed).