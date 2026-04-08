
## 4 Użytkownicy bazodanowi 2 pkt



**Lista użytkowników bazodanowych.**

ssbd01admin - Główny administrator bazy danych. Konto techniczne, nieprzypisane do żadnego modułu aplikacji. Służy wyłącznie administratorom do ręcznego zarządzania bazą.

ssbd01mok - Użytkownik przypisany do Modułu Obsługi Kont. Ma uprawnienia ograniczone wyłącznie do tabel przechowujących dane uwierzytelniające, informacje o kontach oraz poziomach dostępu.

ssbd01mpp - Użytkownik przypisany do Modułu Poszukiwania Plagiatu. Odpowiada za zarządzanie cyklem życia przedmiotów, repozytoriami, regułami, harmonogramami oraz umożliwia Prowadzącym wgląd i usuwanie wygenerowanych raportów z analizy.



**Tabela uprawnień użytkowników bazodanowych.**

Tabela 1.4.1 Tabela uprawnień dla użytkowników bazodanowych

| Tabela\Użytkownik  | ssbd01admin | ssbd01mok | ssbd01mpp | 
|--------------------|-------------|-----------|-----------|
| access_level_dict  | S,I,U,D     | S         | S         |
| language_dict      | S,I,U,D     | S         | S         |
| raport_level_dict  | S,I,U,D     | -         | S         |
| account            | S,I,U,D     | S,I,U,D   | S         |
| access_level       | S,I,U,D     | S,I,U,D   | S         |
| admin              | S,I,U,D     | S,I,U     | S         |
| teacher            | S,I,U,D     | S,I,U     | S         |
| student            | S,I,U,D     | S,I,U,D   | S         |
| subject            | S,I,U,D     | -         | S,I,U,D   |
| subject_assignment | S,I,U,D     | -         | S,I,U,D   |
| rule_preset        | S,I,U,D     | -         | S,I,U,D   |
| schedule           | S,I,U,D     | -         | S,I,U,D   |
| repository         | S,I,U,D     | -         | S,I,U,D   |
| repository_student | S,I,U,D     | -         | S,I,U,D   |
| report             | S,I,U,D     | -         | S,I,U,D   |
| report_repository  | S,I,U,D     | -         | S,I,U,D   |


**Legenda**

Tabela 1.4.2 Legenda oznaczeń dla Tabeli 1.4.1

|Skrót| S      | U      | I      | D      |
|---|--------|--------|--------|--------|
|Nazwa| SELECT | UPDATE | INSERT | DELETE | 



```sql
CREATE USER ssbd01mok WITH PASSWORD 'MokPass123!';
CREATE USER ssbd01mpp WITH PASSWORD 'MppPass123!';
CREATE DATABASE ssbd01_db OWNER ssbd01admin;
```
Listing 1.4.1 Plik init.sql, w którym tworzeni są użytkownicy bazodanowi.

```sql
GRANT SELECT, INSERT, UPDATE, DELETE ON public.account TO ssbd01mok;
GRANT SELECT, INSERT, UPDATE, DELETE ON public.access_level TO ssbd01mok;
GRANT SELECT, INSERT, UPDATE ON public.admin TO ssbd01mok;
GRANT SELECT, INSERT, UPDATE ON public.teacher TO ssbd01mok;
GRANT SELECT, INSERT, UPDATE, DELETE ON public.student TO ssbd01mok;

GRANT SELECT ON public.account TO ssbd01mpp;
GRANT SELECT ON public.access_level TO ssbd01mpp;
GRANT SELECT ON public.admin TO ssbd01mpp;
GRANT SELECT ON public.teacher TO ssbd01mpp;
GRANT SELECT ON public.student TO ssbd01mpp;
GRANT SELECT, INSERT, UPDATE, DELETE ON public.subject TO ssbd01mpp;
GRANT SELECT, INSERT, UPDATE, DELETE ON public.subject_assignment TO ssbd01mpp;
GRANT SELECT, INSERT, UPDATE, DELETE ON public.rule_preset TO ssbd01mpp;
GRANT SELECT, INSERT, UPDATE, DELETE ON public.schedule TO ssbd01mpp;
GRANT SELECT, INSERT, UPDATE, DELETE ON public.repository TO ssbd01mpp;
GRANT SELECT, INSERT, UPDATE, DELETE ON public.repository_student TO ssbd01mpp;
GRANT SELECT, INSERT, UPDATE, DELETE ON public.report TO ssbd01mpp;
GRANT SELECT, INSERT, UPDATE, DELETE ON public.report_repository TO ssbd01mpp;

GRANT SELECT ON public.access_level_dict TO ssbd01mok, ssbd01mpp;
GRANT SELECT ON public.language_dict TO ssbd01mok, ssbd01mpp;
GRANT SELECT ON public.raport_level_dict TO ssbd01mpp;
```

Listing 1.4.2 Plik V2_Grant-privileges.sql określający uprawnienia dla użytkowników bazodanowych
