package no.nav.klage.oppgave.api.view

data class SakenGjelderWrapped(
    val sakenGjelder: PersonView
) {
    data class PersonView(
        val fnr: String,
        val navn: String?
    )
}