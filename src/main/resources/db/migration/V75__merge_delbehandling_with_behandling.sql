ALTER TABLE klage.behandling
    ADD COLUMN utfall_id                                  TEXT,
    ADD COLUMN dato_behandling_avsluttet_av_saksbehandler TIMESTAMP,
    ADD COLUMN dato_behandling_avsluttet                  TIMESTAMP,
    ADD COLUMN medunderskriverident TEXT,
    ADD COLUMN dato_sendt_medunderskriver TIMESTAMP,
    ADD COLUMN medunderskriverflyt_id TEXT;


UPDATE klage.behandling b
SET utfall_id = (select d.utfall_id
                 from klage.delbehandling d
                 where d.behandling_id = b.id);

UPDATE klage.behandling b
SET dato_behandling_avsluttet_av_saksbehandler = (select d.dato_behandling_avsluttet_av_saksbehandler
                                                  from klage.delbehandling d
                                                  where d.behandling_id = b.id);
UPDATE klage.behandling b
SET dato_behandling_avsluttet = (select d.dato_behandling_avsluttet
                                 from klage.delbehandling d
                                 where d.behandling_id = b.id);

UPDATE klage.behandling b
SET medunderskriverident = (select d.medunderskriverident
                 from klage.delbehandling d
                 where d.behandling_id = b.id);

UPDATE klage.behandling b
SET dato_sendt_medunderskriver = (select d.dato_sendt_medunderskriver
                 from klage.delbehandling d
                 where d.behandling_id = b.id);

UPDATE klage.behandling b
SET medunderskriverflyt_id = (select d.medunderskriverflyt_id
                                  from klage.delbehandling d
                                  where d.behandling_id = b.id);


ALTER TABLE klage.delbehandling_registreringshjemmel
    ADD COLUMN behandling_id UUID;

UPDATE klage.delbehandling_registreringshjemmel dr
SET behandling_id = (select d.behandling_id
                                 from klage.delbehandling d
                                 where d.id = dr.delbehandling_id);

ALTER TABLE klage.delbehandling_registreringshjemmel
    ALTER COLUMN behandling_id SET NOT NULL;

ALTER TABLE klage.delbehandling_registreringshjemmel
    ADD CONSTRAINT fk_behandling_registreringshjemmel
        FOREIGN KEY (behandling_id)
            REFERENCES klage.behandling (id);

ALTER TABLE klage.delbehandling_registreringshjemmel
    DROP COLUMN delbehandling_id;

ALTER TABLE klage.delbehandling_registreringshjemmel
    RENAME TO behandling_registreringshjemmel;


ALTER TABLE klage.medunderskriverhistorikk
    ADD COLUMN behandling_id UUID;

UPDATE klage.medunderskriverhistorikk m
SET behandling_id = (select d.behandling_id
                     from klage.delbehandling d
                     where d.id = m.delbehandling_id);

ALTER TABLE klage.medunderskriverhistorikk
    ALTER COLUMN behandling_id SET NOT NULL;

ALTER TABLE klage.medunderskriverhistorikk
    ADD CONSTRAINT fk_behandling_medunderskriverhistorikk
        FOREIGN KEY (behandling_id)
            REFERENCES klage.behandling (id);

ALTER TABLE klage.medunderskriverhistorikk
    DROP COLUMN delbehandling_id;