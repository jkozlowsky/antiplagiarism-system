
## 4 Użytkownicy bazodanowi 2 pkt



**Lista użytkowników bazodanowych.**

ssbd01admin - Główny administrator bazy danych. Konto techniczne, nieprzypisane do żadnego modułu aplikacji. Służy wyłącznie administratorom do ręcznego zarządzania bazą.

ssbd01mok - Użytkownik przypisany do Modułu Obsługi Kont. Ma uprawnienia ograniczone wyłącznie do tabel przechowujących dane uwierzytelniające, informacje o kontach oraz poziomach dostępu.

ssbd01mpp - Użytkownik przypisany do Modułu Poszukiwania Plagiatu. Odpowiada za zarządzanie cyklem życia przedmiotów, repozytoriami, regułami, harmonogramami oraz umożliwia Prowadzącym wgląd i usuwanie wygenerowanych raportów z analizy.



**Tabela uprawnień użytkowników bazodanowych.**

Tabela 1.4.1 Tabela uprawnień dla użytkowników bazodanowych

| Tabela\Użytkownik  | ssbd01admin | ssbd01mok | ssbd01mpp | 
|--------------------|-------------|-----------|-----------|
| dict_role          | S,I,U,D     | S         | S         |
| dict_language      | S,I,U,D     | S         | S         |
| dict_report_level  | S,I,U,D     | -         | S         |
| account            | S,I,U,D     | S,I,U     | S         |
| access_level       | S,I,U,D     | S,I,U     | S         |
| subject            | S,I,U,D     | -         | S,I,U,D   |
| subject_assignment | S,I,U,D     | -         | S,I,U,D   |
| rule               | S,I,U,D     | -         | S,I,U,D   |
| schedule           | S,I,U,D     | -         | S,I,U,D   |
| repository         | S,I,U,D     | -         | S,I,U,D   |
| student_repository | S,I,U,D     | -         | S,I,U,D   |
| report             | S,I,U,D     | -         | S,I,U,D   |


**Legenda**

Tabela 1.4.2 Legenda oznaczeń dla Tabeli 1.4.1

|Skrót| S      | U      | I      | D      |
|---|--------|--------|--------|--------|
|Nazwa| SELECT | UPDATE | INSERT | DELETE | 
