package no.nav.klage.oppgave.clients.saf.graphql

data class HentDokumentoversiktBrukerGraphqlQuery(
    val query: String,
    val variables: DokumentoversiktBrukerVariables
)

data class DokumentoversiktBrukerVariables(
    val brukerId: BrukerId,
    val tema: List<Tema>?,
    val foerste: Int,
    val etter: String?,
)

data class HentJournalpostIdListForBrukerQuery(
    val query: String,
    val variables: JournalpostIdListForBrukerVariables
)

data class JournalpostIdListForBrukerVariables(
    val brukerId: BrukerId,
    val foerste: Int,
    val etter: String?,
    val tema: List<Tema>?,
    val journalposttyper: List<Journalposttype>?,
)

data class BrukerId(val id: String, val type: BrukerIdType = BrukerIdType.FNR)
enum class BrukerIdType { FNR }

fun hentDokumentoversiktBrukerQuery(
    fnr: String,
    tema: List<Tema>?, //Hvis en tom liste er angitt som argument hentes journalposter på alle tema.
    pageSize: Int,
    previousPageRef: String?
): HentDokumentoversiktBrukerGraphqlQuery {
    val journalpostProperties = HentJournalpostGraphqlQuery::class.java.getResource("/saf/journalpostProperties.txt")
        .readText()
    val query =
        HentDokumentoversiktBrukerGraphqlQuery::class.java.getResource("/saf/hentDokumentoversiktBruker.graphql")
            .readText()
            .replace("<replace>", journalpostProperties)
            .replace("[\n\r]", "")
    return HentDokumentoversiktBrukerGraphqlQuery(
        query,
        DokumentoversiktBrukerVariables(
            brukerId = BrukerId(fnr),
            tema = if (tema.isNullOrEmpty()) null else tema,
            foerste = pageSize,
            etter = previousPageRef
        )
    )
}

fun hentJournalpostIdListForBrukerQuery(
    fnr: String,
    pageSize: Int,
    previousPageRef: String?,
    tema: List<Tema>,
    journalposttyper: List<Journalposttype>,
): HentJournalpostIdListForBrukerQuery {
    val journalpostProperties = HentJournalpostGraphqlQuery::class.java.getResource("/saf/journalpostId.txt")
        .readText()
    val query =
        HentJournalpostIdListForBrukerQuery::class.java.getResource("/saf/getJournalpostIdListForBruker.graphql")
            .readText()
            .replace("<replace>", journalpostProperties)
            .replace("[\n\r]", "")
    return HentJournalpostIdListForBrukerQuery(
        query,
        JournalpostIdListForBrukerVariables(
            brukerId = BrukerId(fnr),
            tema = if (tema.isNullOrEmpty()) null else tema,
            journalposttyper = if (journalposttyper.isNullOrEmpty()) null else journalposttyper,
            foerste = pageSize,
            etter = previousPageRef
        )
    )
}