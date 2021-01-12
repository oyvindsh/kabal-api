CREATE SCHEMA kodeverk;

CREATE TABLE kodeverk.lov
(
    id              INTEGER PRIMARY KEY,
    navn            TEXT     NOT NULL,
    beskrivelse     TEXT
);

CREATE TABLE kodeverk.sakstype
(
    id              INTEGER PRIMARY KEY,
    navn            TEXT     NOT NULL,
    beskrivelse     TEXT
);

CREATE TABLE kodeverk.eoes
(
    id              INTEGER PRIMARY KEY,
    navn            TEXT     NOT NULL,
    beskrivelse     TEXT
);

CREATE TABLE kodeverk.grunn
(
    id              INTEGER PRIMARY KEY,
    navn            TEXT    NOT NULL,
    beskrivelse     TEXT
);

CREATE TABLE kodeverk.raadfoert_med_lege
(
    id              INTEGER PRIMARY KEY,
    navn            TEXT     NOT NULL,
    beskrivelse     TEXT
);

CREATE TABLE kodeverk.utfall
(
    id              INTEGER PRIMARY KEY,
    navn            TEXT     NOT NULL,
    beskrivelse     TEXT
);

INSERT INTO kodeverk.lov VALUES(1, 'Folketrygdloven', 'Lov om folketrygd');

INSERT INTO kodeverk.sakstype VALUES(1, 'Klage', NULL);
INSERT INTO kodeverk.sakstype VALUES(2, 'Anke', NULL);
INSERT INTO kodeverk.sakstype VALUES(3, 'Gjenopptak', NULL);
INSERT INTO kodeverk.sakstype VALUES(4, 'Revurdering', NULL);

INSERT INTO kodeverk.eoes VALUES(1, 'Riktig', 'Problemstilling knyttet til EØS/utland er godt håndtert');
INSERT INTO kodeverk.eoes VALUES(2, 'Ikke oppdaget', 'Vedtaksinstansen har ikke oppdaget at saken gjelder EØS/utland');
INSERT INTO kodeverk.eoes VALUES(3, 'Feil', 'Vedtaksinstansen har oppdaget at saken gjelder EØS/utland, men har håndtert saken feil');
INSERT INTO kodeverk.eoes VALUES(4, 'Uaktuelt', 'EØS/utenlandsproblematikk er ikke relevant i saken');

INSERT INTO kodeverk.raadfoert_med_lege VALUES(1, 'Mangler', 'Saken burde vært forelagt for ROL i vedtaksinstansen');
INSERT INTO kodeverk.raadfoert_med_lege VALUES(2, 'Riktig', 'Saken er godt nok medisinsk opplyst med ROL-uttalelse i vedtaksinstansen/uten at ROL har blitt konsultert');
INSERT INTO kodeverk.raadfoert_med_lege VALUES(3, 'Mangelfull', 'Saken er forelagt ROL i vedtaksinstans, men er fortsatt mangelfullt medisinsk vurdert');
INSERT INTO kodeverk.raadfoert_med_lege VALUES(4, 'Uaktuelt', 'Saken handler ikke om trygdemedisinske vurderinger');

INSERT INTO kodeverk.utfall VALUES(1, 'Trukket', NULL);
INSERT INTO kodeverk.utfall VALUES(2, 'Retur', NULL);
INSERT INTO kodeverk.utfall VALUES(3, 'Opphevet', NULL);
INSERT INTO kodeverk.utfall VALUES(4, 'Medhold', NULL);
INSERT INTO kodeverk.utfall VALUES(5, 'Delvis medhold', NULL);
INSERT INTO kodeverk.utfall VALUES(6, 'Opprettholdt', NULL);
INSERT INTO kodeverk.utfall VALUES(7, 'Ugunst (Ugyldig)', NULL);
INSERT INTO kodeverk.utfall VALUES(8, 'Avvist', NULL);

INSERT INTO kodeverk.grunn VALUES(1, 'Mangelfull utredning', NULL);
INSERT INTO kodeverk.grunn VALUES(2, 'Andre saksbehandlingsfeil', NULL);
INSERT INTO kodeverk.grunn VALUES(3, 'Endret faktum', NULL);
INSERT INTO kodeverk.grunn VALUES(4, 'Feil i bevisvurderingen', NULL);
INSERT INTO kodeverk.grunn VALUES(5, 'Feil i den generelle lovtolkningen', NULL);
INSERT INTO kodeverk.grunn VALUES(6, 'Feil i den konkrete rettsanvendelsen', NULL);
