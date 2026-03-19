
## 9 Model bezpieczeństwa komponentów aplikacji sieciowej 5 pkt


Wymagane przedstawienie deklaratywnego modelu bezpieczeństwa (kontroli dostępu) metod komponentów aplikacji sieciowej wraz z konfiguracją.

Forma - do wyboru:
* naniesienie odpowiednich wartości na diagramy klas komponentów aplikacji sieciowej, lub
* tabela, w której każdy wiersz reprezentuje pojedynczy przypadek użycia, i zawiera nazwy wszystkich metod (wraz z nazwami komponentów np. mokEndpoint.create()) wykonywanych w ramach realizacji danego przypadku użycia, wraz ze stosownymi adnotacjami

Ponadto należy zaprezentować mapowanie grup oraz tożsamości użytkowników na role aplikacji. Informacje te należy zaprezentować w formie tabeli lub wycinka z odpowiedniego deskryptora aplikacji.

Należy zaprezentować wszystkie elementy niezbędne dla funkcjonowania mechanizmu uwierzytelniania użytkowników (implementacja i konfiguracja). Należy podać czas trwania sesji bezczynnego uwierzytelnionego użytkownika, po upływie którego nastąpi automatyczne zakończenie sesji użytkownika (wylogowanie). W przypadku
zastosowania JWT należy przedstawić kiedy tworzony jest żeton, co zawiera i jaki ma okres ważności. 

Należy też przedstawić rozwiązanie zapewniające wyliczanie i składowanie haseł przypisanych do kont użytkowników w bazie danych w postaci niejawnej (skrótu) wraz z przykładem jego wykorzystania.

Należy też zaprezentować rozwiązanie zapewniające zgłaszanie do dziennika zdarzeń, w którym dla każdego wywołania metody biznesowej każdego z komponentów aplikacji sieciowej odnotowane będą co najmniej:
* tożsamość użytkownika, na rzecz którego wykonywany jest przypadek użycia,
* nazwa wykonywanej metody i klasy, której jest ona członkiem,
* wartości (reprezentacja tekstowa) parametrów i wartości zwracanej, o ile takowe istnieją,
* w przypadku zakończenia metody przez zgłoszenie wyjątku - nazwa i przyczyna (cause) wyjątku.

W ramach prezentacji rozwiązania należy zacytować kod klas i metod realizujących funkcjonalność dziennika zdarzeń oraz fragment kodu wybranego (przykładowego) komponentu obrazujący zastosowanie rozwiązania.

_Realizacja: Wszystkie metody biznesowe wszystkich komponentów (we wszystkich modułach funkcjonalnych aplikacji) muszą być opatrzone stosownymi adnotacjami konfigurującymi deklaratywny model bezpieczeństwa._

_Przedstawione rozwiązanie tworzenia dziennika zdarzeń musi być zaimplementowane i zastosowane do wszystkich klas / metod komponentów_