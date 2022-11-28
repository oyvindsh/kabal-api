package no.nav.klage.oppgave.api.view

data class TildelingEditedView(
    val person: PersonView,
    val fromSaksbehandler: SaksbehandlerView?,
    val toSaksbehandler: SaksbehandlerView?,
) {

    data class PersonView(
        val fnr: String,
        val navn: String?,
    )
}