DROP INDEX klage.dokument_under_arbeid_persistent_dokument_id_idx;

ALTER TABLE klage.dokument_under_arbeid
    DROP COLUMN persistent_dokument_id;

ALTER TABLE klage.dokument_under_arbeid
    DROP COLUMN type;