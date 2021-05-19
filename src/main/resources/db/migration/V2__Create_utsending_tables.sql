CREATE TABLE klage.brevutsending(
    id      UUID PRIMARY KEY,
    status  TEXT NOT NULL DEFAULT 'IKKE_SENDT',
    melding TEXT
);

CREATE TABLE klage.kafka_vedtak_event(
    id                      UUID PRIMARY KEY,
    kildeReferanse          TEXT NOT NULL,
    kilde                   TEXT NOT NULL,
    utfall_id               TEXT NOT NULL,
    vedtaksbrevReferanse    TEXT,
    kabalReferanse          TEXT NOT NULL,
    status_id               TEXT NOT NULL DEFAULT '1',
    melding                 TEXT
);
