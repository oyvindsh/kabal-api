ALTER TABLE oppgave.versjonident
    DROP CONSTRAINT unique_versjonident_verdi_fnr;

ALTER TABLE oppgave.ident
    ADD CONSTRAINT unique_ident_verdi_fnr UNIQUE (verdi, folkeregisterident);