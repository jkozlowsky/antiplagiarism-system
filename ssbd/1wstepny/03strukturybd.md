## 3 Struktury relacyjnej bazy danych 2 pkt

### Struktury tabel ###

#### 3.1. Tabele słownikowe ####

Tabela 3.1.1 Tabela: dict_role (Słownik dostępnych ról w systemie RBAC)

| Nazwa pola    | Typ danych | Znaczenie | Wartość domyślna | Ograniczenia              |
|---------------|----------|---------|------------------|---------------------------|
| **role_name** |   VARCHAR(32)       |  Klucz główny, nazwa poziomu dostępu       | -                | **PRIMARY KEY**, NOT NULL |
| description   |     VARCHAR(100)     |   Opis biznesowy danej roli      | -                |      NOT NULL                     |

* Lokalizacja: public

Tabela 3.1.2 Tabela: dict_language (Słownik wspieranych wersji językowych)

| Nazwa pola                     | Typ danych | Znaczenie | Wartość domyślna | Ograniczenia |
|--------------------------------|----------|---------|------------------|------------|
|       **language_code**               |     VARCHAR(2)     |      Klucz główny, kod języka (np. 'pl', 'en')   | -                |     **PRIMARY KEY**, NOT NULL       |

* Lokalizacja: public

Tabela 3.1.3 Tabela: dict_report_level (Słownik dostępnych poziomów raportowania dla studentów)

| Nazwa pola                     | Typ danych | Znaczenie | Wartość domyślna | Ograniczenia |
|--------------------------------|----------|---------|------------------|------------|
|       **level_name**               |    VARCHAR(32)      |    Klucz główny (np. PEŁNY_WGLĄD)     | -                |    **PRIMARY KEY**, NOT NULL        |

* Lokalizacja: public

#### 3.2. Struktury tabel Modułu Obsługi Kont (MOK) ####

Tabela 3.2.1 Tabela: account (Konto użytkownika systemu)

| Nazwa pola             | Typ danych   | Znaczenie | Wartość domyślna | Ograniczenia |
|------------------------|--------------|---------|----------------|------------|
| **id** | UUID | Identyfikator konta | `gen_random_uuid()` | **PRIMARY KEY**, NOT NULL |
| login | VARCHAR(50) | Unikalny login (nie zawiera danych biznesowych) | \- | UNIQUE, NOT NULL |
| email | VARCHAR(100) | Unikalny adres poczty elektronicznej | \- | UNIQUE, NOT NULL |
| password_hash | CHAR(60) | Hasło w postaci skrótu BCrypt | \- | NOT NULL |
| name | VARCHAR(50) | Imię użytkownika | \- | NOT NULL |
| surname | VARCHAR(50) | Nazwisko użytkownika | \- | NOT NULL |
| language_code | VARCHAR(2) | Preferowana wersja językowa | 'pl' | NOT NULL |
| active | BOOLEAN | Flaga weryfikacji adresu e-mail | false | NOT NULL |
| blocked | BOOLEAN | Flaga zablokowania konta | false | NOT NULL |
| failed_attempts | INTEGER | Licznik nieudanych prób logowania | 0 | NOT NULL, >= 0 |
| last_successful_login | TIMESTAMP | Data i czas ostatniego udanego logowania | \- | \- |
| last_failed_login | TIMESTAMP | Data i czas ostatniego nieudanego logowania | \- | \- |
| last_successful_ip | VARCHAR(45) | Adres IP logowania zakończonego sukcesem | \- | \- |
| last_failed_ip | VARCHAR(45) | Adres IP logowania zakończonego porażką | \- | \- |
| version | BIGINT | Wersja krotki (blokada optymistyczna) | 0 | NOT NULL, >= 0 |
| created_by | UUID | ID konta, które utworzyło wpis | \- | \- |
| creation_timestamp | TIMESTAMP | Czas utworzenia w bazie | `NOW()` | NOT NULL |
| modified_by | UUID | ID konta, które modyfikowało wpis | \- | \- |
| modification_timestamp | TIMESTAMP | Czas ostatniej modyfikacji | \- | \- |

* Klucze obce: language_code -> dict_language(language_code), created_by -> account(id), modified_by -> account(id)
* Indeksy: idx_account_login, idx_account_email, idx_account_lang, idx_account_created, idx_account_modified
* Lokalizacja: public

Tabela 3.2.2 Tabela: access_level (Poziomy dostępu przypisane do konta)

| Nazwa pola             | Typ danych   | Znaczenie | Wartość domyślna | Ograniczenia |
|------------------------|--------------|---------|----------------|------------|
| **id** | UUID | Identyfikator przypisania roli | `gen_random_uuid()` | **PRIMARY KEY**, NOT NULL |
| account_id | UUID | Identyfikator konta użytkownika | \- | NOT NULL |
| role_name | VARCHAR(32) | Typ nadanej roli | \- | NOT NULL |
| active | BOOLEAN | Flaga aktywności danej roli | true | NOT NULL |
| version | BIGINT | Wersja krotki | 0 | NOT NULL |
| created_by | UUID | ID administratora nadającego rolę | \- | NOT NULL |
| creation_timestamp | TIMESTAMP | Czas nadania roli | `NOW()` | NOT NULL |
| modified_by | UUID | ID administratora modyfikującego status | \- | \- |
| modification_timestamp | TIMESTAMP | Czas modyfikacji statusu roli | \- | \- |

* Klucze obce: account_id -> account(id), role_name -> dict_role(role_name), created_by -> account(id), modified_by -> account(id)
* Ograniczenia: UNIQUE(account_id, role_name)
* Indeksy: idx_access_level_account, idx_access_level_role, idx_access_level_created, idx_access_level_modified
* Lokalizacja: public

#### 3.3. Struktury tabel Modułu Poszukiwania Plagiatu (MPP) ####

Tabela 3.3.1 Tabela: subject (Przedmiot)

| Nazwa pola             | Typ danych   | Znaczenie | Wartość domyślna | Ograniczenia |
|------------------------|--------------|---------|----------------|------------|
| **id** | UUID | Identyfikator przedmiotu | `gen_random_uuid()` | **PRIMARY KEY**, NOT NULL |
| name | VARCHAR(100) | Nazwa przedmiotu | \- | NOT NULL |
| year | INTEGER | Rok akademicki / edycja | \- | NOT NULL, > 2000 |
| gitea_url | VARCHAR(255) | Link do organizacji przedmiotu w Gitea | \- | NOT NULL |
| archived | BOOLEAN | Flaga przeniesienia do archiwum | false | NOT NULL |
| version | BIGINT | Wersja krotki (blokada optymistyczna) | 0 | NOT NULL |
| created_by | UUID | ID konta tworzącego | \- | NOT NULL |
| creation_timestamp | TIMESTAMP | Czas utworzenia | `NOW()` | NOT NULL |
| modified_by | UUID | ID konta modyfikującego | \- | \- |
| modification_timestamp | TIMESTAMP | Czas modyfikacji | \- | \- |

* Klucze obce: created_by -> account(id), modified_by -> account(id)
* Indeksy: idx_subject_created, idx_subject_modified
* Lokalizacja: public

Tabela 3.3.2 Tabela: subject_assignment (Tabela łącznikowa: Prowadzący a Przedmiot)

| Nazwa pola             | Typ danych   | Znaczenie                                        | Wartość domyślna | Ograniczenia |
|------------------------|--------------|--------------------------------------------------|----------------|------------|
| **id** | UUID | Identyfikator przypisania                        | `gen_random_uuid()` | **PRIMARY KEY**, NOT NULL |
| account_id | UUID | ID konta prowadzącego (Teacher)                  | \- | NOT NULL |
| subject_id | UUID | ID przedmiotu                                    | \- | NOT NULL |
| has_edit_permissions | BOOLEAN | Czy jest to Prowadzący, czy Prowadzący Dodatkowy | false | NOT NULL |
| version | BIGINT | Wersja krotki                                    | 0 | NOT NULL |
| created_by | UUID | ID konta tworzącego przypisanie | \- | NOT NULL |
| creation_timestamp | TIMESTAMP | Czas utworzenia przypisania | `NOW()` | NOT NULL |
| modified_by | UUID | ID konta modyfikującego przypisanie | \- | \- |
| modification_timestamp | TIMESTAMP | Czas ostatniej modyfikacji | \- | \- |

* Klucze obce: account_id -> account(id), subject_id -> subject(id), created_by -> account(id), modified_by -> account(id)
* Ograniczenia: UNIQUE(account_id, subject_id)
* Indeksy: idx_sa_account, idx_sa_subject, idx_sa_created, idx_sa_modified
* Lokalizacja: public

Tabela 3.3.3 Tabela: rule (Reguły przedmiotu)

| Nazwa pola             | Typ danych   | Znaczenie | Wartość domyślna | Ograniczenia |
|------------------------|--------------|---------|----------------|------------|
| **id** | UUID | Identyfikator reguły | `gen_random_uuid()` | **PRIMARY KEY**, NOT NULL |
| subject_id | UUID | Identyfikator przedmiotu | \- | NOT NULL |
| report_level | VARCHAR(32) | Wymagany poziom raportowania | \- | NOT NULL |
| student_ticket_count | INTEGER | Limit dostępnych analiz prywatnych | 0 | NOT NULL, >= 0 |
| rule_preset | BOOLEAN | Czy jest to zachowany ogólny preset uczelniany | false | NOT NULL |
| version | BIGINT | Wersja krotki | 0 | NOT NULL |
| created_by | UUID | ID konta tworzącego regułę | \- | NOT NULL |
| creation_timestamp | TIMESTAMP | Czas utworzenia reguły | `NOW()` | NOT NULL |
| modified_by | UUID | ID konta modyfikującego regułę | \- | \- |
| modification_timestamp | TIMESTAMP | Czas ostatniej modyfikacji | \- | \- |

* Klucze obce: subject_id -> subject(id), report_level -> dict_report_level(level_name), created_by -> account(id), modified_by -> account(id)
* Ograniczenia: UNIQUE(subject_id) (Relacja 1:1 z Przedmiotem)
* Indeksy: idx_rule_subject, idx_rule_level, idx_rule_created, idx_rule_modified
* Lokalizacja: public

Tabela 3.3.4 Tabela: schedule (Harmonogram sprawdzeń)

| Nazwa pola             | Typ danych   | Znaczenie | Wartość domyślna | Ograniczenia |
|------------------------|--------------|---------|----------------|------------|
| **id** | UUID | Identyfikator harmonogramu | `gen_random_uuid()` | **PRIMARY KEY**, NOT NULL |
| subject_id | UUID | Identyfikator przedmiotu | \- | NOT NULL |
| schedule_date_time | TIMESTAMP | Data i godzina uruchomienia zadania | \- | NOT NULL |
| target_tag | VARCHAR(50) | Nazwa tagu w repozytorium | \- | NOT NULL |
| version | BIGINT | Wersja krotki | 0 | NOT NULL |
| created_by | UUID | ID konta tworzącego harmonogram | \- | NOT NULL |
| creation_timestamp | TIMESTAMP | Czas zaplanowania harmonogramu | `NOW()` | NOT NULL |
| modified_by | UUID | ID konta modyfikującego harmonogram | \- | \- |
| modification_timestamp | TIMESTAMP | Czas ostatniej modyfikacji | \- | \- |

* Klucze obce: subject_id -> subject(id), created_by -> account(id), modified_by -> account(id)
* Indeksy: idx_schedule_subject, idx_schedule_created, idx_schedule_modified
* Lokalizacja: public

Tabela 3.3.5 Tabela: repository (Kody źródłowe studentów)

| Nazwa pola             | Typ danych   | Znaczenie | Wartość domyślna | Ograniczenia |
|------------------------|--------------|---------|----------------|------------|
| **id** | UUID | Identyfikator repozytorium | `gen_random_uuid()` | **PRIMARY KEY**, NOT NULL |
| subject_id | UUID | Identyfikator powiązanego przedmiotu | \- | NOT NULL |
| repo_url | VARCHAR(255) | Link bazowy repozytorium | \- | NOT NULL |
| version | BIGINT | Wersja krotki | 0 | NOT NULL |
| created_by | UUID | ID konta tworzącego wpis | \- | NOT NULL |
| creation_timestamp | TIMESTAMP | Czas dodania do bazy | `NOW()` | NOT NULL |
| modified_by | UUID | ID konta modyfikującego wpis | \- | \- |
| modification_timestamp | TIMESTAMP | Czas ostatniej modyfikacji | \- | \- |

* Klucze obce: subject_id -> subject(id), created_by -> account(id), modified_by -> account(id)
* Indeksy: idx_repo_subject, idx_repo_created, idx_repo_modified
* Lokalizacja: public

Tabela 3.3.6 Tabela: student_repository (Tabela łącznikowa: Repozytorium - Studenci)

| Nazwa pola             | Typ danych   | Znaczenie | Wartość domyślna | Ograniczenia |
|------------------------|--------------|---------|----------------|------------|
| **account_id** | UUID | ID studenta będącego współautorem | \- | **PRIMARY KEY**, NOT NULL |
| **repository_id** | UUID | ID repozytorium | \- | **PRIMARY KEY**, NOT NULL |

* Klucze obce: account_id -> account(id), repository_id -> repository(id)
* Indeksy: idx_sr_account, idx_sr_repo
* Lokalizacja: public

Tabela 3.3.7 Tabela: report (Wygenerowane analizy)

| Nazwa pola             | Typ danych   | Znaczenie | Wartość domyślna | Ograniczenia |
|------------------------|--------------|---------|----------------|------------|
| **id** | UUID | Identyfikator raportu | `gen_random_uuid()` | **PRIMARY KEY**, NOT NULL |
| subject_id | UUID | Identyfikator przedmiotu | \- | NOT NULL |
| path_to_report | VARCHAR(500) | Ścieżka do fizycznego pliku na serwerze | \- | NOT NULL |
| version | BIGINT | Wersja krotki | 0 | NOT NULL |
| created_by | UUID | ID konta tworzącego wpis | \- | NOT NULL |
| creation_timestamp | TIMESTAMP | Czas wygenerowania raportu | `NOW()` | NOT NULL |
| modified_by | UUID | ID konta modyfikującego wpis | \- | \- |
| modification_timestamp | TIMESTAMP | Czas ostatniej modyfikacji | \- | \- |

* Klucze obce: subject_id -> subject(id), created_by -> account(id), modified_by -> account(id)
* Indeksy: idx_report_subject, idx_report_created, idx_report_modified
* Lokalizacja: public

_Prezentowane informacje muszą pokrywać się ze stanem faktycznym bazy danych_

### Dane inicjujące ###

#### 3.4. Pliki definicji bazy danych (DDL i DML) ####

Baza_struktury.sql (DDL)
```postgresql
-- Tworzenie kont (wykonywane z poziomu superusera postgres)
-- CREATE USER ssbd01admin WITH PASSWORD 'AdminPass123!';
-- CREATE USER ssbd01mok WITH PASSWORD 'MokPass123!';
-- CREATE USER ssbd01mpp WITH PASSWORD 'MppPass123!';
-- CREATE DATABASE ssbd01_db OWNER ssbd01admin;

-- =================================================================
-- SŁOWNIKI
-- =================================================================
CREATE TABLE public.dict_role (
                                  role_name VARCHAR(32) PRIMARY KEY,
                                  description VARCHAR(100) NOT NULL
);

CREATE TABLE public.dict_language (
                                      language_code VARCHAR(2) PRIMARY KEY
);

CREATE TABLE public.dict_report_level (
                                          level_name VARCHAR(32) PRIMARY KEY
);

-- =================================================================
-- MODUŁ MOK
-- =================================================================
CREATE TABLE public.account (
                                id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
                                login VARCHAR(50) NOT NULL UNIQUE,
                                email VARCHAR(100) NOT NULL UNIQUE,
                                password_hash CHAR(60) NOT NULL,
                                name VARCHAR(50) NOT NULL,
                                surname VARCHAR(50) NOT NULL,
                                language_code VARCHAR(2) DEFAULT 'pl' NOT NULL,
                                active BOOLEAN DEFAULT FALSE NOT NULL,
                                blocked BOOLEAN DEFAULT FALSE NOT NULL,
                                failed_attempts INTEGER DEFAULT 0 NOT NULL CHECK (failed_attempts >= 0),
                                last_successful_login TIMESTAMP,
                                last_failed_login TIMESTAMP,
                                last_successful_ip VARCHAR(45),
                                last_failed_ip VARCHAR(45),

                                version BIGINT DEFAULT 0 NOT NULL CHECK (version >= 0),
                                created_by UUID,
                                creation_timestamp TIMESTAMP DEFAULT NOW() NOT NULL,
                                modified_by UUID,
                                modification_timestamp TIMESTAMP,

                                CONSTRAINT fk_account_lang FOREIGN KEY (language_code) REFERENCES public.dict_language (language_code),
                                CONSTRAINT fk_account_created FOREIGN KEY (created_by) REFERENCES public.account (id),
                                CONSTRAINT fk_account_modified FOREIGN KEY (modified_by) REFERENCES public.account (id)
);
CREATE INDEX idx_account_login ON public.account (login);
CREATE INDEX idx_account_email ON public.account (email);
CREATE INDEX idx_account_lang ON public.account (language_code);
CREATE INDEX idx_account_created ON public.account (created_by);
CREATE INDEX idx_account_modified ON public.account (modified_by);

CREATE TABLE public.access_level (
                                     id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
                                     account_id UUID NOT NULL,
                                     role_name VARCHAR(32) NOT NULL,
                                     active BOOLEAN DEFAULT TRUE NOT NULL,

                                     version BIGINT DEFAULT 0 NOT NULL CHECK (version >= 0),
                                     created_by UUID NOT NULL,
                                     creation_timestamp TIMESTAMP DEFAULT NOW() NOT NULL,
                                     modified_by UUID,
                                     modification_timestamp TIMESTAMP,

                                     CONSTRAINT uk_account_role UNIQUE (account_id, role_name),
                                     CONSTRAINT fk_al_account FOREIGN KEY (account_id) REFERENCES public.account (id),
                                     CONSTRAINT fk_al_role FOREIGN KEY (role_name) REFERENCES public.dict_role (role_name),
                                     CONSTRAINT fk_al_created FOREIGN KEY (created_by) REFERENCES public.account (id),
                                     CONSTRAINT fk_al_modified FOREIGN KEY (modified_by) REFERENCES public.account (id)
);
CREATE INDEX idx_access_level_acc ON public.access_level (account_id);
CREATE INDEX idx_access_level_role ON public.access_level (role_name);
CREATE INDEX idx_access_level_created ON public.access_level (created_by);
CREATE INDEX idx_access_level_modified ON public.access_level (modified_by);

-- =================================================================
-- MODUŁ MPP
-- =================================================================
CREATE TABLE public.subject (
                                id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
                                name VARCHAR(100) NOT NULL,
                                year INTEGER NOT NULL CHECK (year > 2000),
                                gitea_url VARCHAR(255) NOT NULL,
                                archived BOOLEAN DEFAULT FALSE NOT NULL,

                                version BIGINT DEFAULT 0 NOT NULL,
                                created_by UUID NOT NULL,
                                creation_timestamp TIMESTAMP DEFAULT NOW() NOT NULL,
                                modified_by UUID,
                                modification_timestamp TIMESTAMP,

                                CONSTRAINT fk_sub_created FOREIGN KEY (created_by) REFERENCES public.account (id),
                                CONSTRAINT fk_sub_modified FOREIGN KEY (modified_by) REFERENCES public.account (id)
);
CREATE INDEX idx_subject_created ON public.subject (created_by);
CREATE INDEX idx_subject_modified ON public.subject (modified_by);


CREATE TABLE public.subject_assignment (
                                           id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
                                           account_id UUID NOT NULL,
                                           subject_id UUID NOT NULL,
                                           has_edit_permissions BOOLEAN DEFAULT FALSE NOT NULL,

                                           version BIGINT DEFAULT 0 NOT NULL,
                                           created_by UUID NOT NULL,
                                           creation_timestamp TIMESTAMP DEFAULT NOW() NOT NULL,
                                           modified_by UUID,
                                           modification_timestamp TIMESTAMP,

                                           CONSTRAINT uk_sub_assignment UNIQUE (account_id, subject_id),
                                           CONSTRAINT fk_sa_acc FOREIGN KEY (account_id) REFERENCES public.account (id),
                                           CONSTRAINT fk_sa_sub FOREIGN KEY (subject_id) REFERENCES public.subject (id),
                                           CONSTRAINT fk_sa_created FOREIGN KEY (created_by) REFERENCES public.account (id),
                                           CONSTRAINT fk_sa_modified FOREIGN KEY (modified_by) REFERENCES public.account (id)
);
CREATE INDEX idx_sa_account ON public.subject_assignment (account_id);
CREATE INDEX idx_sa_subject ON public.subject_assignment (subject_id);
CREATE INDEX idx_sa_created ON public.subject_assignment (created_by);
CREATE INDEX idx_sa_modified ON public.subject_assignment (modified_by);

CREATE TABLE public.rule (
                             id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
                             subject_id UUID NOT NULL UNIQUE,
                             report_level VARCHAR(32) NOT NULL,
                             student_ticket_count INTEGER DEFAULT 0 NOT NULL,
                             rule_preset BOOLEAN DEFAULT FALSE NOT NULL,

                             version BIGINT DEFAULT 0 NOT NULL,
                             created_by UUID NOT NULL,
                             creation_timestamp TIMESTAMP DEFAULT NOW() NOT NULL,
                             modified_by UUID,
                             modification_timestamp TIMESTAMP,

                             CONSTRAINT fk_rule_sub FOREIGN KEY (subject_id) REFERENCES public.subject (id),
                             CONSTRAINT fk_rule_level FOREIGN KEY (report_level) REFERENCES public.dict_report_level (level_name),
                             CONSTRAINT fk_rule_created FOREIGN KEY (created_by) REFERENCES public.account (id),
                             CONSTRAINT fk_rule_modified FOREIGN KEY (modified_by) REFERENCES public.account (id)
);
CREATE INDEX idx_rule_subject ON public.rule (subject_id);
CREATE INDEX idx_rule_level ON public.rule (report_level);
CREATE INDEX idx_rule_created ON public.rule (created_by);
CREATE INDEX idx_rule_modified ON public.rule (modified_by);

CREATE TABLE public.schedule (
                                 id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
                                 subject_id UUID NOT NULL,
                                 schedule_date_time TIMESTAMP NOT NULL,
                                 target_tag VARCHAR(50) NOT NULL,

                                 version BIGINT DEFAULT 0 NOT NULL,
                                 created_by UUID NOT NULL,
                                 creation_timestamp TIMESTAMP DEFAULT NOW() NOT NULL,
                                 modified_by UUID,
                                 modification_timestamp TIMESTAMP,

                                 CONSTRAINT fk_sch_sub FOREIGN KEY (subject_id) REFERENCES public.subject (id),
                                 CONSTRAINT fk_sch_created FOREIGN KEY (created_by) REFERENCES public.account (id),
                                 CONSTRAINT fk_sch_modified FOREIGN KEY (modified_by) REFERENCES public.account (id)
);
CREATE INDEX idx_schedule_subject ON public.schedule (subject_id);
CREATE INDEX idx_schedule_created ON public.schedule (created_by);
CREATE INDEX idx_schedule_modified ON public.schedule (modified_by);


CREATE TABLE public.repository (
                                   id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
                                   subject_id UUID NOT NULL,
                                   repo_url VARCHAR(255) NOT NULL,

                                   version BIGINT DEFAULT 0 NOT NULL,
                                   created_by UUID NOT NULL,
                                   creation_timestamp TIMESTAMP DEFAULT NOW() NOT NULL,
                                   modified_by UUID,
                                   modification_timestamp TIMESTAMP,

                                   CONSTRAINT fk_repo_sub FOREIGN KEY (subject_id) REFERENCES public.subject (id),
                                   CONSTRAINT fk_repo_created FOREIGN KEY (created_by) REFERENCES public.account (id),
                                   CONSTRAINT fk_repo_modified FOREIGN KEY (modified_by) REFERENCES public.account (id)
);
CREATE INDEX idx_repo_subject ON public.repository (subject_id);
CREATE INDEX idx_repo_created ON public.repository (created_by);
CREATE INDEX idx_repo_modified ON public.repository (modified_by);


CREATE TABLE public.student_repository (
                                           account_id UUID NOT NULL,
                                           repository_id UUID NOT NULL,
                                           PRIMARY KEY (account_id, repository_id),
                                           CONSTRAINT fk_sr_acc FOREIGN KEY (account_id) REFERENCES public.account (id),
                                           CONSTRAINT fk_sr_repo FOREIGN KEY (repository_id) REFERENCES public.repository (id)
);
CREATE INDEX idx_sr_account ON public.student_repository (account_id);
CREATE INDEX idx_sr_repo ON public.student_repository (repository_id);


CREATE TABLE public.report (
                               id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
                               subject_id UUID NOT NULL,
                               path_to_report VARCHAR(500) NOT NULL,

                               version BIGINT DEFAULT 0 NOT NULL,
                               created_by UUID NOT NULL,
                               creation_timestamp TIMESTAMP DEFAULT NOW() NOT NULL,
                               modified_by UUID,
                               modification_timestamp TIMESTAMP,

                               CONSTRAINT fk_rep_sub FOREIGN KEY (subject_id) REFERENCES public.subject (id),
                               CONSTRAINT fk_rep_created FOREIGN KEY (created_by) REFERENCES public.account (id),
                               CONSTRAINT fk_rep_modified FOREIGN KEY (modified_by) REFERENCES public.account (id)
);
CREATE INDEX idx_report_subject ON public.report (subject_id);
CREATE INDEX idx_report_created ON public.report (created_by);
CREATE INDEX idx_report_modified ON public.report (modified_by);


-- =================================================================
-- UPRAWNIENIA (Role RBAC bazodanowego dla MOK i MPP)
-- =================================================================
GRANT SELECT, INSERT, UPDATE ON public.account TO ssbd01mok;
GRANT SELECT, INSERT, UPDATE ON public.access_level TO ssbd01mok;

GRANT SELECT ON public.account TO ssbd01mpp;
GRANT SELECT ON public.access_level TO ssbd01mpp;
GRANT SELECT, INSERT, UPDATE, DELETE ON public.subject TO ssbd01mpp;
GRANT SELECT, INSERT, UPDATE, DELETE ON public.subject_assignment TO ssbd01mpp;
GRANT SELECT, INSERT, UPDATE, DELETE ON public.rule TO ssbd01mpp;
GRANT SELECT, INSERT, UPDATE, DELETE ON public.schedule TO ssbd01mpp;
GRANT SELECT, INSERT, UPDATE, DELETE ON public.repository TO ssbd01mpp;
GRANT SELECT, INSERT, UPDATE, DELETE ON public.student_repository TO ssbd01mpp;
GRANT SELECT, INSERT, UPDATE, DELETE ON public.report TO ssbd01mpp;

-- Tabele słownikowe (tylko odczyt dla warstw aplikacyjnych)
GRANT SELECT ON public.dict_role TO ssbd01mok, ssbd01mpp;
GRANT SELECT ON public.dict_language TO ssbd01mok, ssbd01mpp;
GRANT SELECT ON public.dict_report_level TO ssbd01mpp;
```

Baza_dane_init.sql (DML)
```postgresql
-- Wypełnienie słowników
-- Wypełnienie wszystkich tabel słownikowych wymaganymi zestawami stałych
INSERT INTO public.dict_role (role_name, description) VALUES
                                                          ('ADMIN', 'Globalny administrator systemu'),
                                                          ('TEACHER', 'Prowadzący zajęcia'),
                                                          ('STUDENT', 'Student');

INSERT INTO public.dict_language (language_code) VALUES ('pl'), ('en');

INSERT INTO public.dict_report_level (level_name) VALUES
                                                      ('PEŁNY_WGLĄD'), ('TYLKO_PROCENTY'), ('BRAK_WGLĄDU');

-- Inicjalizacja kluczowych krotek pozwalających na uruchomienie i testy aplikacji
DO $$
    DECLARE
        admin_id UUID := gen_random_uuid();
        teacher_id UUID := gen_random_uuid();
        student_id UUID := gen_random_uuid();
        sub_id UUID := gen_random_uuid();
    BEGIN
        -- 1. UTWORZENIE ADMINISTRATORA (Wymóg utworzenia konta pozwalającego tworzyć następne)
        INSERT INTO public.account (id, login, email, password_hash, name, surname, language_code, active, blocked, version, created_by)
        VALUES (admin_id, 'admin', 'admin@ssbd.edu.pl', '$2a$12$NqL7C7wYxGz4mFhX1Rj8dO2Bw4oF7h.v5.1oZq4e6xZ2o2X0X3z6e', 'Adam', 'Root', 'pl', true, false, 0, admin_id);

        INSERT INTO public.access_level (account_id, role_name, active, version, created_by)
        VALUES (admin_id, 'ADMIN', true, 0, admin_id);

        -- 2. UTWORZENIE PROWADZĄCEGO (Przez admina)
        INSERT INTO public.account (id, login, email, password_hash, name, surname, language_code, active, blocked, version, created_by)
        VALUES (teacher_id, 'jkowalski', 'jkowalski@ssbd.edu.pl', '$2a$12$NqL7C7wYxGz4mFhX1Rj8dO2Bw4oF7h.v5.1oZq4e6xZ2o2X0X3z6e', 'Jan', 'Kowalski', 'pl', true, false, 0, admin_id);

        INSERT INTO public.access_level (account_id, role_name, active, version, created_by)
        VALUES (teacher_id, 'TEACHER', true, 0, admin_id);

        -- 3. UTWORZENIE STUDENTA (Przez admina)
        INSERT INTO public.account (id, login, email, password_hash, name, surname, language_code, active, blocked, version, created_by)
        VALUES (student_id, 'anowak', 'anowak@ssbd.edu.pl', '$2a$12$NqL7C7wYxGz4mFhX1Rj8dO2Bw4oF7h.v5.1oZq4e6xZ2o2X0X3z6e', 'Anna', 'Nowak', 'pl', true, false, 0, admin_id);

        INSERT INTO public.access_level (account_id, role_name, active, version, created_by)
        VALUES (student_id, 'STUDENT', true, 0, admin_id);

        -- 4. UTWORZENIE PRZEDMIOTU (Przez prowadzącego)
        INSERT INTO public.subject (id, name, year, gitea_url, archived, version, created_by)
        VALUES (sub_id, 'Algorytmy i Struktury Danych', 2026, 'https://gitea.it.p.lodz.pl/asd_2026', false, 0, teacher_id);

        -- Przypisanie prowadzącego do przedmiotu jako Głównego
        INSERT INTO public.subject_assignment (account_id, subject_id, has_edit_permissions, version, created_by)
        VALUES (teacher_id, sub_id, true, 0, teacher_id);

        -- Dodanie domyślnego presetu reguł dla przedmiotu
        INSERT INTO public.rule (subject_id, report_level, student_ticket_count, rule_preset, version, created_by)
        VALUES (sub_id, 'PEŁNY_WGLĄD', 3, true, 0, teacher_id);

    END $$;
```
