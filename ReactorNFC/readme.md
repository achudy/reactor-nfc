# Moblina aplikacja wykorzystująca NFC do autoryzacji

## NFC
Autoryzacja polega na zbliżeniu dwóch telefonów z NFC.
Wykorzystywane są wiadomości Ndef, w których przesyłana jest wiadomość tekstowa.
Autoryzacja jest pomyślna kiedy wiadomość zostanie poprawnie odebrana. 
Następuje wtedy komunikacja z serwerem, który następnie przetwarza otrzymane zapytanie HTTP.

## Powiadomienia
Do wysyłania powiadomień wykorzystany został Firebase - usługa firmy Google.
Przez rejestrację urządzenia w Firebase udostępniony jest token, którym następnie należy komunikować się z urządzeniem i wysyłać powiadomienia.
