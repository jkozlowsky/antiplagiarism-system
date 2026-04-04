
## 4 Użytkownicy bazodanowi 2 pkt





**Lista użytkowników bazodanowych.**

admin - Głowny administrator bazy danych. Konto techniczne, nieprzypisane do żdadnego modułu aplikacji. Służy wyłacznie administratorom do ręcznego zarządzania bazą (rarządzanie rolami, awaryjne interwencje). Nie jest wykorzytywane przez aplikacje.

flyway_user - Użytkownik techniczny wykorzystywany wyłacznie w procesie wdrażania (CI/CD) przez narzędzie Flyway. Służy do wersjonowani i automatyczniej migracji schematu bazy danych. Posiada uprawnienia strukturalne (DDL), ale nie beirze uidziału w obsłudze żądań użytkowników.

mok_user - Użytkownik przypisany do Modułu Obsługi Kont. Posiada uprawienia ograniczone wyłącznie do tabel przechowujących dane uwierzytelniające, informacje o kontach oraz poziomach dostępu.

mpp_user - Użytkownik przypisany do Modułu Poszukiwania Plagiatu. Odpowiada za zarządzanie cyklem życia przedmiotów, repozytoriami, regułami, harmonogramami oraz umożliwia Prowadzącym wgląd i usuwanie wygenerowanych raportów z analizy. 

system_analyzer_user - Użytmownik techniczny obsługujący procesy w tle, realizowane przez aktora "System". Odpowiada za odczyt harmonogramów, weryfikacje repozytorów oraz zapis wyników analizy po zakończeniu przetwarzania danych przez biblioteke JPlag.



**Tabela uprawnień użytkowników bazodanowych.**

| Tabela\Użytkownik | admin   | mok_user | mpp_user | system_analyzer_user | 
|-------------------|---------|----------|----------|----------------------|
| account           | S,I,U,D | S,I,U,D  | S        | -                    |
| access_level      | S,I,U,D | S,I,U,D  | S        | -                    |
| admin             | S,I,U,D | S,I,U,D  | S        | -                    |
| teacher           | S,I,U,D | S,I,U,D  | S        | -                    |
| student           | S,I,U,D         | S,I,U,D  | S        | -                    |
| subject           | S,I,U,D         | -        | S,I,U,D  | S                    |
| rule              | S,I,U,D         | -        | S,I,U,D  | S                    |
| tag               | S,I,U,D         | -        | S,I,U,D  | S                    |
| schedule          | S,I,U,D         | -        | S,I,U,D  | S,U                  |
| repository        | S,I,U,D         | -        | S,I,U,D  | S                    |
| report            | S,I,U,D         | -        | S,D      | S,I,U                |

**Legenda**

|Skrót| S      | U      | I      | D      |
|---|--------|--------|--------|--------|
|Nazwa| SELECT | UPDATE | INSERT | DELETE | 




**Lista użytkowników bazodanowych.**

Dla każdego użytkownika należy podać nazwę przypisanego do niego modułu funkcjonalnego lub krótki opis zastosowania danego użytkownika bazodanowego, o ile nie
jest on utworzony na potrzeby modułu funkcjonalnego.

_Prezentowane informacje muszą pokrywać się ze stanem faktycznym bazy danych_

**Tabela uprawnień użytkowników bazodanowych.**

Należy w formie tabeli przedstawić uprawnienia poszczególnych użytkowników bazodanowych do wykonywania kwerend SELECT, INSERT, UPDATE, DELETE na poszczególnych tabelach. Zastosować jednoliterowe akronimy dla poszczególnych uprawnień i opisać je w legendzie umieszczonej pod tabelą.

_Prezentowane informacje muszą pokrywać się ze stanem faktycznym bazy danych_