CREATE TABLE klage.kafka_dvh_event
(
    id                 UUID PRIMARY KEY,
    klagebehandling_id UUID NOT NULL,
    kilde              TEXT NOT NULL,
    kilde_referanse    TEXT NOT NULL,
    json_payload       TEXT NOT NULL,
    status_id          TEXT NOT NULL DEFAULT '1',
    error_message      TEXT
);

-- Call it what it is
ALTER TABLE klage.kafka_vedtak_event
    RENAME COLUMN melding TO error_message;

-- Call it what it is
ALTER TABLE klage.klagebehandling
    RENAME COLUMN referanse_id TO kilde_referanse;

-- Is really not nullable, since mottak is not.
ALTER TABLE klage.klagebehandling
    ALTER COLUMN kilde_referanse SET NOT NULL;
