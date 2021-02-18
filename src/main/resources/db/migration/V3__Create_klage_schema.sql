CREATE TABLE klage.mottak
(
    id                            UUID PRIMARY KEY,
    tema_id                       VARCHAR(3)               NOT NULL,
    sakstype_id                   VARCHAR(10)              NOT NULL,
    referanse_id                  TEXT,
    innsyn_url                    TEXT,
    foedselsnummer                VARCHAR(11),
    organisasjonsnummer           VARCHAR(9),
    virksomhetsnummer             VARCHAR(9),
    hjemmel_liste                 TEXT,
    beskrivelse                   TEXT,
    avsender_saksbehandlerident   VARCHAR(20),
    avsender_enhet                VARCHAR(10),
    oversendt_klageinstans_enhet  VARCHAR(10),
    status                        VARCHAR(20),
    status_kategori               VARCHAR(20),
    tildelt_enhet                 VARCHAR(10),
    tildelt_saksbehandlerident    VARCHAR(20),
    journalpost_id                VARCHAR(40),
    journalpost_kilde             VARCHAR(40),
    dato_innsendt                 DATE,
    dato_mottatt_foersteinstans   DATE,
    dato_oversendt_klageinstans   DATE,
    dato_frist_fra_foersteinstans DATE,
    kilde                         VARCHAR(15)              NOT NULL,
    created                       TIMESTAMP WITH TIME ZONE NOT NULL,
    modified                      TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_mottak_sakstype
        FOREIGN KEY (sakstype_id)
            REFERENCES kodeverk.sakstype (id),
    CONSTRAINT fk_mottak_tema
        FOREIGN KEY (tema_id)
            REFERENCES kodeverk.tema (id)
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
    mottaker_saksbehandlerident VARCHAR(20),
    mottaker_enhet              VARCHAR(10),
    created                     TIMESTAMP WITH TIME ZONE NOT NULL,
    modified                    TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_kvalitetsvurdering_grunn
        FOREIGN KEY (grunn_id)
            REFERENCES kodeverk.grunn (id),
    CONSTRAINT fk_kvalitetsvurdering_eoes
        FOREIGN KEY (eoes_id)
            REFERENCES kodeverk.eoes (id),
    CONSTRAINT fk_kvalitetsvurdering_rol
        FOREIGN KEY (raadfoert_med_lege_id)
            REFERENCES kodeverk.raadfoert_med_lege (id)
);

CREATE TABLE klage.vedtak
(
    id        UUID PRIMARY KEY,
    utfall_id INTEGER                  NOT NULL,
    modified  TIMESTAMP WITH TIME ZONE NOT NULL,
    created   TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_vedtak_utfall
        FOREIGN KEY (utfall_id)
            REFERENCES kodeverk.utfall (id)
);

CREATE TABLE klage.klagebehandling
(
    id                            UUID PRIMARY KEY,
    versjon                       BIGINT                   NOT NULL,
    foedselsnummer                VARCHAR(11),
    tema_id                       VARCHAR(3)               NOT NULL,
    sakstype_id                   VARCHAR(10)              NOT NULL,
    referanse_id                  TEXT,
    dato_innsendt                 DATE,
    dato_mottatt_foersteinstans   DATE,
    dato_mottatt_klageinstans     DATE                     NOT NULL,
    dato_behandling_startet       DATE,
    dato_behandling_avsluttet     DATE,
    frist                         DATE,
    tildelt_saksbehandlerident    VARCHAR(20),
    tildelt_enhet                 VARCHAR(10),
    avsender_enhet_foersteinstans VARCHAR(10),
    mottak_id                     UUID                     NOT NULL,
    vedtak_id                     UUID,
    kvalitetsvurdering_id         UUID,
    kilde                         VARCHAR(15)              NOT NULL,
    created                       TIMESTAMP WITH TIME ZONE NOT NULL,
    modified                      TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_klagebehandling_sakstype
        FOREIGN KEY (sakstype_id)
            REFERENCES kodeverk.sakstype (id),
    CONSTRAINT fk_behandling_vedtak
        FOREIGN KEY (vedtak_id)
            REFERENCES klage.vedtak (id),
    CONSTRAINT fk_behandling_kvalitetsvurdering
        FOREIGN KEY (kvalitetsvurdering_id)
            REFERENCES klage.kvalitetsvurdering (id),
    CONSTRAINT fk_behandling_mottak
        FOREIGN KEY (mottak_id)
            REFERENCES klage.mottak (id)
);

CREATE TABLE klage.hjemmel
(
    id                 UUID PRIMARY KEY,
    klagebehandling_id UUID NOT NULL,
    lov_id             INTEGER,
    kapittel           INTEGER,
    paragraf           INTEGER,
    ledd               INTEGER,
    bokstav            VARCHAR(1),
    original           TEXT NOT NULL,
    CONSTRAINT fk_hjemmel_lov
        FOREIGN KEY (lov_id)
            REFERENCES kodeverk.lov (id),
    CONSTRAINT fk_hjemmel_behandling
        FOREIGN KEY (klagebehandling_id)
            REFERENCES klage.klagebehandling (id)
);

CREATE TABLE klage.saksdokument
(
    id                 UUID PRIMARY KEY,
    klagebehandling_id UUID NOT NULL,
    referanse          TEXT,
    CONSTRAINT fk_saksdokument_klagebehandling
        FOREIGN KEY (klagebehandling_id)
            REFERENCES klage.klagebehandling (id)
);

CREATE TABLE klage.mottak_oppgave
(
    id         UUID PRIMARY KEY,
    mottak_id  UUID   NOT NULL,
    oppgave_id BIGINT NOT NULL,
    CONSTRAINT fk_mottak_oppgave_mottak
        FOREIGN KEY (mottak_id)
            REFERENCES klage.mottak (id),
    CONSTRAINT fk_mottak_oppgave_oppgave
        FOREIGN KEY (oppgave_id)
            REFERENCES oppgave.oppgave (id)
);
