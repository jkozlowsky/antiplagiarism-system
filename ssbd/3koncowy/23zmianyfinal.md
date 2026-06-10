
## 23 Zmiany - projekt wstępny i szczegółowy

W rozdziale tym należy wymienić zmiany wprowadzone do sprawozdania wstępnego lub szczegółowego, zwięźle je charakteryzując.

### Rozdział 2

- MPP.14 (Przeglądanie harmonogramu): Zmodyfikowano sposób interpretacji i prezentacji stanu harmonogramu. 
Zrezygnowano z opierania logiki o jedno pole ze słownikowym statusem (np. ZAPLANOWANY, ZREALIZOWANY) na rzecz 
wyliczania faktycznego stanu na podstawie flag is_executed oraz has_failed.
- MPP.15 (Tworzenie harmonogramu): Dodano do formularza i głównego przepływu nową regułę biznesową wymagającą podania 
nieprzekraczalnego terminu modyfikacji tworzonego harmonogramu (modifiable_until). Usunięto również krok polegający na 
jawnym ustawianiu statusu na ZAPLANOWANY na rzecz ustawiania domyślnych wartości nowych flag.
- MPP.22 (Wygeneruj raport według harmonogramu): Doprecyzowano aktora systemowego i przepływ zdarzeń dla wyzwalania 
zadań. Proces nie jest już aktywowany bliżej nieokreślonym mechanizmem, lecz konkretnym komponentem wywoływanym 
cyklicznie co minutę. Przebudowano również przepływ zarządzania stanem przetwarzania (usunięto zmianę pomiędzy 
statusami URUCHOMIONY, ZREALIZOWANY, ANULOWANY na rzecz pojedynczej aktualizacji flagi oznaczającej sukces lub błąd). 