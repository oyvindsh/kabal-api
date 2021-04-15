DO
$$
    BEGIN
        IF EXISTS
            (SELECT 1 from pg_roles where rolname = 'cloudsqliamuser')
        THEN
            ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT SELECT ON TABLES TO cloudsqliamuser;
            ALTER DEFAULT PRIVILEGES IN SCHEMA klage GRANT SELECT ON TABLES TO cloudsqliamuser;
        END IF;
    END
$$;

CREATE TABLE klage.part_id
(
    id    UUID PRIMARY KEY,
    type  TEXT NOT NULL, -- Må bli begrenset i kode til 'PERSON', 'ORGANISASJON', 'VIRKSOMHET'
    value TEXT NOT NULL
);

CREATE TABLE klage.mottak
(
    id                            UUID PRIMARY KEY,
    versjon                       BIGINT                   NOT NULL,
    tema_id                       VARCHAR(3)               NOT NULL,
    sakstype_id                   VARCHAR(10)              NOT NULL,
    klager_part_id                UUID                     NOT NULL,
    sak_referanse                 TEXT,
    intern_referanse              TEXT                     NOT NULL,
    dvh_referanse                 TEXT,
    innsyn_url                    TEXT,
    avsender_saksbehandlerident   TEXT,
    avsender_enhet                VARCHAR(10),
    oversendt_klageinstans_enhet  VARCHAR(10),
    dato_innsendt                 DATE,
    dato_mottatt_foersteinstans   DATE,
    dato_oversendt_klageinstans   DATE                     NOT NULL,
    dato_frist_fra_foersteinstans DATE,
    kilde                         TEXT                     NOT NULL,
    created                       TIMESTAMP WITH TIME ZONE NOT NULL,
    modified                      TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_mottak_part
        FOREIGN KEY (klager_part_id)
            REFERENCES klage.part_id (id)
);

CREATE TABLE klage.mottak_dokument
(
    id             UUID PRIMARY KEY,
    mottak_id      UUID NOT NULL,
    type           TEXT NOT NULL, -- Må bli begrenset i kode til "BRUKERS_KLAGE", "OPPRINNELIG_VEDTAK", "OVERSENDELSESBREV", "ANNET"
    journalpost_id TEXT NOT NULL,
    CONSTRAINT fk_dokument_mottak
        FOREIGN KEY (mottak_id)
            REFERENCES klage.mottak (id)
);

CREATE TABLE klage.mottak_hjemmel
(
    id        UUID PRIMARY KEY,
    mottak_id UUID NOT NULL,
    lov       TEXT NOT NULL, -- Enum i koden
    kapittel  INTEGER,
    paragraf  INTEGER,
    CONSTRAINT fk_hjemmel_mottak
        FOREIGN KEY (mottak_id)
            REFERENCES klage.mottak (id)
);

CREATE TABLE klage.mottak_brevmottaker
(
    mottak_id        UUID NOT NULL,
    mottaker_part_id UUID NOT NULL,
    CONSTRAINT fk_mottak_brevmottaker_mottak
        FOREIGN KEY (mottak_id)
            REFERENCES klage.mottak (id),
    CONSTRAINT fk_mottak_brevmottaker_part
        FOREIGN KEY (mottaker_part_id)
            REFERENCES klage.part_id (id)
);

CREATE TABLE klage.kvalitetsvurdering
(
    id                          UUID PRIMARY KEY,
    grunn_id                    INTEGER,
    eoes_id                     INTEGER,
    raadfoert_med_lege_id       INTEGER,
    intern_vurdering            TEXT,
    send_tilbakemelding         BOOLEAN,
    tilbakemelding              TEXT,
    mottaker_saksbehandlerident VARCHAR(50),
    mottaker_enhet              VARCHAR(10),
    created                     TIMESTAMP WITH TIME ZONE NOT NULL,
    modified                    TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE klage.klagebehandling
(
    id                                         UUID PRIMARY KEY,
    versjon                                    BIGINT                   NOT NULL,
    foedselsnummer                             VARCHAR(11),
    tema_id                                    VARCHAR(3)               NOT NULL,
    sakstype_id                                VARCHAR(10)              NOT NULL,
    referanse_id                               TEXT,
    dato_innsendt                              DATE,
    dato_mottatt_foersteinstans                DATE,
    dato_mottatt_klageinstans                  DATE                     NOT NULL,
    dato_behandling_startet                    DATE,
    dato_behandling_avsluttet                  DATE,
    frist                                      DATE,
    tildelt_saksbehandlerident                 VARCHAR(50),
    tildelt_enhet                              VARCHAR(10),
    avsender_enhet_foersteinstans              VARCHAR(10),
    avsender_saksbehandlerident_foersteinstans VARCHAR(50),
    mottak_id                                  UUID                     NOT NULL,
    kvalitetsvurdering_id                      UUID,
    kilde                                      VARCHAR(15)              NOT NULL,
    created                                    TIMESTAMP WITH TIME ZONE NOT NULL,
    modified                                   TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_behandling_kvalitetsvurdering
        FOREIGN KEY (kvalitetsvurdering_id)
            REFERENCES klage.kvalitetsvurdering (id),
    CONSTRAINT fk_behandling_mottak
        FOREIGN KEY (mottak_id)
            REFERENCES klage.mottak (id)
);

CREATE TABLE klage.vedtak
(
    id                 UUID PRIMARY KEY,
    utfall_id          INTEGER                  NOT NULL,
    klagebehandling_id UUID                     NOT NULL,
    modified           TIMESTAMP WITH TIME ZONE NOT NULL,
    created            TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_vedtak_klagebehandling
        FOREIGN KEY (klagebehandling_id)
            REFERENCES klage.klagebehandling (id)
);

CREATE TABLE klage.vedtaksadresse
(
    id            UUID PRIMARY KEY,
    vedtak_id     UUID NOT NULL,
    adressetype   TEXT NOT NULL, -- Må begrenses i kode til 'NORSK', 'UTENLANDSK'
    adresselinje1 TEXT,
    adresselinje2 TEXT,
    adresselinje3 TEXT,
    postnummer    VARCHAR(4),
    poststed      TEXT,
    land          VARCHAR(2)
);

CREATE TABLE klage.brevmottaker
(
    vedtak_id        UUID NOT NULL,
    mottaker_part_id UUID,
    CONSTRAINT fk_brevmottaker_mottak
        FOREIGN KEY (vedtak_id)
            REFERENCES klage.vedtak (id),
    CONSTRAINT fk_brevmottaker_part
        FOREIGN KEY (mottaker_part_id)
            REFERENCES klage.part_id (id)
);

CREATE TABLE klage.klagebehandling_hjemmel
(
    id                 INTEGER NOT NULL,
    klagebehandling_id UUID    NOT NULL,
    PRIMARY KEY (id, klagebehandling_id),
    CONSTRAINT fk_hjemmel_klagebehandling
        FOREIGN KEY (klagebehandling_id)
            REFERENCES klage.klagebehandling (id)
);

CREATE TABLE klage.saksdokument
(
    id                 UUID PRIMARY KEY,
    klagebehandling_id UUID NOT NULL,
    journalpost_id     TEXT,
    dokument_info_id   TEXT,
    CONSTRAINT fk_saksdokument_klagebehandling
        FOREIGN KEY (klagebehandling_id)
            REFERENCES klage.klagebehandling (id)
);

CREATE TABLE klage.vedtak_hjemmel
(
    id        INTEGER NOT NULL,
    vedtak_id UUID    NOT NULL,
    PRIMARY KEY (id, vedtak_id),
    CONSTRAINT fk_hjemmel_vedtak
        FOREIGN KEY (vedtak_id)
            REFERENCES klage.vedtak (id)
);

CREATE TABLE klage.endringslogginnslag
(
    id                 UUID PRIMARY KEY,
    klagebehandling_id UUID                     NOT NULL,
    saksbehandlerident VARCHAR(50),
    kilde              VARCHAR(20)              NOT NULL,
    handling           VARCHAR(20)              NOT NULL,
    felt               VARCHAR(50)              NOT NULL,
    fraverdi           TEXT,
    tilverdi           TEXT,
    tidspunkt          TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_endringslogginnslag_klagebehandling
        FOREIGN KEY (klagebehandling_id)
            REFERENCES klage.klagebehandling (id)
);

CREATE TABLE klage.vedtaksbrev
(
    id                 UUID PRIMARY KEY,
    klagebehandling_id UUID,
    brev_mal           TEXT,

    CONSTRAINT fk_vedtaksbrev_klagebehandling
        FOREIGN KEY (klagebehandling_id)
            REFERENCES klage.klagebehandling (id)
);

CREATE TABLE klage.brevelement
(
    id                 UUID PRIMARY KEY,
    brev_id            UUID    NOT NULL,
    key                VARCHAR NOT NULL,
    display_text       TEXT,
    content            TEXT,
    element_input_type TEXT,

    CONSTRAINT unique_element_brev_id_key UNIQUE (brev_id, key),
    CONSTRAINT fk_brevelement_brev_id FOREIGN KEY (brev_id) REFERENCES klage.vedtaksbrev (id)
);
