# kabal-api
API for oppgaver i klageinstansen.

##Lokal kjøring av appen
Det er mulig å kjøre appen lokalt ved hjelp av docker-compose. For å sette opp database, kafka, elastic search og redis kan du kjøre følgende i rotmappa:

```docker-compose up --build```

Deretter kan du sette opp kjøring av spring boot-appen i IntelliJ, presiser `local` som `active profile`.

