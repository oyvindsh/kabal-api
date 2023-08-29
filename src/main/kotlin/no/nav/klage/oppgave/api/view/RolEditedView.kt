package no.nav.klage.oppgave.api.view

import java.time.LocalDateTime

data class RolEditedView(
    val navIdent: String?,
    val navn: String?,
    val rolStateId: String?,
    val modified: LocalDateTime,
)
