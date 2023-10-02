CREATE TABLE klage.behandling_utfall
(
    id            TEXT NOT NULL,
    behandling_id UUID NOT NULL,
    PRIMARY KEY (id, behandling_id),
    CONSTRAINT fk_hjemmel_klagebehandling
        FOREIGN KEY (behandling_id)
            REFERENCES klage.behandling (id)
);

INSERT INTO klage.behandling_utfall (id, behandling_id)
SELECT b.utfall_id, b.id FROM klage.behandling b
WHERE utfall_id IS NOT NULL;

--Don't delete old field yet.