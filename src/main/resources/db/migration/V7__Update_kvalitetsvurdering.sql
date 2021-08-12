ALTER TABLE klage.kvalitetsvurdering
    ADD COLUMN inkluderte_dato_for_klage   BOOLEAN,
    ADD COLUMN inkluderte_dato_for_vedtak  BOOLEAN,
    ADD COLUMN oversendelsesbrev_bra       BOOLEAN,
    ADD COLUMN avvik_oversendelsesbrev     TEXT,
    ADD COLUMN kommentar_oversendelsesbrev TEXT,
    ADD COLUMN utredning_bra               BOOLEAN,
    ADD COLUMN avvik_utredning             TEXT,
    ADD COLUMN kommentar_utredning         TEXT,
    ADD COLUMN vedtak_bra                  BOOLEAN,
    ADD COLUMN avvik_vedtak                TEXT,
    ADD COLUMN kommentar_vedtak            TEXT,
    ADD COLUMN avvik_stor_konsekvens       BOOLEAN;

CREATE TABLE klage.kvalitetsvurdering_avvik_oversendelsesbrev
(
    id                    TEXT NOT NULL,
    kvalitetsvurdering_id UUID NOT NULL,
    PRIMARY KEY (id, kvalitetsvurdering_id),
    CONSTRAINT fk_kvalitetsvurdering_avvik_oversendelsesbrev
        FOREIGN KEY (kvalitetsvurdering_id)
            REFERENCES klage.kvalitetsvurdering (id)
);

CREATE TABLE klage.kvalitetsvurdering_avvik_utredning
(
    id                    TEXT NOT NULL,
    kvalitetsvurdering_id UUID NOT NULL,
    PRIMARY KEY (id, kvalitetsvurdering_id),
    CONSTRAINT fk_kvalitetsvurdering_avvik_utredning
        FOREIGN KEY (kvalitetsvurdering_id)
            REFERENCES klage.kvalitetsvurdering (id)
);

CREATE TABLE klage.kvalitetsvurdering_avvik_vedtak
(
    id                    TEXT NOT NULL,
    kvalitetsvurdering_id UUID NOT NULL,
    PRIMARY KEY (id, kvalitetsvurdering_id),
    CONSTRAINT fk_kvalitetsvurdering_avvik_vedtak
        FOREIGN KEY (kvalitetsvurdering_id)
            REFERENCES klage.kvalitetsvurdering (id)
);