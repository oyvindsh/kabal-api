package no.nav.klage.oppgave.clients.saf.graphql

data class HentJournalpostGraphqlQuery(
    val query: String,
    val variables: JournalpostVariables
)

data class JournalpostVariables(val journalpostId: String)

fun hentJournalpostQuery(
    journalpostId: String
): HentJournalpostGraphqlQuery {
    val query =
        HentJournalpostGraphqlQuery::class.java.getResource("/saf/hentJournalpost.graphql")
            .readText().replace("[\n\r]", "")
    return HentJournalpostGraphqlQuery(query, JournalpostVariables(journalpostId))
}