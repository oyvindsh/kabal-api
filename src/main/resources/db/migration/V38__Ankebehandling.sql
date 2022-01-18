ALTER TABLE klage.behandling
    ADD COLUMN klage_vedtaks_dato      DATE,
    ADD COLUMN klage_behandlende_enhet TEXT,
    ADD COLUMN klage_id                UUID;

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