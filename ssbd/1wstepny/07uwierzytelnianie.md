
## 7 Model kont i uwierzytelnianie w aplikacji 1 pkt

**Model kont**

W bazie danych muszą znajdować się krotki umożliwiające przeprowadzenie procesu uwierzytelniania dla przykładowych kont użytkowników aplikacji, przy czym przynajmniej jedno z tych kont musi występować jednocześnie w wielu poziomach dostępu

**Procedury zarządzania kontami**

Należy zwięźle opisać planowane procedury zarządzania kontami:
* rejestracja kont - w przypadku stosowania dla różnych poziomów dostępu innych mechanizmów wymagany opis każdego z nich.
* weryfikacji konta po rejestracji samodzielnej - w przypadku stosowania dla różnych poziomów dostępu innych mechanizmów wymagany opis każdego z nich.
* usuwania nieaktywnych kont po upływie limitu czasu na aktywację zarejestrowanego konta.
* blokowania i odblokowania konta
* przydzielania i odbierania poziomów dostępu

Przedstawione opisy muszą odnosić się do modyfikacji dokonywanych w krotkach bazy danych, precyzyjnie wyjaśniając jak wykonywane operacje wpływają na zmiany rekordów w poszczególnych tabelach relacyjnej bazy danych