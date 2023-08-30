package no.nav.klage.oppgave.api.view

import no.nav.klage.kodeverk.FlowState
import java.time.LocalDateTime

data class RolEditedView(
    val navIdent: String?,
    val navn: String?,
    val rolFlowState: FlowState,
    val modified: LocalDateTime,
)

data class Rols(val rols: List<Rol>) {
    data class Rol(val navIdent: String, val navn: String)
}