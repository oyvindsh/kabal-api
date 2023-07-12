DROP TABLE klage.dokument_under_arbeid_brevmottaker_type;

CREATE TABLE klage.dokument_under_arbeid_brevmottaker_ident
(
    identifikator            TEXT NOT NULL,
    dokument_under_arbeid_id UUID NOT NULL,
    PRIMARY KEY (identifikator, dokument_under_arbeid_id),
    CONSTRAINT fk_brevmottaker_ident_dokument_under_arbeid
        FOREIGN KEY (dokument_under_arbeid_id)
            REFERENCES klage.dokument_under_arbeid (id)
);