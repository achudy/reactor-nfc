# Część serwerowa rozwiązania
Prosta aplikacja udostępniająca stronę www oraz endpointy, z którymi komunikują się telefony.
## Przygotowanie środowiska
```
npm i firebase-admin
npm i express
npm i body-parser
npm i bcrypt
npm i axios
```
Zainstalowanie paczki `node`.

## Uruchomienie aplikacji
```
node index.js
```

## Otwarte porty
Aplikacja otwiera porty do komunikacji między dwoma instancjami aplikacji oraz do serwowania strony www.

### Port 3000
Endpointy
#### POST /test
Służy do odbierania POST z jednego urządzenia i wysyłania zapytania do drugiej części w celu potwierdzenia aktywacji.
### POST /token 
Służy do zakomunikowania z telefonu do serwera jego unikalnego tokenu.

### Port 3001
#### POST /test
Służy do odbierania weryfikacji zapytań.
#### POST /firebase/notification1-3
Służy do manualnego wysyłania powiadomień na urządzenia.
#### GET /
Zwraca indeks strony www.
#### GET /register
Zwraca stronę służącą do rejestracji użytkowników.
#### GET /login
Zwraca stronę logowania użytkowników.
W przypadku prawidłego zalogowania umożliwia wysyłanie powiadomień przez kliknięcie przycisku.
