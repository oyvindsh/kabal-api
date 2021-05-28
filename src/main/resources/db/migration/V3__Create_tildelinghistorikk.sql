ALTER TABLE klage.klagebehandling
    ADD COLUMN medunderskriver_enhet      TEXT,
    ADD COLUMN dato_sendt_medunderskriver TIMESTAMP;

CREATE TABLE klage.tildelinghistorikk
(
    id                 UUID PRIMARY KEY,
    klagebehandling_id UUID NOT NULL,
    saksbehandlerident TEXT,
    enhet              TEXT,
    tidspunkt          TIMESTAMP,
    CONSTRAINT fk_tildelinghistorikk_klagebehandling
        FOREIGN KEY (klagebehandling_id)
            REFERENCES klage.klagebehandling (id)
);

CREATE TABLE klage.medunderskriverhistorikk
(
    id                 UUID PRIMARY KEY,
    klagebehandling_id UUID NOT NULL,
    saksbehandlerident TEXT,
    tidspunkt          TIMESTAMP,
    CONSTRAINT fk_medunderskriverhistorikk_klagebehandling
        FOREIGN KEY (klagebehandling_id)
            REFERENCES klage.klagebehandling (id)
);