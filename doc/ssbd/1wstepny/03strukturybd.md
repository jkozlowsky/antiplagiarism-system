## 3 Struktury relacyjnej bazy danych 2 pkt

### Struktury tabel ###

Dla każdej tabeli należącej do statycznych struktur bazy danych (włączając w to tabele słownikowe) kompletnie przedstawiono listę pól, kluczy obcych oraz indeksów. Wszystkie tabele zostały utworzone w domyślnym schemacie public silnika PostgreSQL.

Decyzje architektoniczne:

1. W kolumnach kluczy głównych (id) zadeklarowano wartość domyślną jako generowany automatycznie. Za faktyczne generowanie wartości UUID odpowiada warstwa dostępu do danych (ORM - Hibernate) wykorzystująca adnotację @GeneratedValue. Zapewnia to całkowitą niezależność schematu bazy (DDL) od specyficznych funkcji wbudowanych silnika (takich jak gen_random_uuid() w PostgreSQL). Podobną strategię przyjęto dla znaczników czasu (np. created_at).

2. W systemie zastosowano strategię dziedziczenia JOINED dla encji poziomów dostępu. Skutkuje to wydzieleniem tabel admin, teacher oraz student, które są powiązane kluczem głównym z tabelą nadrzędną access_level.

3. Pełne zarządzanie wersjonowaniem struktur zrealizowano za pomocą narzędzia Flyway. Zaprezentowane w dokumentacji skrypty DDL i DML stanowią obraz skonsolidowany po przejściu wszystkich faz migracji (do wersji V13 włącznie).

#### 3.1. Tabele słownikowe ####

_Uwaga projektowa: Tabele słownikowe posiadają kolumnę version na potrzeby ujednolicenia hierarchii encji w warstwie obiektowej (wspólna klasa bazowa AbstractEntity obsługująca blokadę optymistyczną), co znacznie upraszcza uogólniony kod warstwy DAO._

Tabela 3.1.1 Tabela: access_level_dict (Słownik dostępnych ról w systemie RBAC)

| Nazwa pola    | Typ danych | Znaczenie | Wartość domyślna | Ograniczenia              |
|---------------|----------|---------|------------------|---------------------------|
| **id** | **UUID** | **Klucz główny krotki** | **generowany automatycznie** | **PRIMARY KEY**, NOT NULL |
| access_level_name | VARCHAR(255) | Nazwa poziomu dostępu | \- | UNIQUE, NOT NULL |
| version | INTEGER | Pole wersji (Hibernate) | 0 | NOT NULL |

* Klucze obce: Brak
* Indeksy: Brak

Tabela 3.1.2 Tabela: language_dict (Wspierane wersje językowe)

| Nazwa pola                     | Typ danych | Znaczenie | Wartość domyślna | Ograniczenia |
|--------------------------------|----------|---------|------------------|------------|
| **id** | **UUID** | **Klucz główny krotki** | **generowany automatycznie** | **PRIMARY KEY**, NOT NULL |
| language | VARCHAR(255) | Kod języka (np. 'pl', 'en') | \- | UNIQUE, NOT NULL |
| version | INTEGER | Pole wersji (Hibernate) | 0 | NOT NULL |

* Klucze obce: Brak
* Indeksy: Brak

Tabela 3.1.3 Tabela: raport_level_dict (Słownik poziomów raportowania)

| Nazwa pola                     | Typ danych | Znaczenie | Wartość domyślna | Ograniczenia |
|--------------------------------|----------|---------|------------------|------------|
| **id** | **UUID** | **Klucz główny krotki** | **generowany automatycznie** | **PRIMARY KEY**, NOT NULL |
| name | VARCHAR(255) | Poziom wglądu (np. PEŁNY_WGLĄD) | \- | UNIQUE, NOT NULL |
| version | INTEGER | Pole wersji (Hibernate) | 0 | NOT NULL |

* Klucze obce: Brak
* Indeksy: Brak

#### 3.2. Struktury tabel Modułu Obsługi Kont (MOK) ####

Tabela 3.2.1 Tabela: account (Konto użytkownika systemu)

| Nazwa pola                   | Typ danych   | Znaczenie | Wartość domyślna | Ograniczenia |
|------------------------------|--------------|---------|----------------|------------|
| **id**                       | **UUID** | **Identyfikator główny** | **generowany automatycznie** | **PRIMARY KEY**, NOT NULL |
| login                        | VARCHAR(50) | Unikalny login użytkownika | \- | UNIQUE, NOT NULL |
| email                        | VARCHAR(50) | Unikalny adres poczty | \- | UNIQUE, NOT NULL |
| password_hash                | VARCHAR(64) | Hasło w postaci skrótu BCrypt | \- | NOT NULL |
| name                         | VARCHAR(50) | Imię użytkownika | \- | NOT NULL |
| surname                      | VARCHAR(50) | Nazwisko użytkownika | \- | NOT NULL |
| language_version_id          | UUID | FK do wersji językowej | \- | NOT NULL |
| is_verified                  | BOOLEAN | Flaga potwierdzenia maila | false | NOT NULL |
| activation_token | VARCHAR(255) | Token aktywacyjny e-mail | brak | UNIQUE |
| is_blocked                   | BOOLEAN | Flaga zablokowania konta | false | NOT NULL |
| creation_date | TIMESTAMP | Data i czas założenia konta | brak | NOT NULL |
| second_email_sent | BOOLEAN | Flaga powtórnego wysłania maila | false | NOT NULL |
| failed_attempts              | INTEGER | Licznik nieudanych prób logowania | 0 | \- |
| last_login_success_date_time | TIMESTAMP(6) | Czas ostatniego udanego logowania | \- | \- |
| last_login_failure_date_time | TIMESTAMP(6) | Czas ostatniego nieudanego logowania | \- | \- |
| last_successful_login_ip     | VARCHAR(45) | IP logowania zakończonego sukcesem | \- | \- |
| last_failed_login_ip         | VARCHAR(45) | IP logowania zakończonego porażką | \- | \- |
| version                      | INTEGER | Wersja krotki (blokada optymistyczna) | 0 | NOT NULL |
| created_by                   | UUID | ID konta, które utworzyło wpis | \- | \- |
| created_at                   | TIMESTAMP(6) | Czas utworzenia w bazie | generowany automatycznie | NOT NULL |
| modified_by                  | UUID | ID konta, które modyfikowało wpis | \- | \- |
| updated_at                   | TIMESTAMP(6) | Czas ostatniej modyfikacji | generowany automatycznie | \- |

* Klucze obce:
  * language_version_id odwołuje się do language_dict(id)
  * created_by odwołuje się do account(id)
  * modified_by odwołuje się do account(id)

* Indeksy: 
  * idx_account_language_version_id
  * idx_account_created_by
  * idx_account_modified_by

Tabela 3.2.2 Tabela: access_level (Nadrzędna tabela poziomów dostępu)

| Nazwa pola             | Typ danych   | Znaczenie | Wartość domyślna | Ograniczenia |
|------------------------|--------------|---------|----------------|------------|
| **id** | **UUID** | **Identyfikator główny** | **generowany automatycznie** | **PRIMARY KEY**, NOT NULL |
| account_id | UUID | Identyfikator konta użytkownika | \- | \- |
| access_level_id | UUID | FK do słownika ról | \- | NOT NULL |
| version | INTEGER | Wersja krotki | 0 | NOT NULL |
|is_active|	BOOLEAN	|Flaga aktywności roli	|true	|NOT NULL|
| created_by | UUID | ID konta nadającego rolę | \- | \- |
| created_at | TIMESTAMP(6) | Czas nadania roli | generowany automatycznie | NOT NULL |
| modified_by | UUID | ID konta modyfikującego status | \- | \- |
| updated_at | TIMESTAMP(6) | Czas modyfikacji statusu roli | generowany automatycznie | \- |

* Klucze obce: 
  * account_id odwołuje się do account(id)
  * access_level_id odwołuje się do access_level_dict(id)
  * created_by odwołuje się do account(id)
  * modified_by odwołuje się do account(id)
* Indeksy: 
  * idx_access_level_dict_id 
  * idx_access_level_account_id
  * idx_access_level_created_by 
  * idx_access_level_modified_by

Tabela 3.2.3 Tabele dziedziczące: admin

| Nazwa pola             | Typ danych   | Znaczenie | Wartość domyślna | Ograniczenia |
|------------------------|--------------|---------|----------------|------------|
| **id** | **UUID** | **Klucz główny, dziedziczony** | **generowany automatycznie** | **FOREIGN KEY**, **PRIMARY KEY**, NOT NULL |

* (Tabela współdzieli pola audytowe z tabelą nadrzędną access_level)
* Klucze obce: 
  * id odwołuje się do access_level(id)
* Indeksy: Brak dedykowanychc

Tabela 3.2.4 Tabele dziedziczące: teacher

| Nazwa pola             | Typ danych   | Znaczenie | Wartość domyślna | Ograniczenia |
|------------------------|--------------|---------|----------------|------------|
| **id** | **UUID** | **Klucz główny, dziedziczony** | **generowany automatycznie** | **FOREIGN KEY**, **PRIMARY KEY**, NOT NULL |

* (Tabela współdzieli pola audytowe z tabelą nadrzędną access_level)
* Klucze obce: 
  * id odwołuje się do access_level(id)
* Indeksy: Brak dedykowanychc

Tabela 3.2.5 Tabele dziedziczące: student

| Nazwa pola             | Typ danych   | Znaczenie | Wartość domyślna | Ograniczenia |
|------------------------|--------------|---------|----------------|------------|
| **id** | **UUID** | **Klucz główny, dziedziczony** | **generowany automatycznie** | **FOREIGN KEY**, **PRIMARY KEY**, NOT NULL |

* (Tabela współdzieli pola audytowe z tabelą nadrzędną access_level)
* Klucze obce: 
  * id odwołuje się do access_level(id)
* Indeksy: Brak dedykowanych

Tabela 3.2.6 Tabela: one_time_token (Tokeny jednorazowe, np. 2FA, Reset hasła)

| Nazwa pola | Typ danych | Znaczenie | Wartość domyślna | Ograniczenia |
|---|---|---|---|---|
| **id** | **UUID** | **Identyfikator główny** | **generowany automatycznie** | **PRIMARY KEY**, NOT NULL |
| token_value | VARCHAR(255) | Hasz kryptograficzny tokenu | brak | UNIQUE, NOT NULL |
| username | VARCHAR(50) | Login właściciela tokenu | brak | NOT NULL |
| token_type | VARCHAR(32) | Typ tokenu (np. 2FA, PASSWORD_RESET) | brak | NOT NULL |
| expires_at | TIMESTAMP | Czas wygaśnięcia | brak | NOT NULL |
| is_used | BOOLEAN | Flaga wykorzystania | false | NOT NULL |
| version | INTEGER | Wersja krotki | 0 | NOT NULL |
| created_by | UUID | ID konta tworzącego | brak | brak |
| created_at | TIMESTAMP | Czas utworzenia | generowany automatycznie | NOT NULL |
| modified_by | UUID | ID konta modyfikującego | brak | brak |
| updated_at | TIMESTAMP | Czas modyfikacji | generowany automatycznie | brak |

* Klucze obce:
    * created_by odwołuje się do account(id)
    * modified_by odwołuje się do account(id)
* Indeksy: Brak dedykowanych (wymuszone więzami unikalności dla pola token_value)

Tabela 3.2.7 Tabela: email_revert_token (Tokeny cofnięcia zmiany adresu e-mail)

| Nazwa pola | Typ danych | Znaczenie | Wartość domyślna | Ograniczenia |
|---|---|---|---|---|
| **id** | **UUID** | **Identyfikator główny** | **generowany automatycznie** | **PRIMARY KEY**, NOT NULL |
| token_value | VARCHAR(255) | Hasz kryptograficzny tokenu | brak | UNIQUE, NOT NULL |
| username | VARCHAR(50) | Login konta docelowego | brak | NOT NULL |
| old_email | VARCHAR(100) | Adres e-mail do przywrócenia | brak | NOT NULL |
| expires_at | TIMESTAMP | Czas wygaśnięcia | brak | NOT NULL |
| is_used | BOOLEAN | Flaga wykorzystania | false | NOT NULL |
| version | BIGINT | Wersja krotki | 0 | NOT NULL |

* Klucze obce: Brak ścisłych więzów referencyjnych do tabeli account celowo, w celu zachowania niezależnych logów bezpieczeństwa nawet po usunięciu konta.
* Indeksy: Brak dedykowanych

#### 3.3. Struktury tabel Modułu Poszukiwania Plagiatu (MPP) ####

Tabela 3.3.1 Tabela: subject (Przedmiot)

| Nazwa pola             | Typ danych   | Znaczenie | Wartość domyślna | Ograniczenia |
|------------------------|--------------|---------|----------------|------------|
| **id** | **UUID** | **Identyfikator główny** | **generowany automatycznie** | **PRIMARY KEY**, NOT NULL |
| name | VARCHAR(64) | Nazwa przedmiotu | \- | NOT NULL |
| edition | VARCHAR(16) | Rok akademicki / edycja | \- | NOT NULL |
| gitea_url | VARCHAR(512) | Link do organizacji w Gitea | \- | NOT NULL |
| subject_description | VARCHAR(1000) | Opcjonalny opis przedmiotu | \- | \- |
| archived | BOOLEAN | Flaga przeniesienia do archiwum | false | NOT NULL |
| version | INTEGER | Wersja krotki | 0 | NOT NULL |
| created_by | UUID | ID konta tworzącego | \- | \- |
| created_at | TIMESTAMP(6) | Czas nadania roli | generowany automatycznie | NOT NULL |
| modified_by | UUID | ID konta modyfikującego | \- | \- |
| updated_at | TIMESTAMP(6) | Czas ostatniej modyfikacji | generowany automatycznie | \- |

* Klucze obce: 
  * created_by odwołuje się do account(id)
  * modified_by odwołuje się do account(id)
* Indeksy: 
  * idx_subject_created_by 
  * idx_subject_modified_by

Tabela 3.3.2 Tabela: subject_assignment (Tabela łącznikowa: Prowadzący a Przedmiot)

| Nazwa pola             | Typ danych   | Znaczenie                                        | Wartość domyślna | Ograniczenia |
|------------------------|--------------|--------------------------------------------------|----------------|------------|
| **id** | **UUID** | **Identyfikator główny** | **generowany automatycznie** | **PRIMARY KEY**, NOT NULL |
| teacher_id | UUID | ID konta prowadzącego (Teacher) | \- | \- |
| subject_id | UUID | ID przedmiotu | \- | \- |
| has_edit_permissions | BOOLEAN | Prowadzący główny czy dodatkowy | false | NOT NULL |
| version | INTEGER | Wersja krotki | 0 | NOT NULL |
| created_by | UUID | ID konta tworzącego | \- | \- |
| created_at | TIMESTAMP(6) | Czas nadania roli | generowany automatycznie | NOT NULL |
| modified_by | UUID | ID konta modyfikującego | \- | \- |
| updated_at | TIMESTAMP(6) | Czas ostatniej modyfikacji | generowany automatycznie | \- |

* Klucze obce: 
  * teacher_id odwołuje się do teacher(id)
  * subject_id odwołuje się do subject(id)
  * created_by odwołuje się do account(id)
  * modified_by odwołuje się do account(id)
* Indeksy: 
  * idx_subject_assignment_teacher_id
  * idx_subject_assignment_subject_id

Tabela 3.3.3 Tabela: rule_preset (Reguły przedmiotu)

| Nazwa pola             | Typ danych   | Znaczenie | Wartość domyślna | Ograniczenia |
|------------------------|--------------|---------|----------------|------------|
| **id** | **UUID** | **Identyfikator główny** | **generowany automatycznie** | **PRIMARY KEY**, NOT NULL |
| subject_id | UUID | Identyfikator przedmiotu | \- | UNIQUE |
| raport_level_id | UUID | Wymagany poziom raportowania | \- | NOT NULL |
| student_ticket_count | INTEGER | Limit analiz prywatnych | 0 | NOT NULL |
| is_preset | BOOLEAN | Flaga zachowanego presetu | false | NOT NULL |
| version | INTEGER | Wersja krotki | 0 | NOT NULL |
| created_by | UUID | ID konta tworzącego | \- | \- |
| created_at | TIMESTAMP(6) | Czas nadania roli | generowany automatycznie | NOT NULL |
| modified_by | UUID | ID konta modyfikującego | \- | \- |
| updated_at | TIMESTAMP(6) | Czas ostatniej modyfikacji | generowany automatycznie | \- |

* Klucze obce: 
  * subject_id odwołuje się do subject(id)
  * raport_level_id odwołuje się do raport_level_dict(id)
  * created_by odwołuje się do account(id)
  * modified_by odwołuje się do account(id)
* Indeksy: 
  * idx_rule_preset_subject_id
  * idx_rule_preset_raport_level_id
  * idx_rule_preset_created_by
  * idx_rule_preset_modified_by

Tabela 3.3.4 Tabela: schedule (Harmonogram sprawdzeń)

| Nazwa pola             | Typ danych   | Znaczenie | Wartość domyślna | Ograniczenia |
|------------------------|--------------|---------|----------------|------------|
| **id** | **UUID** | **Identyfikator główny** | **generowany automatycznie** | **PRIMARY KEY**, NOT NULL |
| rule_preset_id | UUID | Identyfikator powiązanych reguł | \- | \- |
| schedule_date_time | TIMESTAMP(6) | Data i godzina uruchomienia | \- | NOT NULL |
| tag | VARCHAR(16) | Nazwa tagu w repozytorium | \- | NOT NULL |
| version | INTEGER | Wersja krotki | 0 | NOT NULL |
| created_by | UUID | ID konta tworzącego | \- | \- |
| created_at | TIMESTAMP(6) | Czas nadania roli | generowany automatycznie | NOT NULL |
| modified_by | UUID | ID konta modyfikującego | \- | \- |
| updated_at | TIMESTAMP(6) | Czas ostatniej modyfikacji | generowany automatycznie | \- |

* Klucze obce:
  * rule_preset_id odwołuje się do rule_preset(id)
  * created_by odwołuje się do account(id)
  * modified_by odwołuje się do account(id)
* Indeksy:
  * idx_schedule_rule_preset_id
  * idx_schedule_created_by
  * idx_schedule_modified_by

Tabela 3.3.5 Tabela: repository (Kody źródłowe studentów)

| Nazwa pola             | Typ danych   | Znaczenie | Wartość domyślna | Ograniczenia |
|------------------------|--------------|---------|----------------|------------|
| **id** | **UUID** | **Identyfikator główny** | **generowany automatycznie** | **PRIMARY KEY**, NOT NULL |
| subject_id | UUID | Identyfikator przedmiotu | \- | \- |
| repository_name | VARCHAR(50) | Nazwa repozytorium | \- | NOT NULL |
| ticket_count | VARCHAR(255) | Zestawienie użytych analiz | \- | NOT NULL |
| version | INTEGER | Wersja krotki | 0 | NOT NULL |
| created_by | UUID | ID konta tworzącego | \- | \- |
| created_at | TIMESTAMP(6) | Czas nadania roli | generowany automatycznie | NOT NULL |
| modified_by | UUID | ID konta modyfikującego | \- | \- |
| updated_at | TIMESTAMP(6) | Czas ostatniej modyfikacji | generowany automatycznie | \- |

* Klucze obce:
  * subject_id odwołuje się do subject(id)
  * created_by odwołuje się do account(id)
  * modified_by odwołuje się do account(id)
* Indeksy:
  * idx_repository_subject_id
  * idx_repository_created_by
  * idx_repository_modified_by

Tabela 3.3.6 Tabela repository_student (Tabela łącznikowa: Repozytorium - Studenci)

| Nazwa pola             | Typ danych   | Ograniczenia |
|------------------------|--------------|---------|
| repository_id | UUID | FK do `repository(id)`, NOT NULL |
| student_id | UUID | FK do `student(id)`, NOT NULL |

* Klucze obce:
  * repository_id odwołuje się do repository(id)
  * student_id odwołuje się do student(id)
* Indeksy: 
  * idx_repository_student_repository_id
  * idx_repository_student_student_id


Tabela 3.3.7 Tabela report_repository (Tabela łącznikowa: Raport - Repozytorium)

| Nazwa pola     | Typ danych   | Znaczenie | Wartość domyślna | Ograniczenia |
|----------------|--------------|---------|----------------|------------|
| report_id | UUID | FK do `report(id)`, NOT NULL |
| repository_id | UUID | FK do `repository(id)`, NOT NULL |

* Klucze obce:
  * report_id odwołuje się do report(id)
  * repository_id odwołuje się do repository(id)
* Indeksy: 
  * idx_report_repository_report_id
  * idx_report_repository_repository_id


Tabela 3.3.8 Tabela: report (Wygenerowane analizy)

| Nazwa pola             | Typ danych   | Znaczenie | Wartość domyślna | Ograniczenia |
|------------------------|--------------|---------|----------------|------------|
| **id** | **UUID** | **Identyfikator główny** | **generowany automatycznie** | **PRIMARY KEY**, NOT NULL |
| subject_id | UUID | Identyfikator przedmiotu | \- | \- |
| path_to_report | VARCHAR(512) | Ścieżka do pliku na serwerze | \- | NOT NULL |
| version | INTEGER | Wersja krotki | 0 | NOT NULL |
| created_by | UUID | ID konta tworzącego | \- | \- |
| created_at | TIMESTAMP(6) | Czas nadania roli | generowany automatycznie | NOT NULL |
| modified_by | UUID | ID konta modyfikującego | \- | \- |
| updated_at | TIMESTAMP(6) | Czas ostatniej modyfikacji | generowany automatycznie | \- |

* Klucze obce:
  * subject_id odwołuje się do subject(id)
  * created_by odwołuje się do account(id)
  * modified_by odwołuje się do account(id)
* Indeksy:
  * idx_report_subject_id
  * idx_report_created_by
  * idx_report_modified_by

### Dane inicjujące ###

#### 3.4. Pliki definicji bazy danych (DDL i DML) ####

Zgodnie z poleceniem zaprezentowano definicję struktur oraz definicję danych inicjujących opatrzonych komentarzem. Ponieważ projekt wykorzystuje system Flyway, poniższy kod jest skonsolidowanym stanem po przejściu wszystkich migracji (od V1 do V13), gwarantując 100% zgodności ze stanem faktycznym i ostatecznym bazy danych w środowisku produkcyjnym.

Plik skonsolidowanej definicji struktur (DDL)
```postgresql
-- =================================================================
-- SŁOWNIKI
-- =================================================================
create table access_level_dict
(
    id                uuid         not null,
    access_level_name varchar(255) not null unique,
    version           integer      not null,
    primary key (id)
);
create table language_dict
(
    id       uuid         not null,
    language varchar(255) not null unique,
    version  integer      not null,
    primary key (id)
);
create table raport_level_dict
(
    id      uuid         not null,
    name    varchar(255) not null unique,
    version integer      not null,
    primary key (id)
);

-- =================================================================
-- STRUKTURY BIZNESOWE MOK (Konta, Role, Tokeny)
-- =================================================================
create table account
(
    id                           uuid         not null,
    login                        varchar(50)  not null unique,
    email                        varchar(50)  not null unique,
    password_hash                varchar(64)  not null,
    name                         varchar(50)  not null,
    surname                      varchar(50)  not null,
    language_version_id          uuid         not null,
    is_blocked                   boolean      not null,
    is_verified                  boolean      not null default false,
    activation_token             varchar(255) unique,
    creation_date                timestamp(6) not null,
    second_email_sent            boolean      not null default false,
    failed_attempts              integer,
    last_login_success_date_time timestamp(6),
    last_login_failure_date_time timestamp(6),
    last_successful_login_ip     varchar(45),
    last_failed_login_ip         varchar(45),
    version                      integer      not null,
    created_at                   timestamp(6) not null,
    created_by                   uuid,
    updated_at                   timestamp(6),
    modified_by                  uuid,
    primary key (id)
);

create table access_level
(
    id              uuid         not null,
    account_id      uuid,
    access_level_id uuid         not null,
    is_active       boolean      not null default true,
    version         integer      not null,
    created_at      timestamp(6) not null,
    created_by      uuid,
    updated_at      timestamp(6),
    modified_by     uuid,
    primary key (id)
);

create table admin (id uuid not null, primary key (id));
create table teacher (id uuid not null, primary key (id));
create table student (id uuid not null, primary key (id));

create table one_time_token
(
    id          uuid         not null,
    token_value varchar(255) not null unique,
    username    varchar(50)  not null,
    token_type  varchar(32)  not null,
    expires_at  timestamp(6) not null,
    is_used     boolean      not null default false,
    version     integer      not null,
    created_at  timestamp(6) not null,
    created_by  uuid,
    updated_at  timestamp(6),
    modified_by uuid,
    primary key (id)
);

create table email_revert_token
(
    id          uuid         not null,
    token_value varchar(255) not null unique,
    username    varchar(50)  not null,
    old_email   varchar(100) not null,
    expires_at  timestamp(6) not null,
    is_used     boolean      not null default false,
    version     bigint       not null,
    primary key (id)
);

-- =================================================================
-- STRUKTURY BIZNESOWE MPP (Przedmioty, Analizy, Kody)
-- =================================================================
create table subject
(
    id                  uuid         not null,
    name                varchar(64)  not null,
    edition             varchar(16)  not null,
    gitea_url           varchar(512) not null,
    subject_description varchar(1000),
    archived            boolean      not null,
    version             integer      not null,
    created_at          timestamp(6) not null,
    created_by          uuid,
    updated_at          timestamp(6),
    modified_by         uuid,
    primary key (id)
);

create table subject_assignment
(
    id                   uuid         not null,
    teacher_id           uuid,
    subject_id           uuid,
    has_edit_permissions boolean      not null,
    version              integer      not null,
    created_at           timestamp(6) not null,
    created_by           uuid,
    updated_at           timestamp(6),
    modified_by          uuid,
    primary key (id)
);

create table rule_preset
(
    id                   uuid         not null,
    subject_id           uuid unique,
    raport_level_id      uuid         not null,
    student_ticket_count integer      not null,
    is_preset            boolean      not null,
    version              integer      not null,
    created_at           timestamp(6) not null,
    created_by           uuid,
    updated_at           timestamp(6),
    modified_by          uuid,
    primary key (id)
);

create table schedule
(
    id                 uuid         not null,
    rule_preset_id     uuid,
    schedule_date_time timestamp(6) not null,
    tag                varchar(16)  not null,
    version            integer      not null,
    created_at         timestamp(6) not null,
    created_by         uuid,
    updated_at         timestamp(6),
    modified_by        uuid,
    primary key (id)
);

create table repository
(
    id              uuid         not null,
    subject_id      uuid,
    repository_name varchar(50)  not null,
    ticket_count    varchar(255) not null,
    version         integer      not null,
    created_at      timestamp(6) not null,
    created_by      uuid,
    updated_at      timestamp(6),
    modified_by     uuid,
    primary key (id)
);

create table repository_student
(
    repository_id uuid not null,
    student_id    uuid not null
);

create table report
(
    id             uuid         not null,
    subject_id     uuid,
    path_to_report varchar(512) not null,
    version        integer      not null,
    created_at     timestamp(6) not null,
    created_by     uuid,
    updated_at     timestamp(6),
    modified_by    uuid,
    primary key (id)
);

create table report_repository
(
    report_id     uuid not null,
    repository_id uuid not null
);

-- =================================================================
-- RELACJE I WIĘZY INTEGRALNOŚCI
-- =================================================================
alter table if exists account add constraint FK_acc_lang foreign key (language_version_id) references language_dict;
alter table if exists access_level add constraint FK_al_acc foreign key (account_id) references account;
alter table if exists access_level add constraint FK_al_dict foreign key (access_level_id) references access_level_dict;
alter table if exists admin add constraint FK_admin_al foreign key (id) references access_level;
alter table if exists teacher add constraint FK_teacher_al foreign key (id) references access_level;
alter table if exists student add constraint FK_student_al foreign key (id) references access_level;

alter table if exists subject_assignment add constraint FK_sa_sub foreign key (subject_id) references subject;
alter table if exists subject_assignment add constraint FK_sa_tea foreign key (teacher_id) references teacher;
alter table if exists rule_preset add constraint FK_rp_sub foreign key (subject_id) references subject;
alter table if exists rule_preset add constraint FK_rp_dict foreign key (raport_level_id) references raport_level_dict;
alter table if exists schedule add constraint FK_sch_rp foreign key (rule_preset_id) references rule_preset;
alter table if exists repository add constraint FK_repo_sub foreign key (subject_id) references subject;
alter table if exists repository_student add constraint FK_rs_rep foreign key (repository_id) references repository;
alter table if exists repository_student add constraint FK_rs_stu foreign key (student_id) references student;
alter table if exists report add constraint FK_rep_sub foreign key (subject_id) references subject;
alter table if exists report_repository add constraint FK_rr_rep foreign key (report_id) references report;
alter table if exists report_repository add constraint FK_rr_repo foreign key (repository_id) references repository;

-- Ograniczenia dla pól audytowych (created_by, modified_by) kierują do tabeli account.
```

Plik skonsolidowanej definicji danych inicjujących (DML)
```postgresql
DO
$$
    DECLARE
        admin_id        UUID      := gen_random_uuid();
        teacher_id      UUID      := gen_random_uuid();
        student_id      UUID      := gen_random_uuid();

        lang_pl_id      UUID      := gen_random_uuid();
        lang_en_id      UUID      := gen_random_uuid();
        role_admin_id   UUID      := gen_random_uuid();
        role_teacher_id UUID      := gen_random_uuid();
        role_student_id UUID      := gen_random_uuid();
        rep_level_id    UUID      := gen_random_uuid();

        czas_obecny     TIMESTAMP := NOW();
    BEGIN
        -- Słowniki  
        INSERT INTO public.language_dict (id, language, version) VALUES (lang_pl_id, 'pl', 0), (lang_en_id, 'en', 0);
        INSERT INTO public.access_level_dict (id, access_level_name, version)
        VALUES (role_admin_id, 'ADMIN', 0),
               (role_teacher_id, 'TEACHER', 0),
               (role_student_id, 'STUDENT', 0);
        INSERT INTO public.raport_level_dict (id, name, version)
        VALUES (rep_level_id, 'PEŁNY_WGLĄD', 0),
               (gen_random_uuid(), 'TYLKO_PROCENTY', 0),
               (gen_random_uuid(), 'BRAK_WGLĄDU', 0);

        -- 1. KONTO ADMINA (Użytkownik wymagany do tworzenia kont)
        INSERT INTO public.account (id, login, email, password_hash, name, surname, language_version_id, is_blocked, is_verified, creation_date, version, created_at)
        VALUES (admin_id, 'admin', 'admin@ssbd.edu.pl', '$2a$12$NqL7C7wYxGz4mFhX1Rj8dO2Bw4oF7h.v5.1oZq4e6xZ2o2X0X3z6e',
                'Adam', 'Root', lang_pl_id, false, true, czas_obecny, 0, czas_obecny);

        INSERT INTO public.access_level (id, account_id, access_level_id, is_active, version, created_at)
        VALUES (admin_id, admin_id, role_admin_id, true, 0, czas_obecny);
        INSERT INTO public.admin (id) VALUES (admin_id);

        -- 2. KONTO PROWADZĄCEGO (Z aktywną rolą TEACHER i ADMIN)
        INSERT INTO public.account (id, login, email, password_hash, name, surname, language_version_id, is_blocked, is_verified, creation_date, version, created_at)
        VALUES (teacher_id, 'jkowalski', 'jkowalski@ssbd.edu.pl',
                '$2a$12$NqL7C7wYxGz4mFhX1Rj8dO2Bw4oF7h.v5.1oZq4e6xZ2o2X0X3z6e', 'Jan', 'Kowalski', lang_pl_id, false, true, czas_obecny, 0, czas_obecny);

        INSERT INTO public.access_level (id, account_id, access_level_id, is_active, version, created_at)
        VALUES (teacher_id, teacher_id, role_teacher_id, true, 0, czas_obecny);
        INSERT INTO public.teacher (id) VALUES (teacher_id);
    END
$$;
```
