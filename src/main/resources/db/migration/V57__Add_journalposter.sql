CREATE TABLE klage.dokument_under_arbeid_journalpost_id
(
    id                       UUID PRIMARY KEY,
    dokument_under_arbeid_id UUID NOT NULL,
    journalpost_id           TEXT,
    CONSTRAINT fk_journalpost_dokument_under_arbeid
        FOREIGN KEY (dokument_under_arbeid_id)
            REFERENCES klage.dokument_under_arbeid (id)
);