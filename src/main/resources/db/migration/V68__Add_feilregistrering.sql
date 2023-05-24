ALTER TABLE klage.behandling
    ADD COLUMN feilregistrering_nav_ident    TEXT,
    ADD COLUMN feilregistrering_registered   TIMESTAMP,
    ADD COLUMN feilregistrering_reason       TEXT,
    ADD COLUMN feilregistrering_fagsystem_id TEXT;