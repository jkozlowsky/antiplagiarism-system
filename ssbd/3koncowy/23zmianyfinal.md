
## 23 Zmiany - projekt wstępny i szczegółowy

W rozdziale tym należy wymienić zmiany wprowadzone do sprawozdania wstępnego lub szczegółowego, zwięźle je charakteryzując.

### Zamiany w projekcie

1. Rozbito przypadek `MPP 6 Przeglądanie przedmiotów` na `MPP 6 Pobierz Liste przedmitów` oraz `MPP 25 - Przeglądaj szczegóły przedmiotu`.


2. Dodano nowy przypadek użycia `MPP 26 - Zarchiwizuj przedmiot`.


3. Dodano nowe role w systemie, zamiast `Prowadzący dodatkowy`, obecnie używamy Właściciela (Owner), Edytora (Editor) oraz Przeglądający (Viewer). Są to role przypisywane tylko do prowadzących i tylko w ramach danego przedmiotu. Właściciel może wprowadzać wszystkie zmiany do przedmiotu, Edytor również może wprowadzać zmiany do przedmiotu, natomiast nie może edytować kadry, usuwać przedmiotu oraz go archiwizować, nauczyciel przeglądający nie może wykonywać żadnych akcji w ramach danego przedmiotu.


4. MPP.14 (Przeglądanie harmonogramu): Zmodyfikowano sposób interpretacji i prezentacji stanu harmonogramu. 
Zrezygnowano z opierania logiki o jedno pole ze słownikowym statusem (np. ZAPLANOWANY, ZREALIZOWANY) na rzecz 
wyliczania faktycznego stanu na podstawie flag is_executed oraz has_failed.


5. MPP.15 (Tworzenie harmonogramu): Dodano do formularza i głównego przepływu nową regułę biznesową wymagającą podania 
nieprzekraczalnego terminu modyfikacji tworzonego harmonogramu (modifiable_until). Usunięto również krok polegający na 
jawnym ustawianiu statusu na ZAPLANOWANY na rzecz ustawiania domyślnych wartości nowych flag.


6. MPP.22 (Wygeneruj raport według harmonogramu): Doprecyzowano aktora systemowego i przepływ zdarzeń dla wyzwalania 
zadań. Proces nie jest już aktywowany bliżej nieokreślonym mechanizmem, lecz konkretnym komponentem wywoływanym 
cyklicznie co minutę. Przebudowano również przepływ zarządzania stanem przetwarzania (usunięto zmianę pomiędzy 
statusami URUCHOMIONY, ZREALIZOWANY, ANULOWANY na rzecz pojedynczej aktualizacji flagi oznaczającej sukces lub błąd). 


7. Aktualizacja diagramu UML klas encji 
