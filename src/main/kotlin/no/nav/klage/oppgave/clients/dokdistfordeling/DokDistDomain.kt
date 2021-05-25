package no.nav.klage.oppgave.clients.dokdistfordeling

import java.util.*

data class DistribuerJournalpostRequestTo(
    val batchId: String? = null,
    val bestillendeFagSystem: String? = null,
    val dokumentProdApp: String? = null,
    val journalpostId: String
)

data class DistribuerJournalpostResponse(
    val bestillingsId: UUID
)
