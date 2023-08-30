package no.nav.klage.oppgave.api.view

import no.nav.klage.kodeverk.FlowState
import java.time.LocalDateTime

data class MedunderskriverFlowStateResponse (
    val navn: String?,
    val navIdent: String?,
    val modified: LocalDateTime,
    val medunderskriverFlowState: FlowState,
)

data class MedunderskriverWrapped (
    val medunderskriver: SaksbehandlerView?,
    val modified: LocalDateTime,
    val medunderskriverFlowState: FlowState,
)
