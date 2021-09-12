ALTER TABLE klage.klagebehandling
    ADD COLUMN vedtak_id UUID REFERENCES klage.vedtak (id);

UPDATE klage.klagebehandling k
    SET vedtak_id = (SELECT v.id FROM klage.vedtak v WHERE v.klagebehandling_id = k.id);

ALTER TABLE klage.vedtak
    DROP COLUMN klagebehandling_id;