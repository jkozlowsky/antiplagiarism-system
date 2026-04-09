## 3 Struktury relacyjnej bazy danych 2 pkt

### Struktury tabel ###

Dla każdej tabeli należącej do statycznych struktur bazy danych (włączając w to tabele słownikowe) kompletnie przedstawiono listę pól, kluczy obcych oraz indeksów. Wszystkie tabele zostały utworzone w domyślnym schemacie public silnika PostgreSQL.

W systemie zastosowano strategię dziedziczenia JOINED dla encji poziomów dostępu, co skutkuje wydzieleniem tabel admin, teacher oraz student powiązanych kluczem głównym z tabelą nadrzędną access_level.

Zarządzanie schematem oraz generowaniem stanu początkowego zrealizowano za pomocą narzędzia zarządzania migracjami Flyway.

#### 3.1. Tabele słownikowe ####

Tabela 3.1.1 Tabela: access_level_dict (Słownik dostępnych ról w systemie RBAC)

| Nazwa pola    | Typ danych | Znaczenie | Wartość domyślna | Ograniczenia              |
|---------------|----------|---------|------------------|---------------------------|
| **id** | UUID | Klucz główny krotki | `gen_random_uuid()` | **PRIMARY KEY**, NOT NULL |
| access_level_name | VARCHAR(255) | Nazwa poziomu dostępu | \- | UNIQUE, NOT NULL |
| version | INTEGER | Wersja krotki | 0 | NOT NULL |

* Lokalizacja: public

Tabela 3.1.2 Tabela: language_dict (Słownik wspieranych wersji językowych)

| Nazwa pola                     | Typ danych | Znaczenie | Wartość domyślna | Ograniczenia |
|--------------------------------|----------|---------|------------------|------------|
| **id** | UUID | Klucz główny krotki | `gen_random_uuid()` | **PRIMARY KEY**, NOT NULL |
| language | VARCHAR(255) | Kod języka (np. 'pl', 'en') | \- | UNIQUE, NOT NULL |
| version | INTEGER | Wersja krotki | 0 | NOT NULL |

* Lokalizacja: public

Tabela 3.1.3 Tabela: raport_level_dict (Słownik poziomów raportowania)

| Nazwa pola                     | Typ danych | Znaczenie | Wartość domyślna | Ograniczenia |
|--------------------------------|----------|---------|------------------|------------|
| **id** | UUID | Klucz główny krotki | `gen_random_uuid()` | **PRIMARY KEY**, NOT NULL |
| name | VARCHAR(255) | Poziom wglądu (np. PEŁNY_WGLĄD) | \- | UNIQUE, NOT NULL |
| version | INTEGER | Wersja krotki | 0 | NOT NULL |

* Lokalizacja: public

#### 3.2. Struktury tabel Modułu Obsługi Kont (MOK) ####

Tabela 3.2.1 Tabela: account (Konto użytkownika systemu)

| Nazwa pola             | Typ danych   | Znaczenie | Wartość domyślna | Ograniczenia |
|------------------------|--------------|---------|----------------|------------|
| **id** | UUID | Identyfikator konta | `gen_random_uuid()` | **PRIMARY KEY**, NOT NULL |
| login | VARCHAR(50) | Unikalny login użytkownika | \- | UNIQUE, NOT NULL |
| email | VARCHAR(50) | Unikalny adres poczty | \- | UNIQUE, NOT NULL |
| password_hash | VARCHAR(64) | Hasło w postaci skrótu BCrypt | \- | NOT NULL |
| name | VARCHAR(50) | Imię użytkownika | \- | NOT NULL |
| surname | VARCHAR(50) | Nazwisko użytkownika | \- | NOT NULL |
| language_version_id | UUID | FK do wersji językowej | \- | NOT NULL |
| is_blocked | BOOLEAN | Flaga zablokowania konta | false | NOT NULL |
| failed_attempts | INTEGER | Licznik nieudanych prób logowania | 0 | \- |
| last_login_success_date_time | TIMESTAMP(6) | Czas ostatniego udanego logowania | \- | \- |
| last_login_failure_date_time | TIMESTAMP(6) | Czas ostatniego nieudanego logowania | \- | \- |
| last_successful_login_ip | VARCHAR(45) | IP logowania zakończonego sukcesem | \- | \- |
| last_failed_login_ip | VARCHAR(45) | IP logowania zakończonego porażką | \- | \- |
| version | INTEGER | Wersja krotki (blokada optymistyczna) | 0 | NOT NULL |
| created_by | UUID | ID konta, które utworzyło wpis | \- | \- |
| created_at | TIMESTAMP(6) | Czas utworzenia w bazie | `NOW()` | NOT NULL |
| modified_by | UUID | ID konta, które modyfikowało wpis | \- | \- |
| updated_at | TIMESTAMP(6) | Czas ostatniej modyfikacji | \- | \- |

* Klucze obce: language_version_id -> language_dict(id), created_by -> account(id), modified_by -> account(id)
* Indeksy: idx_account_language_version_id, idx_account_created_by, idx_account_modified_by
* Lokalizacja: public

Tabela 3.2.2 Tabela: access_level (Nadrzędna tabela poziomów dostępu)

| Nazwa pola             | Typ danych   | Znaczenie | Wartość domyślna | Ograniczenia |
|------------------------|--------------|---------|----------------|------------|
| **id** | UUID | Identyfikator przypisania roli | `gen_random_uuid()` | **PRIMARY KEY**, NOT NULL |
| account_id | UUID | Identyfikator konta użytkownika | \- | \- |
| access_level_id | UUID | FK do słownika ról | \- | NOT NULL |
| version | INTEGER | Wersja krotki | 0 | NOT NULL |
| created_by | UUID | ID konta nadającego rolę | \- | \- |
| created_at | TIMESTAMP(6) | Czas nadania roli | `NOW()` | NOT NULL |
| modified_by | UUID | ID konta modyfikującego status | \- | \- |
| updated_at | TIMESTAMP(6) | Czas modyfikacji statusu roli | \- | \- |

* Klucze obce: account_id -> account(id), access_level_id -> access_level_dict(id), created_by -> account(id), modified_by -> account(id)
* Indeksy: idx_access_level_dict_id, idx_access_level_account_id, idx_access_level_created_by, idx_access_level_modified_by
* Lokalizacja: public

Tabela 3.2.3 Tabele dziedziczące: admin

| Nazwa pola             | Typ danych   | Znaczenie | Wartość domyślna | Ograniczenia |
|------------------------|--------------|---------|----------------|------------|
| **id** | UUID | Klucz główny, FK do `access_level` | \- | **PRIMARY KEY**, NOT NULL |

* (Tabela współdzieli pola audytowe z tabelą nadrzędną access_level)
* Klucze obce: id -> access_level(id)
* Lokalizacja: public

Tabela 3.2.4 Tabele dziedziczące: teacher

| Nazwa pola             | Typ danych   | Znaczenie | Wartość domyślna | Ograniczenia |
|------------------------|--------------|---------|----------------|------------|
| **id** | UUID | Klucz główny, FK do `access_level` | \- | **PRIMARY KEY**, NOT NULL |

* (Tabela współdzieli pola audytowe z tabelą nadrzędną access_level)
* Klucze obce: id -> access_level(id)
* Lokalizacja: public

Tabela 3.2.5 Tabele dziedziczące: student

| Nazwa pola             | Typ danych   | Znaczenie | Wartość domyślna | Ograniczenia |
|------------------------|--------------|---------|----------------|------------|
| **id** | UUID | Klucz główny, FK do `access_level` | \- | **PRIMARY KEY**, NOT NULL |

* (Tabela współdzieli pola audytowe z tabelą nadrzędną access_level)
* Klucze obce: id -> access_level(id)
* Lokalizacja: public

#### 3.3. Struktury tabel Modułu Poszukiwania Plagiatu (MPP) ####

Tabela 3.3.1 Tabela: subject (Przedmiot)

| Nazwa pola             | Typ danych   | Znaczenie | Wartość domyślna | Ograniczenia |
|------------------------|--------------|---------|----------------|------------|
| **id** | UUID | Identyfikator przedmiotu | `gen_random_uuid()` | **PRIMARY KEY**, NOT NULL |
| name | VARCHAR(64) | Nazwa przedmiotu | \- | NOT NULL |
| edition | VARCHAR(16) | Rok akademicki / edycja | \- | NOT NULL |
| gitea_url | VARCHAR(512) | Link do organizacji w Gitea | \- | NOT NULL |
| subject_description | VARCHAR(1000) | Opcjonalny opis przedmiotu | \- | \- |
| archived | BOOLEAN | Flaga przeniesienia do archiwum | false | NOT NULL |
| version | INTEGER | Wersja krotki | 0 | NOT NULL |
| created_by | UUID | ID konta tworzącego | \- | \- |
| created_at | TIMESTAMP(6) | Czas utworzenia | `NOW()` | NOT NULL |
| modified_by | UUID | ID konta modyfikującego | \- | \- |
| updated_at | TIMESTAMP(6) | Czas modyfikacji | \- | \- |

* Klucze obce: created_by -> account(id), modified_by -> account(id)
* Indeksy: idx_subject_created_by, idx_subject_modified_by
* Lokalizacja: public

Tabela 3.3.2 Tabela: subject_assignment (Tabela łącznikowa: Prowadzący a Przedmiot)

| Nazwa pola             | Typ danych   | Znaczenie                                        | Wartość domyślna | Ograniczenia |
|------------------------|--------------|--------------------------------------------------|----------------|------------|
| **id** | UUID | Identyfikator przypisania | `gen_random_uuid()` | **PRIMARY KEY**, NOT NULL |
| teacher_id | UUID | ID konta prowadzącego (Teacher) | \- | \- |
| subject_id | UUID | ID przedmiotu | \- | \- |
| has_edit_permissions | BOOLEAN | Prowadzący główny czy dodatkowy | false | NOT NULL |
| version | INTEGER | Wersja krotki | 0 | NOT NULL |
| created_by | UUID | ID konta tworzącego | \- | \- |
| created_at | TIMESTAMP(6) | Czas utworzenia | `NOW()` | NOT NULL |
| modified_by | UUID | ID konta modyfikującego | \- | \- |
| updated_at | TIMESTAMP(6) | Czas modyfikacji | \- | \- |

* Klucze obce: teacher_id -> teacher(id), subject_id -> subject(id), created_by -> account(id), modified_by -> account(id)
* Indeksy: idx_subject_assignment_teacher_id, idx_subject_assignment_subject_id, idx_subject_assignment_created_by, idx_subject_assignment_modified_by
* Lokalizacja: public

Tabela 3.3.3 Tabela: rule_preset (Reguły przedmiotu)

| Nazwa pola             | Typ danych   | Znaczenie | Wartość domyślna | Ograniczenia |
|------------------------|--------------|---------|----------------|------------|
| **id** | UUID | Identyfikator reguły | `gen_random_uuid()` | **PRIMARY KEY**, NOT NULL |
| subject_id | UUID | Identyfikator przedmiotu | \- | UNIQUE |
| raport_level_id | UUID | Wymagany poziom raportowania | \- | NOT NULL |
| student_ticket_count | INTEGER | Limit analiz prywatnych | 0 | NOT NULL |
| is_preset | BOOLEAN | Flaga zachowanego presetu | false | NOT NULL |
| version | INTEGER | Wersja krotki | 0 | NOT NULL |
| created_by | UUID | ID konta tworzącego | \- | \- |
| created_at | TIMESTAMP(6) | Czas utworzenia | `NOW()` | NOT NULL |
| modified_by | UUID | ID konta modyfikującego | \- | \- |
| updated_at | TIMESTAMP(6) | Czas modyfikacji | \- | \- |

* Klucze obce: subject_id -> subject(id), raport_level_id -> raport_level_dict(id), created_by -> account(id), modified_by -> account(id)
* Indeksy: idx_rule_preset_subject_id, idx_rule_preset_raport_level_id, idx_rule_preset_created_by, idx_rule_preset_modified_by
* Lokalizacja: public

Tabela 3.3.4 Tabela: schedule (Harmonogram sprawdzeń)

| Nazwa pola             | Typ danych   | Znaczenie | Wartość domyślna | Ograniczenia |
|------------------------|--------------|---------|----------------|------------|
| **id** | UUID | Identyfikator harmonogramu | `gen_random_uuid()` | **PRIMARY KEY**, NOT NULL |
| rule_preset_id | UUID | Identyfikator powiązanych reguł | \- | \- |
| schedule_date_time | TIMESTAMP(6) | Data i godzina uruchomienia | \- | NOT NULL |
| tag | VARCHAR(16) | Nazwa tagu w repozytorium | \- | NOT NULL |
| version | INTEGER | Wersja krotki | 0 | NOT NULL |
| created_by | UUID | ID konta tworzącego | \- | \- |
| created_at | TIMESTAMP(6) | Czas utworzenia | `NOW()` | NOT NULL |
| modified_by | UUID | ID konta modyfikującego | \- | \- |
| updated_at | TIMESTAMP(6) | Czas modyfikacji | \- | \- |

* Klucze obce: rule_preset_id -> rule_preset(id), created_by -> account(id), modified_by -> account(id)
* Indeksy: idx_schedule_rule_preset_id, idx_schedule_created_by, idx_schedule_modified_by
* Lokalizacja: public

Tabela 3.3.5 Tabela: repository (Kody źródłowe studentów)

| Nazwa pola             | Typ danych   | Znaczenie | Wartość domyślna | Ograniczenia |
|------------------------|--------------|---------|----------------|------------|
| **id** | UUID | Identyfikator repozytorium | `gen_random_uuid()` | **PRIMARY KEY**, NOT NULL |
| subject_id | UUID | Identyfikator przedmiotu | \- | \- |
| repository_name | VARCHAR(50) | Nazwa repozytorium | \- | NOT NULL |
| ticket_count | VARCHAR(255) | Zestawienie użytych analiz | \- | NOT NULL |
| version | INTEGER | Wersja krotki | 0 | NOT NULL |
| created_by | UUID | ID konta tworzącego | \- | \- |
| created_at | TIMESTAMP(6) | Czas dodania do bazy | `NOW()` | NOT NULL |
| modified_by | UUID | ID konta modyfikującego | \- | \- |
| updated_at | TIMESTAMP(6) | Czas ostatniej modyfikacji | \- | \- |

* Klucze obce: subject_id -> subject(id), created_by -> account(id), modified_by -> account(id)
* Indeksy: idx_repository_subject_id, idx_repository_created_by, idx_repository_modified_by
* Lokalizacja: public

Tabela 3.3.6 Tabela repository_student (Tabela łącznikowa: Repozytorium - Studenci)

| Nazwa pola             | Typ danych   | Ograniczenia |
|------------------------|--------------|---------|
| repository_id | UUID | FK do `repository(id)`, NOT NULL |
| student_id | UUID | FK do `student(id)`, NOT NULL |

* Indeksy: idx_repository_student_repository_id, idx_repository_student_student_id
* Lokalizacja: public

Tabela 3.3.7 Tabela report_repository (Tabela łącznikowa: Raport - Repozytorium)

| Nazwa pola     | Typ danych   | Znaczenie | Wartość domyślna | Ograniczenia |
|----------------|--------------|---------|----------------|------------|
| report_id | UUID | FK do `report(id)`, NOT NULL |
| repository_id | UUID | FK do `repository(id)`, NOT NULL |

* Indeksy: idx_report_repository_report_id, idx_report_repository_repository_id
* Lokalizacja: public

Tabela 3.3.8 Tabela: report (Wygenerowane analizy)

| Nazwa pola             | Typ danych   | Znaczenie | Wartość domyślna | Ograniczenia |
|------------------------|--------------|---------|----------------|------------|
| **id** | UUID | Identyfikator raportu | `gen_random_uuid()` | **PRIMARY KEY**, NOT NULL |
| subject_id | UUID | Identyfikator przedmiotu | \- | \- |
| path_to_report | VARCHAR(512) | Ścieżka do pliku na serwerze | \- | NOT NULL |
| version | INTEGER | Wersja krotki | 0 | NOT NULL |
| created_by | UUID | ID konta tworzącego | \- | \- |
| created_at | TIMESTAMP(6) | Czas wygenerowania | `NOW()` | NOT NULL |
| modified_by | UUID | ID konta modyfikującego | \- | \- |
| updated_at | TIMESTAMP(6) | Czas ostatniej modyfikacji | \- | \- |

* Klucze obce: subject_id -> subject(id), created_by -> account(id), modified_by -> account(id)
* Indeksy: idx_report_subject_id, idx_report_created_by, idx_report_modified_by
* Lokalizacja: public

### Dane inicjujące ###

#### 3.4. Pliki definicji bazy danych (DDL i DML) ####

Zgodnie z poleceniem makiety przedstawiono plik definicji struktur (DDL) oraz plik definicji danych inicjujących (DML) opatrzonych komentarzami dla zastosowanego narzędzia zarządzania migracjami (Flyway).

Plik migracji: V1__create-schema.sql (DDL - Struktury)
```postgresql
-- =================================================================
-- SŁOWNIKI
-- =================================================================
create table access_level_dict  
(  
    version           integer      not null,  
    id                uuid         not null,  
    access_level_name varchar(255) not null unique,  
    primary key (id)  
);
create table language_dict  
(  
    version  integer      not null,  
    id       uuid         not null,  
    language varchar(255) not null unique,  
    primary key (id)  
);  
create table raport_level_dict  
(  
    version integer      not null,  
    id      uuid         not null,  
    name    varchar(255) not null unique,  
    primary key (id)  
);

-- =================================================================
-- STRUKTURY BIZNESOWE (MOK i MPP)
-- =================================================================
create table access_level  
(  
    version         integer      not null,  
    created_at      timestamp(6) not null,  
    updated_at      timestamp(6),  
    access_level_id uuid         not null,  
    account_id      uuid,  
    created_by      uuid,  
    id              uuid         not null,  
    modified_by     uuid,  
    primary key (id)  
);
create table account  
(  
    failed_attempts              integer,  
    is_blocked                   boolean      not null,  
    version                      integer      not null,  
    created_at                   timestamp(6) not null,  
    last_login_failure_date_time timestamp(6),  
    last_login_success_date_time timestamp(6),  
    updated_at                   timestamp(6),  
    created_by                   uuid,  
    id                           uuid         not null,  
    language_version_id          uuid         not null,  
    modified_by                  uuid,  
    last_failed_login_ip         varchar(45),  
    last_successful_login_ip     varchar(45),  
    email                        varchar(50)  not null unique,  
    login                        varchar(50)  not null unique,  
    name                         varchar(50)  not null,  
    surname                      varchar(50)  not null,  
    password_hash                varchar(64)  not null,  
    primary key (id)  
);
create table admin  
(  
    id uuid not null,  
    primary key (id)  
);
create table report  
(  
    version        integer      not null,  
    created_at     timestamp(6) not null,  
    updated_at     timestamp(6),  
    created_by     uuid,  
    id             uuid         not null,  
    modified_by    uuid,  
    subject_id     uuid,  
    path_to_report varchar(512) not null,  
    primary key (id)  
);  
create table report_repository  
(  
    report_id     uuid not null,  
    repository_id uuid not null  
);  
create table repository  
(  
    version         integer      not null,  
    created_at      timestamp(6) not null,  
    updated_at      timestamp(6),  
    created_by      uuid,  
    id              uuid         not null,  
    modified_by     uuid,  
    subject_id      uuid,  
    repository_name varchar(50)  not null,  
    ticket_count    varchar(255) not null,  
    primary key (id)  
);  
create table repository_student  
(  
    repository_id uuid not null,  
    student_id    uuid not null  
);  
create table rule_preset  
(  
    is_preset            boolean      not null,  
    student_ticket_count integer      not null,  
    version              integer      not null,  
    created_at           timestamp(6) not null,  
    updated_at           timestamp(6),  
    created_by           uuid,  
    id                   uuid         not null,  
    modified_by          uuid,  
    raport_level_id      uuid         not null,  
    subject_id           uuid unique,  
    primary key (id)  
);  
create table schedule  
(  
    version            integer      not null,  
    created_at         timestamp(6) not null,  
    schedule_date_time timestamp(6) not null,  
    updated_at         timestamp(6),  
    created_by         uuid,  
    id                 uuid         not null,  
    modified_by        uuid,  
    rule_preset_id     uuid,  
    tag                varchar(16)  not null,  
    primary key (id)  
);  
create table student  
(  
    id uuid not null,  
    primary key (id)  
);  
create table Subject  
(  
    archived            boolean      not null,  
    version             integer      not null,  
    created_at          timestamp(6) not null,  
    updated_at          timestamp(6),  
    created_by          uuid,  
    edition             varchar(16)  not null,  
    id                  uuid         not null,  
    modified_by         uuid,  
    name                varchar(64)  not null,  
    gitea_url           varchar(512) not null,  
    subject_description varchar(1000),  
    primary key (id)  
);  
create table subject_assignment  
(  
    has_edit_permissions boolean      not null,  
    version              integer      not null,  
    created_at           timestamp(6) not null,  
    updated_at           timestamp(6),  
    created_by           uuid,  
    id                   uuid         not null,  
    modified_by          uuid,  
    subject_id           uuid,  
    teacher_id           uuid,  
    primary key (id)  
);  
create table teacher  
(  
    id uuid not null,  
    primary key (id)  
);
-- =================================================================
-- INDEKSY DLA KLUCZY OBCYCH
-- =================================================================
create index idx_access_level_dict_id on access_level (access_level_id);
create index idx_access_level_account_id on access_level (account_id);
create index idx_access_level_created_by on access_level (created_by);
create index idx_access_level_modified_by on access_level (modified_by);
create index idx_account_language_version_id on account (language_version_id);
create index idx_account_created_by on account (created_by);
create index idx_account_modified_by on account (modified_by);
create index idx_report_subject_id on report (subject_id);
create index idx_report_created_by on report (created_by);
create index idx_report_modified_by on report (modified_by);
create index idx_report_repository_report_id on report_repository (report_id);
create index idx_report_repository_repository_id on report_repository (repository_id);
create index idx_repository_subject_id on repository (subject_id);
create index idx_repository_created_by on repository (created_by);
create index idx_repository_modified_by on repository (modified_by);
create index idx_repository_student_repository_id on repository_student (repository_id);
create index idx_repository_student_student_id on repository_student (student_id);
create index idx_rule_preset_raport_level_id on rule_preset (raport_level_id);
create index idx_rule_preset_subject_id on rule_preset (subject_id);
create index idx_rule_preset_created_by on rule_preset (created_by);
create index idx_rule_preset_modified_by on rule_preset (modified_by);
create index idx_schedule_rule_preset_id on schedule (rule_preset_id);
create index idx_schedule_created_by on schedule (created_by);
create index idx_schedule_modified_by on schedule (modified_by);
create index idx_subject_created_by on subject (created_by);
create index idx_subject_modified_by on subject (modified_by);
create index idx_subject_assignment_teacher_id on subject_assignment (teacher_id);
create index idx_subject_assignment_subject_id on subject_assignment (subject_id);
create index idx_subject_assignment_created_by on subject_assignment (created_by);
create index idx_subject_assignment_modified_by on subject_assignment (modified_by);

-- =================================================================
-- RELACJE I WIĘZY INTEGRALNOŚCI
-- =================================================================
alter table if exists access_level  
    add constraint FKof8x67v6rlo4uqhjr3k1csaoo foreign key (created_by) references account;  
alter table if exists access_level  
    add constraint FK73174ufy7xjp6vtev9tr6igsr foreign key (modified_by) references account;  
alter table if exists access_level  
    add constraint FKfwnr16liua1xlmegeroxjpjgm foreign key (access_level_id) references access_level_dict;  
alter table if exists access_level  
    add constraint FKl6ljrvl5w8nhxvrrqt0pu4uou foreign key (account_id) references account;  
alter table if exists account  
    add constraint FKatwoepbie7ousm3au0cp7hira foreign key (created_by) references account;  
alter table if exists account  
    add constraint FKamhcror3cllccnl64tog77g63 foreign key (modified_by) references account;  
alter table if exists account  
    add constraint FK2v4wsury86sg66iwigxcnemur foreign key (language_version_id) references language_dict;  
alter table if exists admin  
    add constraint FKoattn78xgq9lfyf4aqta45t2g foreign key (id) references access_level;  
alter table if exists report  
    add constraint FKtabrqbv4a2qpd7svtm4j2fmbi foreign key (created_by) references account;  
alter table if exists report  
    add constraint FKd493pslr7i7s38afe9smeuspj foreign key (modified_by) references account;  
alter table if exists report  
    add constraint FKpoktbhs862mi55mmkyrhghicn foreign key (subject_id) references Subject;  
alter table if exists report_repository  
    add constraint FKn3nnvnmu3pjohdx4ertor9l14 foreign key (repository_id) references repository;  
alter table if exists report_repository  
    add constraint FKjoyicyakroqqnq643wj4x9172 foreign key (report_id) references report;  
alter table if exists repository  
    add constraint FKle9b5p7a6321xq7ubhcl9ko4i foreign key (created_by) references account;  
alter table if exists repository  
    add constraint FKtql9cw6i0walo49vsi5djsep6 foreign key (modified_by) references account;  
alter table if exists repository  
    add constraint FKawknd2f17lcrfsgxd7k2yghmb foreign key (subject_id) references Subject;  
alter table if exists repository_student  
    add constraint FKn2e3iler0nx4t2ceea55umflt foreign key (student_id) references student;  
alter table if exists repository_student  
    add constraint FK85w0wmqpemm6gh9thi4ox3vi2 foreign key (repository_id) references repository;  
alter table if exists rule_preset  
    add constraint FKofpcwoi34pt9d85jjp6th61o8 foreign key (created_by) references account;  
alter table if exists rule_preset  
    add constraint FK4s3ud9n5jhfpt8xdy642swoum foreign key (modified_by) references account;  
alter table if exists rule_preset  
    add constraint FK3qdb95ypmn1s55wlcr8slycwl foreign key (raport_level_id) references raport_level_dict;  
alter table if exists rule_preset  
    add constraint FK4neg3d5mhugn257y5l4m0omps foreign key (subject_id) references Subject;  
alter table if exists schedule  
    add constraint FKjb826iva7idpl3lnkuj6jbab1 foreign key (created_by) references account;  
alter table if exists schedule  
    add constraint FKop4s18vy50g7nrwo6y8kq2nge foreign key (modified_by) references account;  
alter table if exists schedule  
    add constraint FKk7b7145vnsrqm7qp8bsrgvv5c foreign key (rule_preset_id) references rule_preset;  
alter table if exists student  
    add constraint FK4tmav461awf0040phhbp9b33 foreign key (id) references access_level;  
alter table if exists Subject  
    add constraint FK81v3e3ofl29ivy47fu87wf2vx foreign key (created_by) references account;  
alter table if exists Subject  
    add constraint FK4ppt1q7rbioqruy16uu1fvtpm foreign key (modified_by) references account;  
alter table if exists subject_assignment  
    add constraint FK5c3a1htdcdtctpvbrg0gum1b8 foreign key (created_by) references account;  
alter table if exists subject_assignment  
    add constraint FKl4e5w9fsbanhdt55f5chrhshh foreign key (modified_by) references account;  
alter table if exists subject_assignment  
    add constraint FKoo00eu0c4uy1fbfuslx7htbxd foreign key (subject_id) references Subject;  
alter table if exists subject_assignment  
    add constraint FKjvousvwbu0p39th35dryscg44 foreign key (teacher_id) references teacher;  
alter table if exists teacher  
    add constraint FKgnb2jmlqrk5860w55vclmx1eo foreign key (id) references access_level;
```

Plik migracji: V3__insert-values.sql (DML - Dane inicjujące)
```postgresql
DO  
$$  
    DECLARE  
        admin_id        UUID      := gen_random_uuid();  
        teacher_id      UUID      := gen_random_uuid();  
        student_id      UUID      := gen_random_uuid();  
  
        -- Słowniki  
        lang_pl_id      UUID      := gen_random_uuid();  
        lang_en_id      UUID      := gen_random_uuid();  
        role_admin_id   UUID      := gen_random_uuid();  
        role_teacher_id UUID      := gen_random_uuid();  
        role_student_id UUID      := gen_random_uuid();  
        rep_level_id    UUID      := gen_random_uuid();  
  
        -- Elementy MPP  
        sub_id          UUID      := gen_random_uuid();  
        al_admin_id     UUID      := gen_random_uuid();  
        al_teacher_id   UUID      := gen_random_uuid();  
        al_student_id   UUID      := gen_random_uuid();  
        czas_obecny     TIMESTAMP := NOW();  
    BEGIN  
        -- SŁOWNIKI (Dostosowane do braku pól audytowych)  
        INSERT INTO public.language_dict (id, language, version) VALUES (lang_pl_id, 'pl', 0), (lang_en_id, 'en', 0);  
        INSERT INTO public.access_level_dict (id, access_level_name, version)  
        VALUES (role_admin_id, 'ADMIN', 0),  
               (role_teacher_id, 'TEACHER', 0),  
               (role_student_id, 'STUDENT', 0);  
        INSERT INTO public.raport_level_dict (id, name, version)  
        VALUES (rep_level_id, 'PEŁNY_WGLĄD', 0),  
               (gen_random_uuid(), 'TYLKO_PROCENTY', 0),  
               (gen_random_uuid(), 'BRAK_WGLĄDU', 0);  
  
        -- 1. KONTO ADMINA  
        INSERT INTO public.account (id, login, email, password_hash, name, surname, language_version_id, is_blocked,  
                                    version, created_by, created_at)  
        VALUES (admin_id, 'admin', 'admin@ssbd.edu.pl', '$2a$12$NqL7C7wYxGz4mFhX1Rj8dO2Bw4oF7h.v5.1oZq4e6xZ2o2X0X3z6e',  
                'Adam', 'Root', lang_pl_id, false, 0, admin_id, czas_obecny);  
  
        INSERT INTO public.access_level (id, account_id, access_level_id, version, created_by, created_at)  
        VALUES (al_admin_id, admin_id, role_admin_id, 0, admin_id, czas_obecny);  
        INSERT INTO public.admin (id) VALUES (al_admin_id);  
  
        -- 2. KONTO PROWADZĄCEGO / ADMINA 
        INSERT INTO public.account (id, login, email, password_hash, name, surname, language_version_id, is_blocked,  
                                    version, created_by, created_at)  
        VALUES (teacher_id, 'jkowalski', 'jkowalski@ssbd.edu.pl',  
                '$2a$12$NqL7C7wYxGz4mFhX1Rj8dO2Bw4oF7h.v5.1oZq4e6xZ2o2X0X3z6e', 'Jan', 'Kowalski', lang_pl_id, false, 0,  
                admin_id, czas_obecny);  
  
        INSERT INTO public.access_level (id, account_id, access_level_id, version, created_by, created_at)  
        VALUES (al_teacher_id, teacher_id, role_teacher_id, 0, admin_id, czas_obecny);  
        INSERT INTO public.teacher (id) VALUES (al_teacher_id);  
  
        INSERT INTO public.access_level (id, account_id, access_level_id, version, created_by, created_at)  
        VALUES (al_teacher_id, teacher_id, role_admin_id, 0, admin_id, czas_obecny);  
        INSERT INTO public.teacher (id) VALUES (al_teacher_id);  
  
        -- 3. KONTO STUDENTA  
        INSERT INTO public.account (id, login, email, password_hash, name, surname, language_version_id, is_blocked,  
                                    version, created_by, created_at)  
        VALUES (student_id, 'anowak', 'anowak@ssbd.edu.pl',  
                '$2a$12$NqL7C7wYxGz4mFhX1Rj8dO2Bw4oF7h.v5.1oZq4e6xZ2o2X0X3z6e', 'Anna', 'Nowak', lang_pl_id, false, 0,  
                admin_id, czas_obecny);  
  
        INSERT INTO public.access_level (id, account_id, access_level_id, version, created_by, created_at)  
        VALUES (al_student_id, student_id, role_student_id, 0, admin_id, czas_obecny);  
        INSERT INTO public.student (id) VALUES (al_student_id);  
  
        -- 4. INICJALIZACJA MPP  
        INSERT INTO public.Subject (id, name, edition, gitea_url, archived, version, created_by, created_at)  
        VALUES (sub_id, 'Algorytmy', '2026', 'https://gitea.it.p.lodz.pl/asd_2026', false, 0, teacher_id, czas_obecny);  
  
        INSERT INTO public.subject_assignment (id, teacher_id, subject_id, has_edit_permissions, version, created_by,  
                                               created_at)  
        VALUES (gen_random_uuid(), al_teacher_id, sub_id, true, 0, teacher_id, czas_obecny);  
  
        INSERT INTO public.rule_preset (id, subject_id, raport_level_id, student_ticket_count, is_preset, version,  
                                        created_by, created_at)  
        VALUES (gen_random_uuid(), sub_id, rep_level_id, 3, true, 0, teacher_id, czas_obecny);  
    END  
$$;
```
