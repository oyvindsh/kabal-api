CREATE TABLE klage.dokument_under_arbeid
(
    id                     UUID PRIMARY KEY,
    persistent_dokument_id UUID      NOT NULL,
    type                   TEXT      NOT NULL,
    dokument_type          TEXT      NOT NULL,
    mellomlager_id         TEXT      NOT NULL,
    opplastet              TIMESTAMP NOT NULL,
    size                   BIGINT    NOT NULL,
    name                   TEXT      NOT NULL,
    smarteditor_id         UUID,
    behandling_id          UUID      NOT NULL,
    created                TIMESTAMP NOT NULL,
    modified               TIMESTAMP NOT NULL,
    dokument_enhet_id      UUID,
    markert_ferdig         TIMESTAMP,
    ferdigstilt            TIMESTAMP,
    parent_id              UUID,

    CONSTRAINT unique_persistent_dokument_id UNIQUE (persistent_dokument_id) DEFERRABLE INITIALLY DEFERRED
);

CREATE INDEX dokument_under_arbeid_persistent_dokument_id_idx ON klage.dokument_under_arbeid (persistent_dokument_id);
