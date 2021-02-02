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
    POL,
    FEIL
}

enum class Variantformat {
    ARKIV,
    SLADDET,
    PRODUKSJON,
    PRODUKSJON_DLF,
    FULLVERSJON,
    ORIGINAL
}

data class Sak(val datoOpprettet: LocalDateTime?, val fagsakId: String?, val fagsaksystem: String?)

enum class Tema {
    AAP,
    AAR,
    AGR,
    BAR,
    BID,
    BIL,
    DAG,
    ENF,
    ERS,
    FAR,
    FEI,
    FOR,
    FOS,
    FRI,
    FUL,
    GEN,
    GRA,
    GRU,
    HEL,
    HJE,
    IAR,
    IND,
    KON,
    KTR,
    MED,
    MOB,
    OMS,
    OPA,
    OPP,
    PEN,
    PER,
    REH,
    REK,
    RPO,
    RVE,
    SAA,
    SAK,
    SAP,
    SER,
    SIK,
    STO,
    SUP,
    SYK,
    SYM,
    TIL,
    TRK,
    TRY,
    TSO,
    TSR,
    UFM,
    UFO,
    UKJ,
    VEN,
    YRA,
    YRK,
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
    I,
    U,
    N
}