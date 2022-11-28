package no.nav.klage.oppgave.api.view

import java.time.LocalDateTime

data class SaksbehandlerView(
    val navIdent: String,
    val navn: String
)

data class SaksbehandlerInput(
    val navIdent: String?,
)

data class SaksbehandlerModifiedResponse(
    val navn: String?,
    val navIdent: String?,
    val modified: LocalDateTime,
)