CREATE TABLE klage.kommentar
(
    id                 UUID PRIMARY KEY,
    klagebehandling_id UUID NOT NULL,
    saksbehandlerident TEXT,
    kommentar          TEXT,
    tidspunkt          TIMESTAMP,
    CONSTRAINT fk_kommentar_klagebehandling
        FOREIGN KEY (klagebehandling_id)
            REFERENCES klage.klagebehandling (id)
);