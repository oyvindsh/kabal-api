ALTER TABLE klage.delbehandling
    ADD COLUMN dato_behandling_avsluttet                  TIMESTAMP,
    ADD COLUMN dato_behandling_avsluttet_av_saksbehandler TIMESTAMP;

UPDATE klage.delbehandling d
SET dato_behandling_avsluttet = (select b.dato_behandling_avsluttet
                                 from klage.behandling b
                                 where d.behandling_id = b.id);

UPDATE klage.delbehandling d
SET dato_behandling_avsluttet_av_saksbehandler = (select b.dato_behandling_avsluttet_av_saksbehandler
                                                  from klage.behandling b
                                                  where d.behandling_id = b.id);

ALTER TABLE klage.behandling
    DROP COLUMN dato_behandling_avsluttet,
    DROP COLUMN dato_behandling_avsluttet_av_saksbehandler;