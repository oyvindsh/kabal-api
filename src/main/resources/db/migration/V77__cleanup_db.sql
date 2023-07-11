DROP TABLE klage.valgt_enhet;
DROP TABLE klage.ankebehandling_registreringshjemmel;
DROP TABLE klage.brevutsending;
DROP TABLE klage.innstillinger;

ALTER TABLE klage.behandling_registreringshjemmel
DROP COLUMN delbehandling_id;

ALTER TABLE klage.medunderskriverhistorikk
DROP COLUMN delbehandling_id;

DROP TABLE klage.delbehandling;
