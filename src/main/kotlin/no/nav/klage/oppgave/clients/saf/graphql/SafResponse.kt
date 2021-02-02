package no.nav.klage.oppgave.clients.saf.graphql

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.time.LocalDateTime

data class DokumentoversiktBrukerResponse(val data: DokumentoversiktBrukerDataWrapper?, val errors: List<Error>?)

@JsonIgnoreProperties(ignoreUnknown = true)
data class Error(val message: String, val extensions: Extensions)

data class Extensions(val classification: String)

data class DokumentoversiktBrukerDataWrapper(val dokumentoversiktBruker: DokumentoversiktBruker)

data class DokumentoversiktBruker(val journalposter: List<Journalpost>, val sideInfo: SideInfo)

data class SideInfo(val sluttpeker: String?, val finnesNesteSide: Boolean)

data class JournalpostResponse(val data: JournalpostDataWrapper?, val errors: List<Error>?)

data class JournalpostDataWrapper(val journalpost: Journalpost?)

@JsonIgnoreProperties(ignoreUnknown = true)
data class Journalpost(
    val journalpostId: String,
    val tittel: String?,
    val journalposttype: Journalposttype?,
    val journalstatus: Journalstatus?,
    val tema: Tema?,
    val temanavn: String?,
    val behandlingstema: String?,
    val behandlingstemanavn: String?,
    val sak: Sak?,
    val skjerming: String?,
    val datoOpprettet: LocalDateTime,
    val dokumenter: List<DokumentInfo>?
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class DokumentInfo(
    val dokumentInfoId: String,
    val tittel: String?,
    val brevkode: String?,
    val skjerming: String?,
    val dokumentvarianter: List<Dokumentvariant>
)

data class Dokumentvariant(
    val variantformat: Variantformat,
    val filnavn: String?,
    val saksbehandlerHarTilgang: Boolean,
    val skjerming: SkjermingType?
)

enum class SkjermingType {
    POL, //Indikerer at det er fattet et vedtak etter personopplysningsloven (GDPR - brukers rett til å bli glemt).
    FEIL //Indikerer at det har blitt gjort en feil under mottak, journalføring eller brevproduksjon, slik at journalposten eller dokumentene er markert for sletting.
}

enum class Variantformat {
    //Den "offisielle" versjonen av et dokument, som er beregnet på visning og langtidsbevaring. I de fleste tilfeller er arkivvarianten lik dokumentet brukeren sendte inn eller mottok (digitalt eller på papir). Arkivvarianten er alltid i menneskelesbart format, som PDF, PDF/A eller PNG.
    //Alle dokumenter har en arkivvariant, med mindre bruker har fått innvilget vedtak om sletting eller skjerming av opplysninger i arkivet.
    ARKIV,

    //Dette er en sladdet variant av det opprinnelige dokumentet. Dersom det finnes en SLADDET variant, vil de fleste NAV-ansatte kun ha tilgang til denne varianten og ikke arkivvariant. Enkelte saksbehandlere vil imidlertid ha tilgang til både SLADDET og ARKIV.
    SLADDET,

    //Produksjonsvariant i eget proprietært format. Varianten finnes for dokumenter som er produsert i Metaforce eller Brevklient.
    PRODUKSJON,

    //Produksjonsvariant i eget proprietært format. Varianten finnes kun for dokumenter som er produsert i Exstream Live Editor.
    PRODUKSJON_DLF,

    //Variant av dokument som inneholder spørsmålstekster, hjelpetekster og ubesvarte spørsmål fra søknadsdialogen. Fullversjon genereres for enkelte søknadsskjema fra nav.no, og brukes ved klagebehandling.
    FULLVERSJON,

    //Variant av dokumentet i strukturert format, f.eks. XML eller JSON. Originalvarianten er beregnet på maskinell lesning og behandling.
    ORIGINAL
}

data class Sak(val datoOpprettet: LocalDateTime?, val fagsakId: String?, val fagsaksystem: String?)

enum class Tema {
    AAP, //Arbeidsavklaringspenger
    AAR, //Aa-registeret
    AGR, //Ajourhold - Grunnopplysninger
    BAR, //Barnetrygd
    BID, //Bidrag
    BIL, //Bil
    DAG, //Dagpenger
    ENF, //Enslig forsørger
    ERS, //Erstatning
    FAR, //Farskap
    FEI, //Feilutbetaling
    FOR, //Foreldre- og svangerskapspenger
    FOS, //Forsikring
    FRI, //Kompensasjon for selvstendig næringsdrivende/frilansere
    FUL, //Fullmakt
    GEN, //Generell
    GRA, //Gravferdsstønad
    GRU, //Grunn- og hjelpestønad
    HEL, //Helsetjenester og ortopediske hjelpemidler
    HJE, //Hjelpemidler
    IAR, //Inkluderende arbeidsliv
    IND, //Tiltakspenger
    KON, //Kontantstøtte
    KTR, //Kontroll
    MED, //Medlemskap
    MOB, //Mobilitetsfremmende stønad
    OMS, //Omsorgspenger, pleiepenger og opplæringspenger
    OPA, //Oppfølging - Arbeidsgiver
    OPP, //Oppfølging
    PEN, //Pensjon
    PER, //Permittering og masseoppsigelser
    REH, //Rehabilitering
    REK, //Rekruttering og stilling
    RPO, //Retting av personopplysninger
    RVE, //Rettferdsvederlag
    SAA, //Sanksjon - Arbeidsgiver
    SAK, //Saksomkostninger
    SAP, //Sanksjon - Person
    SER, //Serviceklager
    SIK, //Sikkerhetstiltak
    STO, //Regnskap/utbetaling
    SUP, //	Supplerende stønad
    SYK, //Sykepenger
    SYM, //Sykmeldinger
    TIL, //Tiltak
    TRK, //Trekkhåndtering
    TRY, //Trygdeavgift
    TSO, //Tilleggsstønad
    TSR, //Tilleggsstønad arbeidssøkere
    UFM, //Unntak fra medlemskap
    UFO, //Uføretrygd
    UKJ, //Ukjent
    VEN, //Ventelønn
    YRA, //Yrkesrettet attføring
    YRK //Yrkesskade / Menerstatning
}

enum class Journalstatus {
    MOTTATT,
    JOURNALFOERT,
    FERDIGSTILT,
    EKSPEDERT,
    UNDER_ARBEID,
    FEILREGISTRERT,
    UTGAAR,
    AVBRUTT,
    UKJENT_BRUKER,
    RESERVERT,
    OPPLASTING_DOKUMENT,
    UKJENT
}

enum class Journalposttype {
    I, //Inngående dokument: Dokumentasjon som NAV har mottatt fra en ekstern part. De fleste inngående dokumenter er søknader, ettersendelser av dokumentasjon til sak, eller innsendinger fra arbeidsgivere. Meldinger brukere har sendt til "Skriv til NAV" arkiveres også som inngående dokumenter.
    U, //Utgående dokument: Dokumentasjon som NAV har produsert og sendt ut til en ekstern part. De fleste utgående dokumenter er informasjons- eller vedtaksbrev til privatpersoner eller organisasjoner. "Skriv til NAV"-meldinger som saksbehandlere har sendt til brukere arkiveres også som utgående dokumenter.
    N //Notat: Dokumentasjon som NAV har produsert selv, uten at formålet er å distribuere dette ut av NAV. Eksempler på notater er samtalereferater med veileder på kontaktsenter og interne forvaltningsnotater.
}