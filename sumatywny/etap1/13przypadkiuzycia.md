## 1.3. Słownik aktorów (ról) i diagramy UML przypadków użycia

Tabela 1.3.1 Słownik aktorów w systemie

|Aktor|Opis roli aktora|Przypadki użycia aktora|
|-|-|-|
|Gość|Użytkownik nieuwierzytelniony, aby skorzystać z funkcjonalnośći aplikacji należy zarejestrować się lub zalogować się na obecnie istniejące konto z innym poziomem dostępu.|Z wyjątkiem Modułu Obsługi Kont - brak|
|Administrator|Administrator to jedyny aktor posiadający uprawnienia pozwalające na zarządzanie wszystkimi dostępnymi kontami w systemie.|MPP.22 - Zmiana kierownika przedmiotu|
|Student|Student to uczeń, który realizuje dany przedmiot lub przedmioty na semestrze, gdzie jest zobligowany do zamieszczania kodu na repozytorium.|MPP.19 - Sprawdzenie swojego kodu<br />MPP.20 - Przeglądanie swoich raportów|
|Prowadzący|Prowadzący to nauczyciel akademicki, który zarządza dostępnymi przedmiotami wraz z ich regułami oraz harmonogramami i monitoruje wyniki Studentów względem możliwego plagiatu.|MPP.1 - Generowanie raportu po ustawieniu harmonogramu<br />MPP.2 - Generowanie raportu na żądanie<br />MPP.3 - Przeglądanie wszystkich raportów<br />MPP.4 - Przeglądanie jednego raportu<br />MPP.5 - Przeglądanie przedmiotów<br />MPP.6 - Filtrowanie przedmiotów<br />MPP.7 - Sortowanie przedmiotów<br />MPP.8 - Usuwanie raportów<br />MPP.9 - Przeglądanie repozytoriów<br />MPP.10 - Przeglądanie kont użytkowników<br />MPP.11 - Przeglądanie harmonogramu<br />MPP.12 - Przeglądanie reguł<br />MPP.13 - Tworzenie przedmiotu<br />MPP.14 - Edycja przedmiotu<br />MPP.15 - Usuwanie przedmiotu<br />MPP.16 - Tworzenie reguł<br />MPP.17 - Edycja reguł<br />MPP.18 - Usuwanie reguł|
|Prowadzący dodatkowy|Prowadzący dodatkowy to nauczyciel akademicki posiadający jedynie prawo do odczytu w danym przedmiocie.|MPP.2 - Generowanie raportu na żądanie<br />MPP.3 - Przeglądanie wszystkich raportów<br />MPP.4 - Przeglądanie jednego raportu<br />MPP.5 - Przeglądanie przedmiotów<br />MPP.6 - Filtrowanie przedmiotów<br />MPP.7 - Sortowanie przedmiotów<br />MPP.9 - Przeglądanie repozytoriów<br />MPP.10 - Przeglądanie kont użytkowników<br />MPP.12 - Przeglądanie reguł|
|System|System to aktor w systemie odpowiadający za automatyczne wykonywanie zaplanowanych akcji bez dodatkowego działania Prowadzącego.|MPP.21 - Wygeneruj raport według harmonogramu|

<br>

![Diagram przypadków użycia - aktor Student](etap1/img/uc_student.png)

Rysunek 1.3.1 Diagram przypadków użycia dla aktora Student

![Diagram przypadków użycia - aktor Prowadzący](etap1/img/uc_teacher.png)

Rysunek 1.3.2 Diagram przypadków użycia dla aktora Prowadzący

![Diagram przypadków użycia - aktor Prowadzący dodatkowy](etap1/img/uc_additional_teacher.png)

Rysunek 1.3.3 Diagram przypadków użycia dla aktora Prowadzący dodatkowy

![Diagram przypadków użycia - aktor System](etap1/img/uc_system.png)

Rysunek 1.3.4 Diagram przypadków użycia dla aktora System

![Diagram przypadków użycia - aktor Administrator](etap1/img/uc_admin.png)

Rysunek 1.3.5 Diagram przypadków użycia dla aktora Administrator

Dla aktora Gość nie zamieszczono diagramu przypadków użycia, ponieważ nie posiada on innych przypadków użycia oprócz tych zawartych w Module Obsługi Kont.