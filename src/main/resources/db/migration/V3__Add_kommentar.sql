ALTER TABLE klage.mottak
    ADD COLUMN kommentar TEXT;

ALTER TABLE klage.klagebehandling
    ADD COLUMN kommentar_fra_foersteinstans TEXT;