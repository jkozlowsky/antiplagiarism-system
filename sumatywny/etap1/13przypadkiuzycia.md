## 1.3. Słownik aktorów (ról) i diagramy UML przypadków użycia

Tabela 1.3.1 Słownik aktorów w systemie

|Aktor|Opis roli aktora|Przypadki użycia aktora|
|-|-|-|
|Gość|Użytkownik nieuwierzytelniony, nieposiadający dostępu do głównych funkcjonalności systemu. W celu wyświetlenia dalszych informacji oprócz strony głównej i strony logowania, użytkownik musi zalogować się do systemu.|Z wyjątkiem Modułu Obsługi Kont - brak|
|Administrator|Administrator systemu, posiadający uprawnienia do zarządzania Modułem Obsługi Kont.|Z wyjątkiem Modułu Obsługi Kont - brak|
|Student|Student to uczeń, który realizuje dany przedmiot lub przedmioty na semestrze, gdzie jest zobligowany do zamieszczania kodu na repozytorium.|Sprawdzanie swojego kodu - MPP X<br />Przeglądanie swoich raportów - MPP X|
|Prowadzący|Prowadzący to nauczyciel, który ma dostęp do danego przedmiotu, może precyzować zakresy uprawnień dla Studentów i inne ustawienia przedmiotu. Jest on właścicielem danego przedmiotu.|Generowanie raportów - MPP X<br />Generowanie raportów po ustawieniu harmonogramu - MPP X<br />Generowanie raportów na żądanie - MPP X<br />Przeglądanie raportów - MPP X<br />Przeglądanie wszystkich raportów - MPP X<br />Przeglądanie jednego raportu - MPP X<br />Przeglądanie przedmiotów - MPP X<br />Sortuj przedmioty - MPP X<br />Filtruj przedmioty - MPP X<br />Usuwanie raportów - MPP X<br />Przeglądanie repozytoriów - MPP X<br />Przeglądanie kont użytkowników - MPP X<br />Tworzenie harmonogramu - MPP X<br />Tworzenie przedmiotu - MPP X<br />Edycja przedmiotu - MPP X<br />Usuwanie przedmiotu - MPP X<br />Tworzenie reguł - MPP X<br />Przeglądanie reguł - MPP X<br />Edycja reguł - MPP X<br />Usuwanie reguł - MPP X|
|Prowadzący dodatkowy|Prowadzący dodatkowy to nauczyciel, który jest współrealizatorem danego przedmiotu, jednak nie posiada praw edycji.|Generowanie raportów - MPP X<br />Generowanie raportów na żądanie - MPP X<br />Przeglądanie raportów - MPP X<br />Przeglądanie wszystkich raportów - MPP X<br />Przeglądanie jednego raportu - MPP X<br />Przeglądanie przedmiotów - MPP X<br />Sortuj przedmioty - MPP X<br />Filtruj przedmioty - MPP X<br />Przeglądanie repozytoriów - MPP X<br />Przeglądanie kont użytkowników - MPP X<br />Przeglądanie reguł - MPP X|
|System|System to aktor w systemie odpowiadający za automatycznie wykonywane i zaplanowane akcje bez dodatkowego działania Prowadzącego.|Wygeneruj raport według harmonogramu - MPP X|

<br>

![Diagram przypadków użycia - aktor Student](/img/uc_student.png)

Rysunek 1.3.1 Diagram przypadków użycia dla aktora Student


![Diagram przypadków użycia - aktor Prowadzący](/img/uc_teacher.png)

Rysunek 1.3.2 Diagram przypadków użycia dla aktora Prowadzący

![Diagram przypadków użycia - aktor Prowadzący dodatkowy](/img/uc_additional_teacher.png)

Rysunek 1.3.3 Diagram przypadków użycia dla aktora Prowadzący dodatkowy

![Diagram przypadków użycia - aktor System](/img/uc_system.png)

Rysunek 1.3.4 Diagram przypadków użycia dla aktora System

Dla aktorów Gość oraz Administrator nie zamieszczono diagramów przypadków użycia, ponieważ oboje nie posiadają innych przypadków użycia oprócz tych zawartych w Module Obsługi Kont.