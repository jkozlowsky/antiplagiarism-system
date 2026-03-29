


## 2 Moduły funkcjonalne, aktorzy, przypadki użycia 3 pkt

**Lista modułów funkcjonalnych**
- MOK - Moduł Obsługi Kont
- MPP - Moduł Poszkiwania Plagiatu

**Lista poziomów dostępu (aktorów)**

Tabela 1.2.1 Przedstawienie poziomów dostępu systemu
|Poziom dostępu (Aktor)|Opis danego poziomu dostępu|Sekwencja czynności prowadząca do utworzenia funkcjonującego konta o wskazanym poziomie dostępu|
|-|-|-|
|Gość|Użytkownik nieuwierzytelniony, aby skorzystać z funkcjonalnośći aplikacji należy zarejestrować się lub zalogować się na obecnie istniejące konto z innym poziomem dostępu.|Nie dotyczy|
|System|System to aktor w systemie odpowiadający za automatyczne wykonywanie zaplanowanych akcji bez dodatkowego działania Prowadzącego.|Nie dotyczy|
|Administrator|Administrator to jedyny aktor posiadający uprawnienia pozwalające na zarządzanie wszystkimi dostępnymi kontami w systemie.|Administrator to domyślnie utworzony użytkownik wraz z  początkiem działania systemu.|
|Prowadzący|Prowadzący to nauczyciel akademicki, który zarządza dostępnymi przedmiotami wraz z ich regułami oraz harmonogramami i monitoruje wyniki Studentów względem możliwego plagiatu.|Poziom dostępu może zostać nadany przez Administratora systemu, wymagana jest wcześniejsza poprawna rejestracja w systemie (należy utworzyć konto z poziomu Gościa).|
|Prowadzący dodatkowy|Prowadzący dodatkowy to nauczyciel akademicki posiadający jedynie prawo do odczytu w danym przedmiocie.|Poziom dostępu może zostać nadany przez Administratora systemu, wymagana jest wcześniejsza poprawna rejestracja w systemie (należy utworzyć konto z poziomu Gościa).|
|Student|Student to uczeń na danej uczelni wyższej, realizujący dany przedmiot gdzie występują zadania programistyczne, które są weryfikowane względem możliwego plagiatu.|Poziom dostępu może zostać nadany przez Administratora systemu, wymagana jest wcześniejsza poprawna rejestracja w systemie (należy utworzyć konto z poziomu Gościa).|

Uwaga! W jednym przedmiocie dana osoba nie może być jednocześnie Prowadzącym oraz Prowadzącym dodatkowym z uwagi na wzajemne wykluczanie się uprawnień. Jednakże w obrębie systemu, jedna osoba może być Prowadzącym oraz Prowadzącym dodatkowym w wielu różnych przedmiotach.

**Diagramy przypadków użycia**

![alt text](img/uc_gosc.png)

Rysunek 1.2.1 Diagram przypadków użycia dla aktora Gość

![alt text](img/uc_system.png)

Rysunek 1.2.2 Diagram przypadków użycia dla aktora System

![alt text](img/uc_admin.png)

Rysunek 1.2.3 Diagram przypadków użycia dla aktora Administrator

![alt text](img/uc_pro.png)

Rysunek 1.2.4 Diagram przypadków użycia dla aktora Prowadzący

![alt text](img/uc_prodod.png)

Rysunek 1.2.5 Diagram przypadków użycia dla aktora Prowadzący dodatkowy

![alt text](img/uc_student.png)

Rysunek 1.2.6 Diagram przypadków użycia dla aktora Student


Tabela 1.2.2 Scenariusze przypadków użycia

|Przypadek użycia|Twórca|Poziom ważności|Typ przypadku użycia|Aktorzy|Krótki opis|Warunki wstępne|Warunki końcowe|Główny przepływ zdarzeń|Alternatywne przepłīwy zdarzeń|Specjalne wymagania|Notatki i kwestie|
|--|--|--|--|--|--|--|--|--|--|--|--|
|MOK.1| | | | | | | | | | | |
|MOK.2| | | | | | | | | | | |
|MOK.3| | | | | | | | | | | |
|MOK.4| | | | | | | | | | | |
|MOK.5| | | | | | | | | | | |
|MOK.6| | | | | | | | | | | |
|MOK.7| | | | | | | | | | | |
|MOK.8| | | | | | | | | | | |
|MOK.9| | | | | | | | | | | |
|MOK.10| | | | | | | | | | | |
|MOK.11| | | | | | | | | | | |
|MOK.12| | | | | | | | | | | |
|MOK.13| | | | | | | | | | | |
|MOK.14| | | | | | | | | | | |
|MPP.1| | | | | | | | | | | |
|MPP.2| | | | | | | | | | | |
|MPP.3| | | | | | | | | | | |
|MPP.4| | | | | | | | | | | |
|MPP.5| | | | | | | | | | | |
|MPP.6| | | | | | | | | | | |
|MPP.7| | | | | | | | | | | |
|MPP.8| | | | | | | | | | | |
|MPP.9| | | | | | | | | | | |
|MPP.10| | | | | | | | | | | |
|MPP.11| | | | | | | | | | | |
|MPP.12| | | | | | | | | | | |
|MPP.13| | | | | | | | | | | |
|MPP.14| | | | | | | | | | | |
|MPP.15| | | | | | | | | | | |
|MPP.16| | | | | | | | | | | |
|MPP.17| | | | | | | | | | | |
|MPP.18| | | | | | | | | | | |
|MPP.19| | | | | | | | | | | |
|MPP.20| | | | | | | | | | | |
|MPP.21| | | | | | | | | | | |

**Lista modułów funkcjonalnych i przypadków użycia**

- MOK.1 - Rejestracja konta użytkownika
- MOK.2 - Uwierzytelnienie użytkownika
- MOK.3 - Reset hasła
- MOK.4 - Zablokuj konto użytkownika
- MOK.5 - Odblokuj konto użytkownika
- MOK.6 - Dołącz poziom dostępu do konta
- MOK.7 - Odłącz poziom dostępu od konta
- MOK.8 - Zmień własne hasło
- MOK.9 - Zmień hasło innego użytkownika
- MOK.10 - Zmień adres e-mail własnego konta
- MOK.11 - Zmień adres e-mail innego konta
- MOK.12 - Edytuj dane własnego konta
- MOK.13 - Edytuj dane konta innego użytkownika
- MOK.14 - Wyloguj
- MPP.1 - Generowanie raportu po ustawieniu harmonogramu
- MPP.2 - Generowanie raportu na żądanie
- MPP.3 - Przeglądanie wszystkich raportów
- MPP.4 - Przeglądanie jednego raportu
- MPP.5 - Przeglądanie przedmiotów
- MPP.6 - Filtrowanie przedmiotów
- MPP.7 - Sortowanie przedmiotów
- MPP.8 - Usuwanie raportów
- MPP.9 - Przeglądanie repozytoriów
- MPP.10 - Przeglądanie kont użytkowników
- MPP.11 - Przeglądanie harmonogramu
- MPP.12 - Przeglądanie reguł
- MPP.13 - Tworzenie przedmiotu
- MPP.14 - Edycja przedmiotu
- MPP.15 - Usuwanie przedmiotu
- MPP.16 - Tworzenie reguł
- MPP.17 - Edycja reguł
- MPP.18 - Usuwanie reguł
- MPP.19 - Sprawdzenie swojego kodu
- MPP.20 - Przeglądanie swoich raportów
- MPP.21 - Wygeneruj raport według harmonogramu

