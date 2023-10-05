CREATE TABLE klage.behandling_extra_utfall
(
    id            TEXT NOT NULL,
    behandling_id UUID NOT NULL,
    PRIMARY KEY (id, behandling_id),
    CONSTRAINT fk_hjemmel_klagebehandling
        FOREIGN KEY (behandling_id)
            REFERENCES klage.behandling (id)
);