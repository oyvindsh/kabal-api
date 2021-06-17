CREATE TABLE klage.valgtenhet
(
    saksbehandlerident TEXT PRIMARY KEY,
    enhet_id           TEXT      NOT NULL,
    enhet_navn         TEXT      NOT NULL,
    tidspunkt          TIMESTAMP NOT NULL
);