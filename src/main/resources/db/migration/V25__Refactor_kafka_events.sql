DROP TABLE klage.kafka_vedtak_event, klage.kafka_dvh_event;

CREATE TABLE klage.kafka_event
(
    id                 UUID PRIMARY KEY,
    klagebehandling_id UUID      NOT NULL,
    kilde              TEXT      NOT NULL,
    kilde_referanse    TEXT      NOT NULL,
    json_payload       TEXT      NOT NULL,
    status_id          TEXT      NOT NULL DEFAULT '1',
    error_message      TEXT,
    type               TEXT,
    created            TIMESTAMP NOT NULL
);