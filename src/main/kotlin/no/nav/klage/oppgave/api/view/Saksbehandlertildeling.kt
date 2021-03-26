package no.nav.klage.oppgave.api.view

data class Saksbehandlertildeling(
    val navIdent: String,
    val oppgaveversjon: String?,
    val klagebehandlingVersjon: Long? = null
)

data class Saksbehandlerfradeling(
    val oppgaveversjon: String?,
    val klagebehandlingVersjon: Long? = null
)