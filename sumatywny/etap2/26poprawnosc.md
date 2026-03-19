
## 2.6. Poprawność danych i komunikaty błędów
Wskazać format wykorzystywany do przesyłania danych pomiędzy klientem a kontekstem serwera. Zaprezentować przykład/przykłady komunikacji ilustrujące
zastosowanie tego formatu w obu kierunkach przesyłania danych.

Na przykładzie danych opisujących konto użytkownika w systemie przedstawić zasady poprawności tych danych:
* wewnętrznej - składniowej,
* zewnętrznej - wynikającej z koegzystencji w całym zbiorze danych (np. unikalność wartości atrybutu w zbiorze)

Na tym samym przykładzie przedstawić zastosowane mechanizmy weryfikacji / zapewnienia poprawności w obu przypadkach.

Przedstawić zunifikowany sposób dostarczania przez aplikację warstwy logiki do klienta informacji o wyniku przetwarzania żądania.

Zaprezentować w formie tabeli lub listy przykłady komunikatów związanych z następującymi wynikami przetwarzania:
* nieprawidłowy format przesyłanych danych
* niespełniona zasada poprawności wewnętrznej
* niespełniona zasada poprawności zewnętrznej
* niedostarczenie poświadczeń wymaganych do autoryzacji
* dostarczenie poświadczeń nie pozwalających na autoryzację
* dostarczenie poświadczeń wymaganych do autoryzacji, ale zmanipulowanych poza kontekstem serwera
* nieprawidłowy URL żądania
* nie przewidziany wcześniej błąd

