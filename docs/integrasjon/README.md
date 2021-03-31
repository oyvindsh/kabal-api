# Integrere med Kabal
![](klage_teknisk.png "Dataflyt")

## Kort fortalt

- Klage kommer inn via skjema på nav.no
- Klage arkiveres i JOARK
- Melding om innkommen klage sendes på JOARK sitt kafka-topic
- Førsteinstans håndterer klage i sin vedtaksløsning
- Dersom klage skal videre til klageinstans gjøres en POST på `kabal-api`
- Når klage er ferdig behandlet på klageinstans, legges en melding på et kafka-topic

### Oversende klage til Kabal

````
POST <kabal-api-url>/oversendelse/klage <oversendt-klage-json>
````
- DEV-url: `https://kabal-api.dev.nav.no`
- PROD-url: `https://kabal-api.intern.nav.no`

Skjema for oversendt klage kan finnes [her](../schema/oversendt-klage.json).

### Klage-vedtak på Kafka

Skjema for klage-vedtak sendt på `klage.vedtak-fattet.v1` kan finnes [her](../schema/oversendt-klage.json).
