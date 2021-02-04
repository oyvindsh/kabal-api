CREATE TABLE klage.mottak
(
    id                     UUID PRIMARY KEY,
    referanse_id           TEXT,
    foedselsnummer         VARCHAR(11)              NOT NULL,
    hjemmel_liste          TEXT,
    avsender_enhet         INTEGER                  NOT NULL,
    avsender_saksbehandler VARCHAR(7)               NOT NULL,
    tema                   VARCHAR(3)               NOT NULL,
    innsyn_url             TEXT,
    created                TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE TABLE klage.tilbakemelding
(
    id                          UUID PRIMARY KEY,
    mottaker_saksbehandlerident VARCHAR(7), -- Hente fra mottak-tabellen i stedet?
    tilbakemelding              TEXT                     NOT NULL,
    modified                    TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    created                     TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE TABLE klage.vedtak
(
    id                UUID PRIMARY KEY,
    enhet             INTEGER                  NOT NULL,
    utfall_id         INTEGER                  NOT NULL,
    grunn_id          INTEGER                  NOT NULL,
    tilbakemelding_id UUID,
    modified          TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    created           TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_vedtak_utfall
        FOREIGN KEY (utfall_id)
            REFERENCES kodeverk.utfall (id),
    CONSTRAINT fk_vedtak_grunn
        FOREIGN KEY (grunn_id)
            REFERENCES kodeverk.grunn (id),
    CONSTRAINT fk_vedtak_tilbakemelding
        FOREIGN KEY (tilbakemelding_id)
            REFERENCES klage.tilbakemelding (id)
);

CREATE TABLE klage.klagebehandling
(
    id                              UUID PRIMARY KEY,
    foedselsnummer                  VARCHAR(11)              NOT NULL,
    tema                            VARCHAR(3)               NOT NULL,
    sakstype_id                     VARCHAR(10)              NOT NULL,
    mottak_id                       UUID,
    dato_mottatt_fra_foersteinstans DATE                     NOT NULL,
    dato_behandling_startet         DATE,
    dato_behandling_avsluttet       DATE,
    frist                           DATE                     NOT NULL,
    tildelt_saksbehandlerident      VARCHAR(7),
    eoes_id                         INTEGER,
    raadfoert_med_lege_id           INTEGER,
    vedtak_id                       UUID,
    modified                        TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    created                         TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_vedtak_eoes
        FOREIGN KEY (eoes_id)
            REFERENCES kodeverk.eoes (id),
    CONSTRAINT fk_vedtak_rol
        FOREIGN KEY (raadfoert_med_lege_id)
            REFERENCES kodeverk.raadfoert_med_lege (id),
    CONSTRAINT fk_klagebehandling_sakstype
        FOREIGN KEY (sakstype_id)
            REFERENCES kodeverk.sakstype (id),
    CONSTRAINT fk_behandling_vedtak
        FOREIGN KEY (vedtak_id)
            REFERENCES klage.vedtak (id),
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

CREATE TABLE klage.klage_oppgave
(
    id                 UUID PRIMARY KEY,
    klagebehandling_id UUID   NOT NULL,
    oppgave_id         BIGINT NOT NULL,
    CONSTRAINT fk_klage_oppgave_klagebehandling
        FOREIGN KEY (klagebehandling_id)
            REFERENCES klage.klagebehandling (id),
    CONSTRAINT fk_klage_oppgave_oppgave
        FOREIGN KEY (oppgave_id)
            REFERENCES oppgave.oppgave (id)
);
