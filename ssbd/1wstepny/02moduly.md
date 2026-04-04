


## 2 Moduły funkcjonalne, aktorzy, przypadki użycia 3 pkt

**Lista modułów funkcjonalnych**
- MOK - Moduł Obsługi Kont
- MPP - Moduł Poszukiwania Plagiatu

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

Tabela 1.2.2 Scenariusz dla przypadku użycia MOK.1

| Pole                           | Treść                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                       |
|--------------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Oznaczenie                     | MOK.1                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                       |
| Przypadek użycia               | Rejestracja konta użytkownika                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                               |
| Twórca                         | Karol Dawid                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                 |
| Poziom ważności                | Wysoki                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                      |
| Typ przypadku użycia           | Podstawowy                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                  |
| Aktorzy                        | Gość                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                        |
| Krótki opis                    | Przypadek użycia pozwala niezalogowanemu użytkownikowi (Gościowi) na utworzenie nowego konta w systemie. Zgodnie z założeniami biznesowymi, po pomyślnej rejestracji konto automatycznie otrzymuje uprawnienia Studenta, jednak wymaga potwierdzenia tożsamości poprzez e-mail                                                                                                                                                                                                                                                                                                                                                                                                                                                                                              |
| Warunki wstępne                | Gość nie jest zalogowany w systemie i posiada dostęp do formularza rejestracyjnego.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                         |
| Warunki końcowe                | W bazie danych systemu zostaje utworzone nowe konto użytkownika z domyślnie przypisanym poziomem dostępu Student. Konto posiada status nieaktywnego do czasu weryfikacji adresu e-mail przez użytkownika.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                   |
| Główny przepływ zdarzeń        | 1. Gość wybiera opcję "Zarejestruj się" na stronie głównej. <br> 2. System wyświetla formularz rejestracyjny. <br> 3. Gość wprowadza wymagane dane (imię, nazwisko, adres e-mail, login, hasło, powtórzenie hasła).<br>4. Gość zatwierdza wprowadzony formularz.<br>5. System waliduje poprawność wprowadzonych danych oraz sprawdza, czy adres e-mail oraz login nie są już zajęte.<br>6. System tworzy nowe konto w bazie danych i automatycznie przypisuje mu rolę Studenta.<br>7. System generuje unikalny adres URL (lub kod) ważny przez 24 godziny i wysyła go na podany adres e-mail w celu weryfikacji konta.<br>8. System wyświetla komunikat o pomyślnej rejestracji, informuje o konieczności aktywacji konta przez e-mail i przekierowuje do ekranu logowania. |
| Alternatywne przepływy zdarzeń | 5a. Błąd walidacji danych:<br>1. System wykrywa nieprawidłowości w danych (np. zbyt krótkie hasło, niepoprawny format e-maila, puste pola).<br>2. System wyświetla odpowiedni komunikat błędu przy błędnych polach.<br>3. Powrót do kroku 3 głównego przepływu.<br>5b. Użytkownik o podanym adresie e-mail/loginie już istnieje:<br>1. System wykrywa duplikat w bazie danych.<br>2. System wyświetla komunikat, że konto z takimi danymi już istnieje.<br>3. Powrót do kroku 3 głównego przepływu.                                                                                                                                                                                                                                                                         |
| Specjalne wymagania            | Hasło musi spełniać politykę bezpieczeństwa (np. minimum 8 znaków, wielka litera, znak specjalny).                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                          |
| Notatki i kwestie              | Role wyższe (Prowadzący, Administrator) są nadawane ręcznie przez Administratora po założeniu konta ze statusem Studenta. Brak weryfikacji e-mail w ciągu 24h skutkuje usunięciem konta                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                 |

Tabela 1.2.3 Scenariusz dla przypadku użycia MOK.2

| Pole                           | Treść                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                 |
|--------------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Oznaczenie                     | MOK.2                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                 |
| Przypadek użycia               | Uwierzytelnienie użytkownika                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                          |
| Twórca                         | Karol Dawid                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                           |
| Poziom ważności                | Krytyczny                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                             |
| Typ przypadku użycia           | Podstawowy                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            |
| Aktorzy                        | Gość                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                  |
| Krótki opis                    | Przypadek użycia pozwala niezalogowanemu użytkownikowi (Gościowi) na weryfikację swojej tożsamości w celu uzyskania dostępu do chronionych zasobów systemu. Po zalogowaniu system wczytuje przypisane role (np. domyślną rolę Studenta).                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                              |
| Warunki wstępne                | Gość nie jest zalogowany w systemie, ale posiada wcześniej utworzone, aktywne (zweryfikowane) i niezablokowane konto.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                 |
| Warunki końcowe                | Gość staje się uwierzytelnionym użytkownikiem. System nadaje mu bezpieczną sesję, a interfejs dostosowuje się do posiadanego przez niego poziomu dostępu (AccessLevel).                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                               |
| Główny przepływ zdarzeń        | 1. Gość przechodzi na stronę logowania.<br>2. System wyświetla formularz logowania.<br>3. Gość wprowadza swój login oraz hasło.<br>4. Gość zatwierdza chęć zalogowania (klika "Zaloguj")<br>5. System weryfikuje zgodność podanych danych z informacjami w bazie (sprawdza hash hasła).<br>6. System sprawdza status konta (czy nie jest zablokowane - powiązanie z MOK.4).<br>7. System uwierzytelnia użytkownika, tworzy sesję i przekierowuje go na odpowiedni widok dla zalogowanych w zależności od jego roli.<br>8. Jeżeli uwierzytelniony użytkownik posiada poziom dostępu Administratora, system automatycznie wysyła powiadomienie e-mail z informacją o nowej sesji i adresem IP na jego adres pocztowy.                                                                                                                   |
| Alternatywne przepływy zdarzeń | 5a. Błędne dane logowania (niewłaściwy login lub hasło):<br>1. System wykrywa brak zgodności danych.<br>2. System wyświetla ogólny komunikat błędu (np. "Nieprawidłowy login lub hasło").<br>3. Powrót do kroku 3 głównego przepływu.<br>5b. Przekroczenie limitu błędnych logowań (Auto-blokada):<br>1. System odnotowuje 3. z rzędu nieudaną próbę logowania na dane konto. <br>2. System automatycznie blokuje konto na 24 godziny i wysyła powiadomienie e-mail o zablokowaniu na adres użytkownika.<br>3. Przypadek użycia kończy się niepowodzeniem.<br>6a. Konto użytkownika jest zablokowane:<br>1. System wykrywa, że nałożono blokadę na to konto.<br>2. System odrzuca próbę logowania i wyświetla komunikat "Twoje konto zostało zablokowane. Skontaktuj się z administratorem".<br>3. Przypadek użycia kończy się niepowodzeniem. |
| Specjalne wymagania            | Transmisja danych z formularza musi odbywać się przez szyfrowane połączenie (HTTPS). Hasło nie może być zapisywane w systemie otwartym tekstem.                                                                                                                                                                                                                    |
| Notatki i kwestie              | System powienien opóźniać kolejne próby logowania po kilku błędach, aby zapobiec atakom brute-force.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                  |

Tabela 1.2.4 Scenariusz dla przypadku użycia MOK.3

|Pole|Treść|
|--|--|
|Oznaczenie|MOK.3|
|Przypadek użycia| |
|Twórca| |
|Poziom ważności| |
|Typ przypadku użycia| |
|Aktorzy| |
|Krótki opis| |
|Warunki wstępne| |
|Warunki końcowe| |
|Główny przepływ zdarzeń| |
|Alternatywne przepływy zdarzeń| |
|Specjalne wymagania| |
|Notatki i kwestie| |

Tabela 1.2.5 Scenariusz dla przypadku użycia MOK.4

|Pole|Treść|
|--|--|
|Oznaczenie|MOK.4|
|Przypadek użycia|Zablokuj konto użytkownika|
|Twórca|Paweł Rajnert|
|Poziom ważności|Wysoki|
|Typ przypadku użycia|Istotny|
|Aktorzy|Administrator|
|Krótki opis|Przypadek użycia pozwala na zablokowanie obecnie dostępnego konta danego użytkownika w systemie.|
|Warunki wstępne|Istnienie niezablokowanego, aktywnego konta użytkownika którego będzie dotyczył przypadek użycia.|
|Warunki końcowe|Dostęp do systemu z poziomu wybranego konta użytkownika zostanie zablokowany. Wszystkie aktywne sesje użytkownika zostaną unieważnione.|
|Główny przepływ zdarzeń|1. Administrator przechodzi do odpowiedniego formularza w systemie.<br>2. Administrator znajduje odpowiednie konto użytkownika.<br>3. Administrator wybiera opcję "Zablokuj" w ramach konta danego użytkownika.<br>4. Administrator potwierdza chęć zablokowania konta użytkownika.<br>5. Pojawia się okno z potwierdzeniem poprawnej akcji Administratora, której celem było zablokowanie konta użytkownika w systemie.<br>6. Konto zostaje zablokowane, a jego status w bazie danych oraz dostępnych formularzach zostaje zaaktualizowane.<br>7. Wszystkie aktywne sesje użytkownika, którego konto zostało zablokowane zostaną unieważnione.|
|Alternatywne przepływy zdarzeń|2a. Brak kont użytkowników w systemie - nie można wykonać kolejnych akcji.<br>7b. Użytkownik nie ma aktywnych sesji w systemie.|
|Specjalne wymagania|Zablokowanie każdego konta musi zostać odnotowane poprzez wpis w dzienniku zdarzeń.|
|Notatki i kwestie|Uprzednio zablokowane konto użytkownika może zostać odblokowane (MOK.5).|

Tabela 1.2.6 Scenariusz dla przypadku użycia MOK.5

|Pole|Treść|
|--|--|
|Oznaczenie|MOK.5|
|Przypadek użycia|Odblokuj konto użytkownika|
|Twórca|Paweł Rajnert|
|Poziom ważności|Wysoki|
|Typ przypadku użycia|Istotny|
|Aktorzy|Administrator|
|Krótki opis|Przypadek użycia pozwala na odblokowanie obecnie dostępnego w systemie zablokowanego konta danego użytkownika.|
|Warunki wstępne|Istnienie zablokowanego konta użytkownika w systemie.|
|Warunki końcowe|Dostęp do systemu dla danego użytkownika zostanie odblokowany.|
|Główny przepływ zdarzeń|1. Administrator przechodzi do odpowiedniego formularza w systemie.<br>2. Administrator znajduje odpowiednie konto użytkownika.<br>3. Administrator wybiera opcję "Odblokuj" w ramach konta danego użytkownika.<br>4. Administrator potwierdza chęć odblokowania konta użytkownika.<br>5. Pojawia się okno z potwierdzeniem poprawnej akcji Administratora, której celem było odblokowanie konta użytkownika w systemie.<br>6. Konto zostaje odblokowane, a jego status w bazie danych oraz dostępnych formularzach zostaje zaaktualizowane.|
|Alternatywne przepływy zdarzeń|2a. Brak kont użytkowników w systemie - nie można wykonać kolejnych akcji.|
|Specjalne wymagania|Odblokowanie każdego konta musi zostać odnotowane poprzez wpis w dzienniku zdarzeń.|
|Notatki i kwestie|Po odblokowaniu konta użytkownika, możliwe jest wykonywanie dowolnych akcji z uprzednio nadanym poziomem dostępu przed zablokowaniem konta.|

Tabela 1.2.7 Scenariusz dla przypadku użycia MOK.6

|Pole|Treść|
|--|--|
|Oznaczenie|MOK.6|
|Przypadek użycia| |
|Twórca| |
|Poziom ważności| |
|Typ przypadku użycia| |
|Aktorzy| |
|Krótki opis| |
|Warunki wstępne| |
|Warunki końcowe| |
|Główny przepływ zdarzeń| |
|Alternatywne przepływy zdarzeń| |
|Specjalne wymagania| |
|Notatki i kwestie| |

Tabela 1.2.8 Scenariusz dla przypadku użycia MOK.7

|Pole|Treść|
|--|--|
|Oznaczenie|MOK.7|
|Przypadek użycia| |
|Twórca| |
|Poziom ważności| |
|Typ przypadku użycia| |
|Aktorzy| |
|Krótki opis| |
|Warunki wstępne| |
|Warunki końcowe| |
|Główny przepływ zdarzeń| |
|Alternatywne przepływy zdarzeń| |
|Specjalne wymagania| |
|Notatki i kwestie| |

Tabela 1.2.9 Scenariusz dla przypadku użycia MOK.8

|Pole|Treść|
|--|--|
|Oznaczenie|MOK.8|
|Przypadek użycia| |
|Twórca| |
|Poziom ważności| |
|Typ przypadku użycia| |
|Aktorzy| |
|Krótki opis| |
|Warunki wstępne| |
|Warunki końcowe| |
|Główny przepływ zdarzeń| |
|Alternatywne przepływy zdarzeń| |
|Specjalne wymagania| |
|Notatki i kwestie| |

Tabela 1.2.10 Scenariusz dla przypadku użycia MOK.9

|Pole|Treść|
|--|--|
|Oznaczenie|MOK.9|
|Przypadek użycia| |
|Twórca| |
|Poziom ważności| |
|Typ przypadku użycia| |
|Aktorzy| |
|Krótki opis| |
|Warunki wstępne| |
|Warunki końcowe| |
|Główny przepływ zdarzeń| |
|Alternatywne przepływy zdarzeń| |
|Specjalne wymagania| |
|Notatki i kwestie| |

Tabela 1.2.11 Scenariusz dla przypadku użycia MOK.10

|Pole|Treść|
|--|--|
|Oznaczenie|MOK.10|
|Przypadek użycia|Zmień adres e-mail własnego konta|
|Twórca|Paweł Rajnert|
|Poziom ważności|Wysoki|
|Typ przypadku użycia|Istotny|
|Aktorzy|Administrator, Prowadzący, Prowadzący dodatkowy, Student|
|Krótki opis|Przypadek użycia pozwala na zmianę adresu e-mail dla wybranego z aktorów - są to użytkownicy uwierzytelnieni w systemie.|
|Warunki wstępne|Użytkownik uwierzytelnił się w systemie i jego konto jest aktywne.|
|Warunki końcowe|Adres e-mail danego użytkownika został zmieniony. Wszystkie aktywne sesje użytkownika zostaną unieważnione, kolejne uwierzytelnienie będzie wymagało nowego adresu e-mail.|
|Główny przepływ zdarzeń|1. Wybrany użytkownik uwierzytelniony przechodzi do formularza zawierającego dane o swoim koncie.<br>2. Użytkownik wybiera opcję "Zmień adres e-mail".<br>3. W nowootwartym oknie użytkownik wprowadza nowy adres e-mail.<br>4. Użytkownik potwierdza chęć zmiany adresu e-mail.<br>5. Użytkownik potwierdza swoją tożsamość poświadczeniem w postaci hasła.<br>6. Pojawia się okno z potwierdzeniem poprawnej zmiany adresu e-mail.<br>7. Adres e-mail w formularzu zawierającym dane o swoim koncie został zaaktualizowany.<br>7. Wszystkie aktywne sesje użytkownika zostaną unieważnione.|
|Alternatywne przepływy zdarzeń|4a. Wprowadzony adres e-mail już istnieje w systemie - walidacja została przeprowadzona i zmiana nie jest możliwa.<br>5b. Użytkownik wprowadził niepoprawne hasło i należy ponowić akcję.|
|Specjalne wymagania|Nowy adres e-mail musi być unikalny.|
|Notatki i kwestie|W przypadku zmiany adresu e-mail na inny unikalny, użytkownik ma możliwość do późniejszego powrótu do poprzedniego adresu e-mail po ponownym wykonaniu akcji zmiany własnego adresu e-mail.|

Tabela 1.2.12 Scenariusz dla przypadku użycia MOK.11

| Pole                           | Treść                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                       |
|--------------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Oznaczenie                     | MOK.11                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                      |
| Przypadek użycia               | Zmień adres e-mail innego konta.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            |
| Twórca                         | Jan Kozłowski                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                               |
| Poziom ważności                | Wysoki                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                      |
| Typ przypadku użycia           | Istotny                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                     |
| Aktorzy                        | Administrator                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                               |
| Krótki opis                    | Przypadek użycia pozwala Administratorowi na modyfikację adresu e-mail przypisanego do konta innego użytownika systemu, posiadającego konto.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                |
| Warunki wstępne                | 1. Administartor musi być uwierzytelniony w systemie i posiadać przypisany i aktywny poziom dostępu Admin. <br> 2. W systemie musi istnieć konto użytkownika podlegające edycji.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            |
| Warunki końcowe                | Adres e-mail przypisany do edytowanego konta użytkownika został zaktualizowany w bazie danych oraz wersja encji uległa zmianie (inkrementacja numeru wersji).                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                               |
| Główny przepływ zdarzeń        | 1. Administartor żąda edycji danych wybranego konta. <br> 2. System prezentuje Administratorowi formularz edycji konta wybranego użytkonika. <br> 3. Administrator wprowadza nowy e-mail i zatwierdza formularz. <br> 4. System waliduje dane pod względem unikalności i formatu. <br> 5. System aktualizuje e-mail w bazie danych dla wybranego konta. <br> 6. System wysyła powiadomienie o zmianie na nowy oraz stary adres e-mail użytkownika. <br> 7. System informuje Administratora o pomyślnym wprowadzeniu zmiany.                                                                                                                                                                                                 |
| Alternatywne przepływy zdarzeń | 3a. Administrator anuluje operację edycji. <ol> <li>System przerywa operację.</li> <li>System przekierowuje Administartora do widoku szczegółów konta. </li> </ol> 4a. Nowy e-mail nie spełnia wymogów walidacji. <ol> <li>System wyświetla komunikat błędu.</li> <li>Następuje powrót do kroku nr 2 głównego przepływu zdarzeń.</li> </ol> 5a. Wystąpił błąd blokady optymistycznej (dane konta zostały zmodyfikowane w tzw. międzyczasie przez inną transakcję). <ol> <li>System odrzuca żądanie zapisu.</li> <li>System synchronizuje dane konta z bazą. </li> <li>System wyświetla komunikat błędu informujący o dezaktualizacji danych.</li> <li>Przepływ powraca do kroku nr 2 głównego przepływu zdarzeń.</li> </ol> |
| Specjalne wymagania            | Nowy adres e-mail musi posiadać odpowiedni format oraz być unikalny w skali całego systemu.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                 |
| Notatki i kwestie              | Operacja zapisu jest chroniona mechanizmem blokady optymistycznej.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                          |

Tabela 1.2.13 Scenariusz dla przypadku użycia MOK.12

|Pole|Treść|
|--|--|
|Oznaczenie|MOK.12|
|Przypadek użycia| |
|Twórca| |
|Poziom ważności| |
|Typ przypadku użycia| |
|Aktorzy| |
|Krótki opis| |
|Warunki wstępne| |
|Warunki końcowe| |
|Główny przepływ zdarzeń| |
|Alternatywne przepływy zdarzeń| |
|Specjalne wymagania| |
|Notatki i kwestie| |

Tabela 1.2.14 Scenariusz dla przypadku użycia MOK.13

|Pole|Treść|
|--|--|
|Oznaczenie|MOK.13|
|Przypadek użycia| |
|Twórca| |
|Poziom ważności| |
|Typ przypadku użycia| |
|Aktorzy| |
|Krótki opis| |
|Warunki wstępne| |
|Warunki końcowe| |
|Główny przepływ zdarzeń| |
|Alternatywne przepływy zdarzeń| |
|Specjalne wymagania| |
|Notatki i kwestie| |

Tabela 1.2.15 Scenariusz dla przypadku użycia MOK.14

| Pole                           | Treść                                                                                                                                                                                                                                                                                                        |
|--------------------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Oznaczenie                     | MOK.14                                                                                                                                                                                                                                                                                                       |
| Przypadek użycia               | Wyloguj                                                                                                                                                                                                                                                                                                      |
| Twórca                         | Jan Kozłowski                                                                                                                                                                                                                                                                                                |
| Poziom ważności                | Wysoki                                                                                                                                                                                                                                                                                                       |
| Typ przypadku użycia           | Istotny                                                                                                                                                                                                                                                                                                      |
| Aktorzy                        | Administrator, Prowadzący, Prowadzący dodatkowy, Student                                                                                                                                                                                                                                                     |
| Krótki opis                    | Przypadek użycia pozwala zalogowanemu użytkownikowi na bezpieczne zakończenie pracy z aplikacją i zakończenie sesji poprzez porzucenie tokena.                                                                                                                                                               |
| Warunki wstępne                | Użytkownik musi posiadać konto w systemie i mieć aktywną sesję (poprawny token).                                                                                                                                                                                                                             |
| Warunki końcowe                | Token zostaje unieważniony oraz użytkownik traci dostęp do widoków i zasobów wymagających autoryzacji.                                                                                                                                                                                                       |
| Główny przepływ zdarzeń        | 1. Użytkownik żąda wylogowania z systemu poprzez kliknięcie odpowiedniej opcji w interfejsie. <br> 2. System unieważnia aktywny token uwierzytelniający użytkownika. <br> 3. System przekierowuje użytkownika na stronę logowania.                                                                           |
| Alternatywne przepływy zdarzeń | 1a. Token uwierzytelniający użytkownika wygasł przed świadomym wywołaniem akcji wylogowania.  <ol> <li>System odmawia wykonania akcji żądanej przez użytkownika</li> <li>System informuje użytkownika o wygaśnięciu sesji.</li> <li> Przepływ przechodzi do kroku nr 3 Głównego przepływu zdarzeń.</li></ol> |
| Specjalne wymagania            | System musi zapobiec użyciu starego tokena po wylogowaniu.                                                                                                                                                                                                                                                   |
| Notatki i kwestie              | Brak.                                                                                                                                                                                                                                                                                                        |

Tabela 1.2.16 Scenariusz dla przypadku użycia MPP.1

|Pole|Treść|
|--|--|
|Oznaczenie|MPP.1|
|Przypadek użycia| |
|Twórca| |
|Poziom ważności| |
|Typ przypadku użycia| |
|Aktorzy| |
|Krótki opis| |
|Warunki wstępne| |
|Warunki końcowe| |
|Główny przepływ zdarzeń| |
|Alternatywne przepływy zdarzeń| |
|Specjalne wymagania| |
|Notatki i kwestie| |

Tabela 1.2.17 Scenariusz dla przypadku użycia MPP.2

|Pole|Treść|
|--|--|
|Oznaczenie|MPP.2|
|Przypadek użycia| |
|Twórca| |
|Poziom ważności| |
|Typ przypadku użycia| |
|Aktorzy| |
|Krótki opis| |
|Warunki wstępne| |
|Warunki końcowe| |
|Główny przepływ zdarzeń| |
|Alternatywne przepływy zdarzeń| |
|Specjalne wymagania| |
|Notatki i kwestie| |

Tabela 1.2.18 Scenariusz dla przypadku użycia MPP.3

|Pole|Treść|
|--|--|
|Oznaczenie|MPP.3|
|Przypadek użycia| |
|Twórca| |
|Poziom ważności| |
|Typ przypadku użycia| |
|Aktorzy| |
|Krótki opis| |
|Warunki wstępne| |
|Warunki końcowe| |
|Główny przepływ zdarzeń| |
|Alternatywne przepływy zdarzeń| |
|Specjalne wymagania| |
|Notatki i kwestie| |


Tabela 1.2.19 Scenariusz dla przypadku użycia MPP.4

| Pole                           | Treść                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                       |
|--------------------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Oznaczenie                     | MPP.4                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                       |
| Przypadek użycia               | Przeglądanie jednego raportu                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                |
| Twórca                         | Karol Dawid                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                 |
| Poziom ważności                | Średni                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                      |
| Typ przypadku użycia           | Rozszerzający (względem MPP.3)                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                              |
| Aktorzy                        | Prowadzący, Prowadzący dodatkowy                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            |
| Krótki opis                    | Przypadek użycia pozwala uprawnionemu pracownikowi naukowemu na wyświetlenie szczegółowych wyników i analizy konkretnego raportu antyplagiatowego wybranego z listy.                                                                                                                                                                                                                                                                                                                                                                                        |
| Warunki wstępne                | Użytkownik jest zalogowany jako Prowadzący lub Prowadzący dodatkowy. Użytkownik znajduje się w widoku listy raportów (wywołał przypadek użycia MPP.3).                                                                                                                                                                                                                                                                                                                                                                                                      |
| Warunki końcowe                | System prezentuje szczegółowe widoki i metryki zawarte w wybranym raporcie.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                 |
| Główny przepływ zdarzeń        | 1. Użytkownik przegląda listę dostępnych raportów.<br>2. Użytkownik wybiera z listy konkretny raport i klika opcję "Pokaż szczegóły".<br>3. System lokalizuje i odczytuje fizyczny plik raportu z serwera (na podstawie ścieżki zapisanej w bazie danych).<br>4. System przetwarza zawartość pliku i weryfikuje uprawnienia użytkownika do przeglądania poszczególnych danych.<br>5. System prezentuje użytkownikowi przetworzony, czytelny raport, zawierający m.in. wskazane fragmenty kodu, wskaźniki podobieństwa oraz listę podejrzanych repozytoriów. |
| Alternatywne przepływy zdarzeń | 3a. Raport nie istnieje lub plik raportu został usunięty:<br>1. System nie może zlokalizować danych raportu pod wskazanym adresem.<br>2. System wyświetla komunikat błędu "Nie można załadować szczegółów raportu. Plik może być uszkodzony lub usunięty".<br>3. Użytkownik zostaje cofnięty do widoku listy raportów.                                                                                                                                                                                                                                      |
| Specjalne wymagania            | Raport musi czytelnie prezentować stopień podobieństwa w procentach oraz pozwalać na identyfikację konkretnych repozytoriów (Studentów) biorących udział w porównaniu.                                                                                                                                                                                                                                                                                                                                                                                      |
| Notatki i kwestie              | Prowadzący dodatkowy ma do tego widoku wyłącznie prawa do odczytu (nie może edytować ani usuwać wyników).                                                                                                                                                                                                                                                                                                                                                                                                                                                   |

Tabela 1.2.20 Scenariusz dla przypadku użycia MPP.5

|Pole|Treść|
|--|--|
|Oznaczenie|MPP.5|
|Przypadek użycia| |
|Twórca| |
|Poziom ważności| |
|Typ przypadku użycia| |
|Aktorzy| |
|Krótki opis| |
|Warunki wstępne| |
|Warunki końcowe| |
|Główny przepływ zdarzeń| |
|Alternatywne przepływy zdarzeń| |
|Specjalne wymagania| |
|Notatki i kwestie| |

Tabela 1.2.21 Scenariusz dla przypadku użycia MPP.6

|Pole|Treść|
|--|--|
|Oznaczenie|MPP.6|
|Przypadek użycia| |
|Twórca| |
|Poziom ważności| |
|Typ przypadku użycia| |
|Aktorzy| |
|Krótki opis| |
|Warunki wstępne| |
|Warunki końcowe| |
|Główny przepływ zdarzeń| |
|Alternatywne przepływy zdarzeń| |
|Specjalne wymagania| |
|Notatki i kwestie| |

Tabela 1.2.22 Scenariusz dla przypadku użycia MPP.7

|Pole|Treść|
|--|--|
|Oznaczenie|MPP.7|
|Przypadek użycia| |
|Twórca| |
|Poziom ważności| |
|Typ przypadku użycia| |
|Aktorzy| |
|Krótki opis| |
|Warunki wstępne| |
|Warunki końcowe| |
|Główny przepływ zdarzeń| |
|Alternatywne przepływy zdarzeń| |
|Specjalne wymagania| |
|Notatki i kwestie| |

Tabela 1.2.23 Scenariusz dla przypadku użycia MPP.8

|Pole|Treść|
|--|--|
|Oznaczenie|MPP.8|
|Przypadek użycia| |
|Twórca| |
|Poziom ważności| |
|Typ przypadku użycia| |
|Aktorzy| |
|Krótki opis| |
|Warunki wstępne| |
|Warunki końcowe| |
|Główny przepływ zdarzeń| |
|Alternatywne przepływy zdarzeń| |
|Specjalne wymagania| |
|Notatki i kwestie| |

Tabela 1.2.24 Scenariusz dla przypadku użycia MPP.9

| Pole                           | Treść                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                       |
|--------------------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Oznaczenie                     | MPP.9                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                       |
| Przypadek użycia               | Usuwanie przedmiotu                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                         |
| Twórca                         | Karol Dawid                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                 |
| Poziom ważności                | Wysoki                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                      |
| Typ przypadku użycia           | Rozszerzający (względem MPP.6)                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                              |
| Aktorzy                        | Prowadzący                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                  |
| Krótki opis                    | Przypadek użycia umożliwia Prowadzącemu trwałe usunięcie przedmiotu z systemu. Proces obejmuje usunięcie rekordów z bazy danych oraz fizyczne skasowanie wszystkich powiązanych plików raportów z dysku serwera.                                                                                                                                                                                                                                                                                                                                                                                                                                            |
| Warunki wstępne                | Użytkownik jest zalogowany jako Prowadzący. Użytkownik posiada uprawnienia do modyfikacji danego przedmiotu (hasEditPermissions). Użytkownik znajduje się w widoku listy przedmiotów (MPP.6).                                                                                                                                                                                                                                                                                                                                                                                                                                                               |
| Warunki końcowe                | Przedmiot oraz jego elementy zależne zostają usunięte z bazy danych. Wszystkie pliki raportów skojarzone z przedmiotem zostają trwale usunięte z pamięci masowej serwera. Wykonanie operacji zostaje trwale odnotowane w systemowym dzienniku zdarzeń (logach audytowych) wraz ze szczegółami akcji i loginem Prowadzącego.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                  |
| Główny przepływ zdarzeń        | 1. Prowadzący wybiera z listy przedmiot, który chce usunąć, i klika przycisk "Usuń".<br>2. System wyświetla okno z ostrzeżeniem, że operacja jest nieodwracalna, i prosi o potwierdzenie decyzji (pojawienie się przycisków "Potwierdź" oraz "Anuluj").<br>3. Prowadzący potwierdza chęć trwałego usunięcia.<br>4. System weryfikuje uprawnienia Prowadzącego do usunięcia przedmiotu.<br>5. System usuwa kaskadowo dane z bazy (przedmiot, repozytoria, reguły, harmonogramy) oraz wyszukuje i trwale kasuje fizyczne pliki raportów z serwera (na podstawie ścieżek pathToReport).<br>6. System odświeża listę przedmiotów i wyświetla komunikat o sukcesie. |
| Alternatywne przepływy zdarzeń | 3a. Prowadzący anuluje akcję:<br>1. Prowadzący klika "Anuluj" w oknie potwierdzenia.<br>2. System zamyka okno bez wprowadzania jakichkolwiek zmian w bazie.<br>4a. Brak wystarczających uprawnień:<br>1. System wykrywa, że Prowadzący nie jest głównym zarządzającym tym przedmiotem.<br>2. System odrzuca żądanie, blokuje usunięcie i wyświetla stosowny komunikat.<br>4b. Błąd spójności danych (Blokada optymistyczna):<br>1. System wykrywa, że pole wersji (version) usuwanego przedmiotu w bazie różni się od wersji przekazanej przez interfejs (dane zmodyfikowane w innej sesji).<br>2. System odwołuje transakcję (rollback) i odrzuca żądanie.<br>3. System wyświetla komunikat: "Dane zostały zmodyfikowane przez innego użytkownika. Odśwież widok i spróbuj ponownie."                                                                                                                                                                                                                                                                                  |
| Specjalne wymagania            | System musi zagwarantować spójność: pliki powinny zostać usunięte tylko wtedy, gdy operacja na bazie danych zakończy się sukcesem (transakcja).                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                             |
| Notatki i kwestie              | Fizyczne usuwanie plików jest krytyczne dla zachowania wolnego miejsca na serwerze.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                         |

Tabela 1.2.25 Scenariusz dla przypadku użycia MPP.10

|Pole|Treść|
|--|--|
|Oznaczenie|MPP.10|
|Przypadek użycia| |
|Twórca| |
|Poziom ważności| |
|Typ przypadku użycia| |
|Aktorzy| |
|Krótki opis| |
|Warunki wstępne| |
|Warunki końcowe| |
|Główny przepływ zdarzeń| |
|Alternatywne przepływy zdarzeń| |
|Specjalne wymagania| |
|Notatki i kwestie| |

Tabela 1.2.26 Scenariusz dla przypadku użycia MPP.11

|Pole|Treść|
|--|--|
|Oznaczenie|MPP.11|
|Przypadek użycia| |
|Twórca| |
|Poziom ważności| |
|Typ przypadku użycia| |
|Aktorzy| |
|Krótki opis| |
|Warunki wstępne| |
|Warunki końcowe| |
|Główny przepływ zdarzeń| |
|Alternatywne przepływy zdarzeń| |
|Specjalne wymagania| |
|Notatki i kwestie| |

Tabela 1.2.27 Scenariusz dla przypadku użycia MPP.12

|Pole|Treść|
|--|--|
|Oznaczenie|MPP.12|
|Przypadek użycia| |
|Twórca| |
|Poziom ważności| |
|Typ przypadku użycia| |
|Aktorzy| |
|Krótki opis| |
|Warunki wstępne| |
|Warunki końcowe| |
|Główny przepływ zdarzeń| |
|Alternatywne przepływy zdarzeń| |
|Specjalne wymagania| |
|Notatki i kwestie| |

Tabela 1.2.28 Scenariusz dla przypadku użycia MPP.13

| Pole                           | Treść                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                           |
|--------------------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Oznaczenie                     | MPP.13                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                          |
| Przypadek użycia               | Usuwanie reguł                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                  |
| Twórca                         | Karol Dawid                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                     |
| Poziom ważności                | Średni                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                          |
| Typ przypadku użycia           | Rozszerzający (względem MPP.10)                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                 |
| Aktorzy                        | Prowadzący                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                      |
| Krótki opis                    | Przypadek użycia pozwala Prowadzącemu na usunięcie całego presetu reguł (zestawu zasad antyplagiatowych) powiązanego z przedmiotem. Ponieważ harmonogramy sprawdzania opierają się na danym presecie, zostaną one usunięte wraz z nim.                                                                                                                                                                                                                                                                                                                                                                                                          |
| Warunki wstępne                | Użytkownik jest zalogowany jako Prowadzący z prawami edycji przedmiotu. Użytkownik przegląda listę zdefiniowanych reguł dla przedmiotu (wywołał przypadek użycia MPP.10).                                                                                                                                                                                                                                                                                                                                                                                                                                                                       |
| Warunki końcowe                | Wskazany zestaw reguł oraz wszystkie opierające się na nim zaplanowane harmonogramy (Schedule) zostają trwale usunięte z bazy danych. Wykonanie operacji zostaje trwale odnotowane w systemowym dzienniku zdarzeń wraz ze szczegółami akcji i loginem Prowadzącego.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                          |
| Główny przepływ zdarzeń        | 1. Prowadzący klika przycisk usuwania przy wybranym presecie reguł na liście.<br>2. System weryfikuje, czy z usuwanym zestawem reguł powiązane są aktywne harmonogramy (Schedule).<br>3. System wyświetla okno z prośbą o potwierdzenie akcji, wyraźnie ostrzegając: "Czy na pewno chcesz usunąć ten zestaw reguł? Spowoduje to anulowanie i usunięcie wszystkich powiązanych zaplanowanych sprawdzeń".<br>4. Prowadzący potwierdza decyzję o usunięciu.<br>5. System usuwa kaskadowo z bazy danych rekord presetu reguł oraz przypisaną do niego listę harmonogramów.<br>6. System wyświetla komunikat o pomyślnym usunięciu i odświeża listę. |
| Alternatywne przepływy zdarzeń | 4a. Prowadzący rezygnuje z usunięcia po zobaczeniu ostrzeżenia:<br>1. Prowadzący wybiera opcję "Anuluj" w oknie potwierdzenia.<br>2. System zamyka okno komunikatu i przerywa proces usuwania.<br>3. Żadne zmiany nie zostają wprowadzone w bazie danych.<br>4b. Błąd spójności danych (Blokada optymistyczna):<br>1. System wykrywa, że pole wersji (version) usuwanego zestawu reguł w bazie różni się od wersji przekazanej przez interfejs.<br>2. System odwołuje transakcję (rollback) i przerywa usuwanie.<br>3. System wyświetla komunikat: "Dane zostały zmodyfikowane przez innego użytkownika. Odśwież widok i spróbuj ponownie."                                                                                                                                                                                                                                                                                                                                                                                   |
| Specjalne wymagania            | Baza danych musi zapewniać, że usunięcie obiektu nadrzędnego jakim jest zestaw reguł wymusza kaskadowe usunięcie powiązanych z nim obiektów podrzędnych (Schedule).                                                                                                                                                                                                                                                                                                                                                                                                                                                                             |
| Notatki i kwestie              |                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                 |

Tabela 1.2.29 Scenariusz dla przypadku użycia MPP.14

|Pole|Treść|
|--|--|
|Oznaczenie|MPP.14|
|Przypadek użycia| |
|Twórca| |
|Poziom ważności| |
|Typ przypadku użycia| |
|Aktorzy| |
|Krótki opis| |
|Warunki wstępne| |
|Warunki końcowe| |
|Główny przepływ zdarzeń| |
|Alternatywne przepływy zdarzeń| |
|Specjalne wymagania| |
|Notatki i kwestie| |

Tabela 1.2.30 Scenariusz dla przypadku użycia MPP.15

|Pole|Treść|
|--|--|
|Oznaczenie|MPP.15|
|Przypadek użycia| |
|Twórca| |
|Poziom ważności| |
|Typ przypadku użycia| |
|Aktorzy| |
|Krótki opis| |
|Warunki wstępne| |
|Warunki końcowe| |
|Główny przepływ zdarzeń| |
|Alternatywne przepływy zdarzeń| |
|Specjalne wymagania| |
|Notatki i kwestie| |

Tabela 1.2.31 Scenariusz dla przypadku użycia MPP.16

|Pole|Treść|
|--|--|
|Oznaczenie|MPP.16|
|Przypadek użycia|Edycja harmonogramu|
|Twórca|Paweł Rajnert|
|Poziom ważności|Średni|
|Typ przypadku użycia|Przeciętnie istotny|
|Aktorzy|Prowadzący|
|Krótki opis|Przypadek użycia pozwala na edycję obecnie istniejącego harmongoramu do generowania raportów dla danego przedmiotu.|
|Warunki wstępne|Istnieje przedmiot i Prowadzący jest jego właścicielem oraz został utworzony harmonogram dla danego przedmiotu.|
|Warunki końcowe|Harmonogram został zaaktualizowany.|
|Główny przepływ zdarzeń|1. Prowadzący przechodzi do danego przedmiotu, którego jest właścicielem i za razem kierownikiem.<br>2. Prowadzący wybiera opcję podglądu harmonogramu.<br>3. Wyświetla się obecnie utworzony harmonogram.<br>4. Prowadzący dokonuje żądanej edycji harmonogramu - może zmienić datę skanowania repozytoriów oraz tag zadania.<br>5. Prowadzący potwierdza zmiany w harmonogramie.<br>6. Pojawia się okno potwierdzające zmianę parametrów harmonogramu.<br>7. Zaaktualizowane dane są widoczne w formularzu z przeglądem harmonogramów.|
|Alternatywne przepływy zdarzeń|4a. Prowadzący wprowadza nieistniejący tag lub niepoprawną datę (np. z przeszłości).<br>5a. Edycja harmonogramu nie zostanie zaakceptowana po walidacji danych.|
|Specjalne wymagania|Brak|
|Notatki i kwestie|Prowadzący edytuje harmonogram najczęściej przed zbliżającym się kolejnym zadaniem z uwagi na konieczność zmiany tagu zadania.|

Tabela 1.2.32 Scenariusz dla przypadku użycia MPP.17

|Pole|Treść|
|--|--|
|Oznaczenie|MPP.17|
|Przypadek użycia| |
|Twórca| |
|Poziom ważności| |
|Typ przypadku użycia| |
|Aktorzy| |
|Krótki opis| |
|Warunki wstępne| |
|Warunki końcowe| |
|Główny przepływ zdarzeń| |
|Alternatywne przepływy zdarzeń| |
|Specjalne wymagania| |
|Notatki i kwestie| |

Tabela 1.2.33 Scenariusz dla przypadku użycia MPP.18

|Pole|Treść|
|--|--|
|Oznaczenie|MPP.18|
|Przypadek użycia| |
|Twórca| |
|Poziom ważności| |
|Typ przypadku użycia| |
|Aktorzy| |
|Krótki opis| |
|Warunki wstępne| |
|Warunki końcowe| |
|Główny przepływ zdarzeń| |
|Alternatywne przepływy zdarzeń| |
|Specjalne wymagania| |
|Notatki i kwestie| |

Tabela 1.2.34 Scenariusz dla przypadku użycia MPP.19

|Pole|Treść|
|--|--|
|Oznaczenie|MPP.19|
|Przypadek użycia|Przeglądanie kont użytkowników|
|Twórca|Paweł Rajnert|
|Poziom ważności|Wysoki|
|Typ przypadku użycia|Niezbędny|
|Aktorzy|Prowadzący, Prowadzący dodatkowy|
|Krótki opis|Wszyscy prowadzący mają możliwość przeglądania kont wybranych użytkowników (Studentów) w celu zobaczenia ich statystyk względem przedmiotów.|
|Warunki wstępne|Użytkownik (Prowadzący/Prowadzący dodatkowy) musi się uwierzytelnić na swoje konto oraz dostępne musi być minimum jedno konto Studenta w celu uzyskania o nim informacji.|
|Warunki końcowe|Każdy z prowadzących ma możliwość przeglądania wybranego konta użytkownika (Studenta) - wyświetlone będą jego statystki.|
|Główny przepływ zdarzeń|1. Prowadzący przechodzi do formularza z dostępnymi kontami w systemie.<br>2. Prowadzący wybiera konto Studenta, którego statystyki chce wyświetlić.<br>3. Po wyborze Studenta, wyświetlane są jego statystyki.|
|Alternatywne przepływy zdarzeń|3a. Prowadzący może zobaczyć szczegóły wybranej statystyki poprzez naciśnięcie pożądanej opcji na ekranie.|
|Specjalne wymagania|Brak|
|Notatki i kwestie|W przypadku istnienia konta Studenta, jednocześnie z brakiem istnienia jakichkolwiek statystyk, przegląd dalej jest dostępny.|

Tabela 1.2.35 Scenariusz dla przypadku użycia MPP.20

|Pole|Treść|
|--|--|
|Oznaczenie|MPP.20|
|Przypadek użycia|Sprawdzenie swojego kodu|
|Twórca|Paweł Rajnert|
|Poziom ważności|Wysoki|
|Typ przypadku użycia|Niezbędny|
|Aktorzy|Student|
|Krótki opis|Student w przypadku udostępnienia tej opcji przez Prowadzącego ma możliwość sprawdzenia swojego kodu względem występowania ewentualnego plagiatu w danym przedmiocie.|
|Warunki wstępne|Student należy do danego przedmiotu oraz istnieje zbiór danych (kod innych Studentów na repozytoriach) do porównania względem ewentualnego plagiatu.|
|Warunki końcowe|Kod Studenta zostanie sprawdzony względem ewentualnego plagiatu w porównaniu do istniejących na repozytoriach rozwiązań innych studentów (również w przypadku innych skanów testowych).|
|Główny przepływ zdarzeń|1. Student przechodzi do formualrza z opcją sprawdzania swojego kodu.<br>2. Student sprawdza ilość dostępnych skanowań kodu (w przypadku udostępnienia przez Prowadzącego możliwości sprawdzenia swojego rozwiązania).<br>3. Student w przypadku posiadania odpowiedniej liczby dostępnych skanowań kodu wybiera zadanie którego skan ma dotyczyć.<br>4. Następnie zostanie wskazany tag testowy, z którym należy umieścić na repozytorium kod do sprawdzenia.<br>5. Student załącza swój kod z odpowiednim tagiem.<br>6. Sprawdzenie kodu studenta rozpoczyna się wraz ze stosowną informacją o tym działaniu.<br>7. Wynik skanowania wyświetla się na ekranie, w zależności od ustawionego zbioru reguł przez prowadzącego, rezultat jest odpowiednio ograniczony np. jedynie do wskaźnika podobieństwa.|
|Alternatywne przepływy zdarzeń|2a. Przedmiot do którego należy student nie udostępnia opcji sprawdzania swojego kodu - żadne sprawdzenie nie zostanie wykonane z uwagi na ograniczony dostęp do tej opcji.<br>3b. Student wybrał złe zadanie w danym przedmiocie.<br>5b. Student oznacza zadanie nieodpowiednim tagiem.<br>6b. Skanowanie kodu zostanie uruchomione - Student traci jedno z użyć tej opcji.<br>7b. Uzyskany wynik skanowania kodu Studenta jest niemiarodajny i zaburza przyszły zbiór testowy kodu źródłowego na repozytoriach.|
|Specjalne wymagania|Istnieje kod utworzony przez innych Studentów i udostępniony na repozytoriach, do którego Student może porównać swoją pracę. Kod musi zostać oznaczony odpowiednim tagiem testowym.|
|Notatki i kwestie|Student ma możliwość sprawdzenia swojego kodu bez konsekwencji określoną przez Prowadzącego poprzez zestaw reguł ilość.|

Tabela 1.2.36 Scenariusz dla przypadku użycia MPP.21

| Pole                           | Treść                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                          |
|--------------------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Oznaczenie                     | MPP.21                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                         |
| Przypadek użycia               | Przeglądanie swoich raportów                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                   |
| Twórca                         | Karol Dawid                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                    |
| Poziom ważności                | Wysoki                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                         |
| Typ przypadku użycia           | Podstawowy                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                     |
| Aktorzy                        | Student                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                        |
| Krótki opis                    | Przypadek użycia pozwala Studentowi na wgląd w wyniki działania systemu antyplagiatowego wygenerowane dla jego własnych repozytoriów kodu.                                                                                                                                                                                                                                                                                                                                                                                                                                                     |
| Warunki wstępne                | Użytkownik jest zalogowany jako Student. System wygenerował już wcześniej raport antyplagiatowy obejmujący repozytorium Studenta.                                                                                                                                                                                                                                                                                                                                                                                                                                                              |
| Warunki końcowe                | Student wyświetla treść swojego raportu.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                       |
| Główny przepływ zdarzeń        | 1. Student przechodzi do zakładki "Moje Raporty" w swoim panelu.<br>2. System weryfikuje przypisanie repozytoriów do konta Studenta.<br>3. System pobiera listę dostępnych raportów dla tych repozytoriów, weryfikując uprawnienia widoczności (parametr studentRaportLevel).<br>4. System prezentuje Studentowi listę jego raportów.<br>5. Student klika w wybrany raport.<br>6. System odczytuje fizyczny plik wybranego raportu z serwera.<br>7. System filtruje zawartość pliku na podstawie polityki (studentRaportLevel), usuwając kody źródłowe i dane osobowe innych studentów.<br>8. System prezentuje Studentowi bezpieczny, okrojony widok raportu, pokazujący wyłącznie dopasowania dotyczące jego własnego kodu. |
| Alternatywne przepływy zdarzeń | 3a. Brak dostępnych raportów:<br>1. System stwierdza brak powiązanych raportów lub Prowadzący zablokował widoczność raportów dla studentów (studentRaportLevel).<br>2. System wyświetla komunikat: "Obecnie nie masz dostępnych raportów do wglądu".<br>3. Koniec przypadku użycia.                                                                                                                                                                                                                                                                                                            |
| Specjalne wymagania            | System musi bezwzględnie filtrować dane i chronić prywatność innych studentów (Student może widzieć tylko ewentualne podobieństwa swojego kodu, ale nie może widzieć pełnych danych innych studentów w raporcie oraz ich kodu)                                                                                                                                                                                                                                                                                                                                                                 |
| Notatki i kwestie              | Poziom szczegółowości widoku powiązany jest z polem studentRaportLevel z klasy Rule i to ten parametr dyktuje, ile informacji udostępnić.                                                                                                                                                                                                                                                                                                                                                                                                                                                      |

Tabela 1.2.37 Scenariusz dla przypadku użycia MPP.22

|Pole|Treść|
|--|--|
|Oznaczenie|MPP.22|
|Przypadek użycia| |
|Twórca| |
|Poziom ważności| |
|Typ przypadku użycia| |
|Aktorzy| |
|Krótki opis| |
|Warunki wstępne| |
|Warunki końcowe| |
|Główny przepływ zdarzeń| |
|Alternatywne przepływy zdarzeń| |
|Specjalne wymagania| |
|Notatki i kwestie| |

Tabela 1.2.38 Scenariusz dla przypadku użycia MPP.23

|Pole|Treść|
|--|--|
|Oznaczenie|MPP.23|
|Przypadek użycia|Obliczenie zagregowanych wartości|
|Twórca|Paweł Rajnert|
|Poziom ważności|Średni|
|Typ przypadku użycia|Istotny|
|Aktorzy|System|
|Krótki opis|Przypadek użycia polega na obliczeniu statystyk na podstawie posiadanych danych w systemie np. średni wskaźnik podobieństwa w całej historii przedmiotu, dla danego zadania.|
|Warunki wstępne|Istnieją dane w bazie danych, na podstawie których można dokonać ich agregacji.|
|Warunki końcowe|Zagregowane dane zostaną zwrócone dla Prowadzącego w oknie przeglądania danego przedmiotu.|
|Główny przepływ zdarzeń|1. Agregacja danych zostaje wywołana wraz z wywołaniem przypadku użycia "Przeglądanie przedmiotu" (MPP.6).<br>2. Pobierane są dane o przedmiocie, między innymi o współczynniku podobnieństwa.<br>3. Dane są agregowane i po tym działaniu tworzone są: histogram z rozkładem współczynnika podobieństwa, statystyki współczynnika podobieństwa względem poprzednich zadań, najczęściej występujący fragment kodu.<br>4. Dane są wyświetlane w odpowiednim miejscu formularza do przeglądania przedmiotu.|
|Alternatywne przepływy zdarzeń|2a. Przedmiot obecnie nie posiada danych do agregacji, zwrócona zostanie informacja "Brak danych do obliczenia statystyk" zamiast żądanych statystyk.|
|Specjalne wymagania|Brak|
|Notatki i kwestie|W przypadku braku danych, pojawi się informacja o braku agregacji danych zamiast odpowiednich statystyk.|

Tabela 1.2.39 Scenariusz dla przypadku użycia MPP.24

|Pole|Treść|
|--|--|
|Oznaczenie|MPP.24|
|Przypadek użycia| |
|Twórca| |
|Poziom ważności| |
|Typ przypadku użycia| |
|Aktorzy| |
|Krótki opis| |
|Warunki wstępne| |
|Warunki końcowe| |
|Główny przepływ zdarzeń| |
|Alternatywne przepływy zdarzeń| |
|Specjalne wymagania| |
|Notatki i kwestie| |

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
- MPP.5 - Usuwanie raportów
- MPP.6 - Przeglądanie przedmiotów
- MPP.7 - Tworzenie przedmiotu
- MPP.8 - Edycja przedmiotu
- MPP.9 - Usuwanie przedmiotu
- MPP.10 - Przeglądanie reguł
- MPP.11 - Tworzenie reguł
- MPP.12 - Edycja reguł
- MPP.13 - Usuwanie reguł
- MPP.14 - Przeglądanie harmonogramu
- MPP.15 - Tworzenie harmonogramu
- MPP.16 - Edycja harmonogramu
- MPP.17 - Usuwanie harmonogramu
- MPP.18 - Przeglądanie repozytoriów
- MPP.19 - Przeglądanie kont użytkowników
- MPP.20 - Sprawdzenie swojego kodu
- MPP.21 - Przeglądanie swoich raportów
- MPP.22 - Wygeneruj raport według harmonogramu
- MPP.23 - Obliczenie zagregowanych wartości
- MPP.24 - Zmiana kierownika przedmiotu