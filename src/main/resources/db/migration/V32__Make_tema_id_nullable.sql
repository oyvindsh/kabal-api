ALTER TABLE klage.mottak
    ALTER COLUMN tema_id DROP NOT NULL;

ALTER TABLE klage.mottak
    ADD COLUMN ytelse_id TEXT;

ALTER TABLE klage.klagebehandling
    ALTER COLUMN tema_id DROP NOT NULL;