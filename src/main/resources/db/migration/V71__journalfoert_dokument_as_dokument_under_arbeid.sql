ALTER TABLE klage.dokument_under_arbeid
    ALTER COLUMN dokument_type drop not null;

ALTER TABLE klage.dokument_under_arbeid
    RENAME COLUMN dokument_type TO dokument_type_id;

ALTER TABLE klage.dokument_under_arbeid
    ADD COLUMN journalfoert_dokument_journalpost_id TEXT,
    ADD COLUMN journalfoert_dokument_dokument_info_id TEXT;