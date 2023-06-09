CREATE TABLE klage.document_to_merge
(
    id               UUID PRIMARY KEY,
    reference_id     UUID NOT NULL,
    journalpost_id   TEXT NOT NULL,
    dokument_info_id TEXT NOT NULL,
    created          TIMESTAMP NOT NULL
);