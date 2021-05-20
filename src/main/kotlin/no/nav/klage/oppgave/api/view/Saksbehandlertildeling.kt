package no.nav.klage.oppgave.api.view

data class Saksbehandlertildeling(
    val navIdent: String,
    val enhetId: String?,
    val klagebehandlingVersjon: Long
)

data class Saksbehandlerfradeling(
    val klagebehandlingVersjon: Long
)