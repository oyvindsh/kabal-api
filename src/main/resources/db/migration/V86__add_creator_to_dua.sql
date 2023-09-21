ALTER TABLE klage.dokument_under_arbeid
    ADD COLUMN creator_ident TEXT,
    ADD COLUMN creator_role  TEXT DEFAULT 'KABAL_SAKSBEHANDLING' NOT NULL;

UPDATE klage.dokument_under_arbeid dua
SET creator_ident = (SELECT b.tildelt_saksbehandlerident FROM klage.behandling b WHERE b.id = dua.behandling_id);

ALTER TABLE klage.dokument_under_arbeid
    ALTER COLUMN creator_ident SET NOT NULL;
