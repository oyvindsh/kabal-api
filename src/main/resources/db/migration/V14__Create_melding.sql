CREATE TABLE klage.melding
(
    id                 UUID PRIMARY KEY,
    klagebehandling_id UUID      NOT NULL,
    saksbehandlerident TEXT      NOT NULL,
    text               TEXT      NOT NULL,
    created            TIMESTAMP NOT NULL,
    modified           TIMESTAMP,
    CONSTRAINT fk_kommentar_klagebehandling
        FOREIGN KEY (klagebehandling_id)
            REFERENCES klage.klagebehandling (id)
);