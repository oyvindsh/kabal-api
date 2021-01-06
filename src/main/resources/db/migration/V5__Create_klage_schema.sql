CREATE SCHEMA klage;

CREATE TABLE klage.klagesak
(
    id                              UUID PRIMARY KEY,
    foedselsnummer                  VARCHAR(11),
    modified                        TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    created                         TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE TABLE klage.hjemmel
(
    id                              INTEGER PRIMARY KEY,
    lov_id                          INTEGER                 NOT NULL,
    kapittel                        INTEGER,
    paragraf                        VARCHAR(5),
    ledd                            VARCHAR(5),
    bokstav                         VARCHAR(1),
    CONSTRAINT fk_hjemmel_lov
        FOREIGN KEY (lov_id)
            REFERENCES kodeverk.lov (id)
);

CREATE TABLE klage.dokument
(
    id                              INTEGER PRIMARY KEY,
    referanse                       VARCHAR(250)
);

CREATE TABLE klage.tilbakemelding
(
    id                              INTEGER PRIMARY KEY,
    mottaker_saksbehandlerident     VARCHAR(7),
    tilbakemelding                  TEXT,
    modified                        TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    created                         TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE TABLE klage.vedtak
(
    id                              INTEGER PRIMARY KEY,
    enhet                           INTEGER,
    sakstype_id                     INTEGER,
    eoes_id                         INTEGER,
    rol_id                          INTEGER,
    utfall_id                       INTEGER,
    grunn_id                        INTEGER,
    tilbakemelding_id               INTEGER,
    vedtaksdokument_id              INTEGER,
    modified                        TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    created                         TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_vedtak_sakstype
        FOREIGN KEY (sakstype_id)
            REFERENCES kodeverk.sakstype (id),
    CONSTRAINT fk_vedtak_eoes
        FOREIGN KEY (eoes_id)
            REFERENCES kodeverk.eoes (id),
    CONSTRAINT fk_vedtak_rol
        FOREIGN KEY (rol_id)
            REFERENCES kodeverk.rol (id),
    CONSTRAINT fk_vedtak_utfall
        FOREIGN KEY (utfall_id)
            REFERENCES kodeverk.utfall (id),
    CONSTRAINT fk_vedtak_grunn
        FOREIGN KEY (grunn_id)
            REFERENCES kodeverk.grunn (id),
    CONSTRAINT fk_vedtak_dokument
        FOREIGN KEY (vedtaksdokument_id)
            REFERENCES dokument (id)
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
    hjemmel_id                      INTEGER,
    vedtak_id                       INTEGER,
    modified                        TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    created                         TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_behandling_hjemmel
        FOREIGN KEY (hjemmel_id)
            REFERENCES hjemmel (id),
    CONSTRAINT fk_behandling_klagesak
        FOREIGN KEY (klagesak_id)
            REFERENCES klagesak (id),
    CONSTRAINT fk_behandling_vedtak
        FOREIGN KEY (vedtak_id)
            REFERENCES vedtak (id)
);

CREATE TABLE klage.behandlingslogg
(
    behandling_id           INTEGER                  NOT NULL,
    created                 TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    beskrivelse             VARCHAR(500),
    CONSTRAINT fk_behandling
        FOREIGN KEY (behandling_id)
            REFERENCES behandling (id)
);

CREATE TABLE klage.behandling_dokument
(
    behandling_id           INTEGER NOT NULL,
    dokument_id             INTEGER NOT NULL,
    CONSTRAINT fk_behandling
        FOREIGN KEY (behandling_id)
            REFERENCES behandling (id),
    CONSTRAINT fk_okument
        FOREIGN KEY (dokument_id)
            REFERENCES dokument (id)
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
