CREATE SCHEMA klage;

CREATE TABLE klage.mottak
(
    id                              UUID,
    foedselsnummer                  VARCHAR(11) NOT NULL,
    hjemmel_liste                   TEXT,
    avsender_enhet                  INTEGER NOT NULL,
    avsender_saksbehandler          VARCHAR(7) NOT NULL,
    ytelse_tema                     VARCHAR(3) NOT NULL,
    referanse_innsyn                TEXT,
    created                         TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE TABLE klage.klagesak
(
    id                              UUID PRIMARY KEY,
    foedselsnummer                  VARCHAR(11) NOT NULL,
    sakstype_id                     INTEGER NOT NULL,
    modified                        TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    created                         TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_klagesak_sakstype
        FOREIGN KEY (sakstype_id)
            REFERENCES kodeverk.sakstype (id)
);

CREATE TABLE klage.saksdokument
(
    id                              INTEGER PRIMARY KEY,
    klagesak_id                     UUID NOT NULL,
    referanse                       TEXT,
    CONSTRAINT fk_saksdokument_klagesak
        FOREIGN KEY (klagesak_id)
            REFERENCES klagesak (id)
);

CREATE TABLE klage.arbeidsdokument
(
    id                              INTEGER PRIMARY KEY,
    dokument                        BYTEA
);

CREATE TABLE klage.tilbakemelding
(
    id                              INTEGER PRIMARY KEY,
    mottaker_saksbehandlerident     VARCHAR(7), -- Hente fra mottak-tabellen i stedet?
    tilbakemelding                  TEXT                     NOT NULL,
    modified                        TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    created                         TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE TABLE klage.vedtak
(
    id                              INTEGER PRIMARY KEY,
    enhet                           INTEGER NOT NULL,
    utfall_id                       INTEGER NOT NULL,
    grunn_id                        INTEGER NOT NULL,
    tilbakemelding_id               INTEGER,
    vedtaksdokument_id              INTEGER,
    modified                        TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    created                         TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_vedtak_utfall
        FOREIGN KEY (utfall_id)
            REFERENCES kodeverk.utfall (id),
    CONSTRAINT fk_vedtak_grunn
        FOREIGN KEY (grunn_id)
            REFERENCES kodeverk.grunn (id),
    CONSTRAINT fk_vedtak_dokument
        FOREIGN KEY (vedtaksdokument_id)
            REFERENCES arbeidsdokument (id),
    CONSTRAINT fk_vedtak_tilbakemelding
        FOREIGN KEY (tilbakemelding_id)
            REFERENCES tilbakemelding (id)
);

CREATE TABLE klage.behandling
(
    id                              UUID PRIMARY KEY,
    klagesak_id                     UUID                     NOT NULL,
    dato_mottatt_fra_foersteinstans DATE                     NOT NULL,
    dato_behandling_startet         DATE,
    dato_behandling_avsluttet       DATE,
    frist                           DATE                     NOT NULL,
    tildelt_saksbehandlerident      VARCHAR(7),
    eoes_id                         INTEGER NOT NULL,
    raadfoert_med_lege_id           INTEGER NOT NULL,
    vedtak_id                       INTEGER,
    modified                        TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    created                         TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_vedtak_eoes
        FOREIGN KEY (eoes_id)
            REFERENCES kodeverk.eoes (id),
    CONSTRAINT fk_vedtak_rol
        FOREIGN KEY (raadfoert_med_lege_id)
            REFERENCES kodeverk.raadfoert_med_lege (id),
    CONSTRAINT fk_behandling_klagesak
        FOREIGN KEY (klagesak_id)
            REFERENCES klagesak (id),
    CONSTRAINT fk_behandling_vedtak
        FOREIGN KEY (vedtak_id)
            REFERENCES vedtak (id)
);

CREATE TABLE klage.hjemmel
(
    id                              INTEGER PRIMARY KEY,
    behandling_id                   UUID                    NOT NULL,
    lov_id                          INTEGER                 NOT NULL,
    kapittel                        INTEGER,
    paragraf                        VARCHAR(5)              NOT NULL,
    ledd                            VARCHAR(5),
    bokstav                         VARCHAR(1),
    CONSTRAINT fk_hjemmel_lov
        FOREIGN KEY (lov_id)
            REFERENCES kodeverk.lov (id),
    CONSTRAINT fk_hjemmel_behandling
        FOREIGN KEY (behandling_id)
            REFERENCES behandling (id)
);

CREATE TABLE klage.klage_oppgave
(
    klagesak_id     UUID    NOT NULL,
    oppgave_id      INTEGER NOT NULL,
    PRIMARY KEY (klagesak_id, oppgave_id),
    CONSTRAINT fk_klage_oppgave_klage
        FOREIGN KEY (klagesak_id)
            REFERENCES klagesak (id),
    CONSTRAINT fk_klage_oppgave_oppgave
        FOREIGN KEY (oppgave_id)
            REFERENCES oppgave.oppgave (id)
);

-- create trigger to update 'modified' field in klage
