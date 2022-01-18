ALTER TABLE klage.behandling
    ADD COLUMN dato_vedtak_klageinstans    DATE,
    ADD COLUMN avsender_enhet_klageinstans TEXT,
    ADD COLUMN forrige_klagebehandling_id  UUID;

CREATE TABLE klage.ankebehandling_registreringshjemmel
(
    id                TEXT,
    ankebehandling_id UUID,
    PRIMARY KEY (id, ankebehandling_id)
);

ALTER TABLE klage.ankebehandling_registreringshjemmel
    ADD CONSTRAINT fk_registreringshjemmel_ankebehandling
        FOREIGN KEY (ankebehandling_id)
            REFERENCES klage.behandling (id);