CREATE TABLE klage.dokument_under_arbeid
(
    id                UUID PRIMARY KEY,
    dokument_id       UUID      NOT NULL,
    type              TEXT      NOT NULL,
    dokument_type     TEXT      NOT NULL,
    mellomlager_id    UUID      NOT NULL,
    opplastet         TIMESTAMP NOT NULL,
    size              BIGINT    NOT NULL,
    name              TEXT      NOT NULL,
    smarteditor_id    UUID,
    behandling_id     UUID      NOT NULL,
    created           TIMESTAMP NOT NULL,
    modified          TIMESTAMP NOT NULL,
    dokument_enhet_id UUID,
    markert_ferdig    TIMESTAMP,
    ferdigstilt       TIMESTAMP,
    parent_id         UUID
);
