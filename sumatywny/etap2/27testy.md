
## 2.7. Testowanie integracyjne
Należy przedstawić główne założenia przyjętej techniki testowania integracyjnego:
* zastosowane dedykowane narzędzie do definiowania i prowadzenia testów
* sposób wdrażania aplikacji w celu przeprowadzenia testów, o ile testy są prowadzone na infrastrukturze innej niż dostarczona zespołowi
* sposób zapewnienia powtarzalnego zestawu danych początkowych dla testów, o ile takie dane są stosowane

Należy przedstawić raport podsumowujący pokrycie aplikacji warstwy logiki testami, wygenerowany za pomocą dedykowanego narzędzia (JaCoCo lub podobne).

Należy także przedstawić pełen zestaw testów pozytywnych oraz negatywnych dla przypadku użycia zarejestrowania nowego konta użytkownika przez użytkownika
nieuwierzytelnionego. Wystarczającą formą jest przytoczenie kodu źródłowego / opisu definicyjnego testu z krótkim komentarzem wskazującym cel realizacji testu.

_Przypomnienie: Wymagania projektowe definiują wykluczenia z obowiązku prowadzenia testów jednostkowych dla określonych przypadków użycia, w szczególności
dotyczy to realizacji przypadku zarejestrowania nowego konta._