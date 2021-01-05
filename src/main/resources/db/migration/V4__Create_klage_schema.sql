CREATE SCHEMA klage;

CREATE TABLE klage.klage
(
    id                              UUID PRIMARY KEY,
    foedselsnummer                  VARCHAR(11),
    dato_mottatt_fra_foersteinstans DATE                     NOT NULL,
    frist                           DATE                     NOT NULL,
    tildelt_saksbehandlerident      VARCHAR(7),
    hjemmel_id                      INTEGER,
    modified                        TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    created                         TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

-- some kind of history for changes in klage table?

CREATE TABLE klage.kvalitetsskjema
(
    klage_id             UUID PRIMARY KEY,
    enhet                INTEGER,
    sakstype             VARCHAR(100),
    eoes                 INTEGER,
    rol                  INTEGER,
    utfall               VARCHAR(128),
    omgjoeringsgrunn     INTEGER,
    motta_tilbakemelding BOOLEAN,
    tilbakemelding       TEXT, --OR VARCHAR?
    hjemmel_id           INTEGER,
    CONSTRAINT fk_kvalitetsskjema_klage
        FOREIGN KEY (klage_id)
            REFERENCES KLAGE (id)
);

CREATE TABLE klage.klage_oppgave
(
    klage_id   UUID    NOT NULL,
    oppgave_id INTEGER NOT NULL,
    PRIMARY KEY (klage_id, oppgave_id),
    CONSTRAINT fk_klage_oppgave_klage
        FOREIGN KEY (klage_id)
            REFERENCES KLAGE (id),
    CONSTRAINT fk_klage_oppgave_oppgave
        FOREIGN KEY (oppgave_id)
            REFERENCES oppgave.oppgave (id)
);

-- create trigger to update 'modified' field in klage