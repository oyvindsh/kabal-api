CREATE TABLE kodeverk.lov
(
    id          INTEGER PRIMARY KEY,
    navn        TEXT NOT NULL,
    beskrivelse TEXT
);

CREATE TABLE kodeverk.sakstype
(
    id          VARCHAR(10) PRIMARY KEY,
    navn        TEXT NOT NULL,
    beskrivelse TEXT
);

CREATE TABLE kodeverk.tema
(
    id          VARCHAR(3) PRIMARY KEY,
    navn        TEXT NOT NULL,
    beskrivelse TEXT
);

CREATE TABLE kodeverk.eoes
(
    id          INTEGER PRIMARY KEY,
    navn        TEXT NOT NULL,
    beskrivelse TEXT
);

CREATE TABLE kodeverk.grunn
(
    id          INTEGER PRIMARY KEY,
    navn        TEXT NOT NULL,
    beskrivelse TEXT
);

CREATE TABLE kodeverk.raadfoert_med_lege
(
    id          INTEGER PRIMARY KEY,
    navn        TEXT NOT NULL,
    beskrivelse TEXT
);

CREATE TABLE kodeverk.utfall
(
    id          INTEGER PRIMARY KEY,
    navn        TEXT NOT NULL,
    beskrivelse TEXT
);

INSERT INTO kodeverk.lov
VALUES (1, 'Folketrygdloven', 'Lov om folketrygd');

INSERT INTO kodeverk.sakstype
VALUES ('ae0058', 'Klage', NULL);
INSERT INTO kodeverk.sakstype
VALUES ('ae0046', 'Anke', NULL);
INSERT INTO kodeverk.sakstype
VALUES ('ae0047', 'Gjenopptak', NULL);
INSERT INTO kodeverk.sakstype
VALUES ('ae0028', 'Revurdering', NULL);
INSERT INTO kodeverk.sakstype
VALUES ('ae0161', 'Feilutbetaling', NULL);

INSERT INTO kodeverk.tema
VALUES ('AAP', 'Arbeidsavklaringspenger', 'Arbeidsavklaringspenger');
INSERT INTO kodeverk.tema
VALUES ('AAR', 'Aa-registeret', 'Aa-registeret');
INSERT INTO kodeverk.tema
VALUES ('AGR', 'Ajourhold - Grunnopplysninger', 'Ajourhold - Grunnopplysninger');
INSERT INTO kodeverk.tema
VALUES ('BAR', 'Barnetrygd', 'Barnetrygd');
INSERT INTO kodeverk.tema
VALUES ('BID', 'Bidrag', 'Bidrag');
INSERT INTO kodeverk.tema
VALUES ('BIL', 'Bil', 'Bil');
INSERT INTO kodeverk.tema
VALUES ('DAG', 'Dagpenger', 'Dagpenger');
INSERT INTO kodeverk.tema
VALUES ('ENF', 'Enslig forsørger', 'Enslig forsørger');
INSERT INTO kodeverk.tema
VALUES ('ERS', 'Erstatning', 'Erstatning');
INSERT INTO kodeverk.tema
VALUES ('FAR', 'Farskap', 'Farskap');
INSERT INTO kodeverk.tema
VALUES ('FEI', 'Feilutbetaling', 'Feilutbetaling');
INSERT INTO kodeverk.tema
VALUES ('FOR', 'Foreldre- og svangerskapspenger', 'Foreldre- og svangerskapspenger');
INSERT INTO kodeverk.tema
VALUES ('FOS', 'Forsikring', 'Forsikring');
INSERT INTO kodeverk.tema
VALUES ('FRI', 'Kompensasjon for selvstendig næringsdrivende/frilansere',
        'Kompensasjon for selvstendig næringsdrivende/frilansere');
INSERT INTO kodeverk.tema
VALUES ('FUL', 'Fullmakt', 'Fullmakt');
INSERT INTO kodeverk.tema
VALUES ('GEN', 'Generell', 'Generell');
INSERT INTO kodeverk.tema
VALUES ('GRA', 'Gravferdsstønad', 'Gravferdsstønad');
INSERT INTO kodeverk.tema
VALUES ('GRU', 'Grunn- og hjelpestønad', 'Grunn- og hjelpestønad');
INSERT INTO kodeverk.tema
VALUES ('HEL', 'Helsetjenester og ortopediske hjelpemidler', 'Helsetjenester og ortopediske hjelpemidler');
INSERT INTO kodeverk.tema
VALUES ('HJE', 'Hjelpemidler', 'Hjelpemidler');
INSERT INTO kodeverk.tema
VALUES ('IAR', 'Inkluderende arbeidsliv', 'Inkluderende arbeidsliv');
INSERT INTO kodeverk.tema
VALUES ('IND', 'Tiltakspenger', 'Tiltakspenger');
INSERT INTO kodeverk.tema
VALUES ('KON', 'Kontantstøtte', 'Kontantstøtte');
INSERT INTO kodeverk.tema
VALUES ('KTR', 'Kontroll', 'Kontroll');
INSERT INTO kodeverk.tema
VALUES ('MED', 'Medlemskap', 'Medlemskap');
INSERT INTO kodeverk.tema
VALUES ('MOB', 'Mobilitetsfremmende stønad', 'Mobilitetsfremmende stønad');
INSERT INTO kodeverk.tema
VALUES ('OMS', 'Omsorgspenger, pleiepenger og opplæringspenger', 'Omsorgspenger, pleiepenger og opplæringspenger');
INSERT INTO kodeverk.tema
VALUES ('OPA', 'Oppfølging - Arbeidsgiver', 'Oppfølging - Arbeidsgiver');
INSERT INTO kodeverk.tema
VALUES ('OPP', 'Oppfølging', 'Oppfølging');
INSERT INTO kodeverk.tema
VALUES ('PEN', 'Pensjon', 'Pensjon');
INSERT INTO kodeverk.tema
VALUES ('PER', 'Permittering og masseoppsigelser', 'Permittering og masseoppsigelser');
INSERT INTO kodeverk.tema
VALUES ('REH', 'Rehabilitering', 'Rehabilitering');
INSERT INTO kodeverk.tema
VALUES ('REK', 'Rekruttering og stilling', 'Rekruttering og stilling');
INSERT INTO kodeverk.tema
VALUES ('RPO', 'Retting av personopplysninger', 'Retting av personopplysninger');
INSERT INTO kodeverk.tema
VALUES ('RVE', 'Rettferdsvederlag', 'Rettferdsvederlag');
INSERT INTO kodeverk.tema
VALUES ('SAA', 'Sanksjon - Arbeidsgiver', 'Sanksjon - Arbeidsgiver');
INSERT INTO kodeverk.tema
VALUES ('SAK', 'Saksomkostninger', 'Saksomkostninger');
INSERT INTO kodeverk.tema
VALUES ('SAP', 'Sanksjon - Person', 'Sanksjon - Person');
INSERT INTO kodeverk.tema
VALUES ('SER', 'Serviceklager', 'Serviceklager');
INSERT INTO kodeverk.tema
VALUES ('SIK', 'Sikkerhetstiltak', 'Sikkerhetstiltak');
INSERT INTO kodeverk.tema
VALUES ('STO', 'Regnskap/utbetaling', 'Regnskap/utbetaling');
INSERT INTO kodeverk.tema
VALUES ('SUP', 'Supplerende stønad', 'Supplerende stønad');
INSERT INTO kodeverk.tema
VALUES ('SYK', 'Sykepenger', 'Sykepenger');
INSERT INTO kodeverk.tema
VALUES ('SYM', 'Sykmeldinger', 'Sykmeldinger');
INSERT INTO kodeverk.tema
VALUES ('TIL', 'Tiltak', 'Tiltak');
INSERT INTO kodeverk.tema
VALUES ('TRK', 'Trekkhåndtering', 'Trekkhåndtering');
INSERT INTO kodeverk.tema
VALUES ('TRY', 'Trygdeavgift', 'Trygdeavgift');
INSERT INTO kodeverk.tema
VALUES ('TSO', 'Tilleggsstønad', 'Tilleggsstønad');
INSERT INTO kodeverk.tema
VALUES ('TSR', 'Tilleggsstønad arbeidssøkere', 'Tilleggsstønad arbeidssøkere');
INSERT INTO kodeverk.tema
VALUES ('UFM', 'Unntak fra medlemskap', 'Unntak fra medlemskap');
INSERT INTO kodeverk.tema
VALUES ('UFO', 'Uføretrygd', 'Uføretrygd');
INSERT INTO kodeverk.tema
VALUES ('UKJ', 'Ukjent', 'Ukjent');
INSERT INTO kodeverk.tema
VALUES ('VEN', 'Ventelønn', 'Ventelønn');
INSERT INTO kodeverk.tema
VALUES ('YRA', 'Yrkesrettet attføring', 'Yrkesrettet attføring');
INSERT INTO kodeverk.tema
VALUES ('YRK', 'Yrkesskade / Menerstatning', 'Yrkesskade / Menerstatning');

INSERT INTO kodeverk.eoes
VALUES (1, 'Riktig', 'Problemstilling knyttet til EØS/utland er godt håndtert');
INSERT INTO kodeverk.eoes
VALUES (2, 'Ikke oppdaget', 'Vedtaksinstansen har ikke oppdaget at saken gjelder EØS/utland');
INSERT INTO kodeverk.eoes
VALUES (3, 'Feil', 'Vedtaksinstansen har oppdaget at saken gjelder EØS/utland, men har håndtert saken feil');
INSERT INTO kodeverk.eoes
VALUES (4, 'Uaktuelt', 'EØS/utenlandsproblematikk er ikke relevant i saken');

INSERT INTO kodeverk.raadfoert_med_lege
VALUES (1, 'Mangler', 'Saken burde vært forelagt for ROL i vedtaksinstansen');
INSERT INTO kodeverk.raadfoert_med_lege
VALUES (2, 'Riktig',
        'Saken er godt nok medisinsk opplyst med ROL-uttalelse i vedtaksinstansen/uten at ROL har blitt konsultert');
INSERT INTO kodeverk.raadfoert_med_lege
VALUES (3, 'Mangelfull', 'Saken er forelagt ROL i vedtaksinstans, men er fortsatt mangelfullt medisinsk vurdert');
INSERT INTO kodeverk.raadfoert_med_lege
VALUES (4, 'Uaktuelt', 'Saken handler ikke om trygdemedisinske vurderinger');

INSERT INTO kodeverk.utfall
VALUES (1, 'Trukket', NULL);
INSERT INTO kodeverk.utfall
VALUES (2, 'Retur', NULL);
INSERT INTO kodeverk.utfall
VALUES (3, 'Opphevet', NULL);
INSERT INTO kodeverk.utfall
VALUES (4, 'Medhold', NULL);
INSERT INTO kodeverk.utfall
VALUES (5, 'Delvis medhold', NULL);
INSERT INTO kodeverk.utfall
VALUES (6, 'Opprettholdt', NULL);
INSERT INTO kodeverk.utfall
VALUES (7, 'Ugunst (Ugyldig)', NULL);
INSERT INTO kodeverk.utfall
VALUES (8, 'Avvist', NULL);

INSERT INTO kodeverk.grunn
VALUES (1, 'Mangelfull utredning', NULL);
INSERT INTO kodeverk.grunn
VALUES (2, 'Andre saksbehandlingsfeil', NULL);
INSERT INTO kodeverk.grunn
VALUES (3, 'Endret faktum', NULL);
INSERT INTO kodeverk.grunn
VALUES (4, 'Feil i bevisvurderingen', NULL);
INSERT INTO kodeverk.grunn
VALUES (5, 'Feil i den generelle lovtolkningen', NULL);
INSERT INTO kodeverk.grunn
VALUES (6, 'Feil i den konkrete rettsanvendelsen', NULL);
