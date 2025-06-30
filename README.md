# Posts

## Opis Projektu

Testowa aplikacja CNApp oparta na protokole SSMMP. Interfejsem użytkownika jest klient wiersza poleceń (CLI), który komunikuje się z systemem mikrousług poprzez API Gateway. Żądania i odpowiedzi przesyłane są w formie obiektów klasy String, a cała architektura jest bezstanowa.

## Rola Komponentu

Ta mikrousługa umożliwia przechowywanie postów użytkowników. Odbiera żądania typu `send_post_request` od API Gateway, weryfikuje istnienie użytkownika, a następnie zapisuje treść posta w bazie danych, przypisując go do odpowiedniego użytkownika.

## Konfiguracja

Ten komponent wymaga następujących zmienkowych w pliku `.env`:

POSTS_MICROSERVICE_PORT=

DB_HOST=

DB_PORT=

DB_NAME=

DB_USER=

DB_PASSWORD=


## Uruchomienie

Serwis można uruchomić, wykonując główną metodę `main` w klasie `Posts.java`