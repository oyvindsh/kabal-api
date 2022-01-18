CREATE TABLE klage.ankebehandling_registreringshjemmel
(
    id                TEXT,
    ankebehandling_id UUID
        CONSTRAINT fk_registreringshjemmel_ankebehandling
            REFERENCES behandling (id),
    PRIMARY KEY (id, ankebehandling_id)
);

CREATE INDEX IF NOT EXISTS ankebehandling_registreringshjemmel_ankebehandling_idx
    ON ankebehandling_registreringshjemmel (ankebehandling_id);

ALTER TABLE klage.behandling
    ADD COLUMN dato_vedtak_klageinstans    DATE,
    ADD COLUMN avsender_enhet_klageinstans TEXT,
    ADD COLUMN forrige_klagebehandling_id UUID;