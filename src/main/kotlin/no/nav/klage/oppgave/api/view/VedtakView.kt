package no.nav.klage.oppgave.api.view

import no.nav.klage.oppgave.domain.klage.PartId
import java.time.LocalDateTime
import java.util.*

data class VedtakView(
    val id: UUID,
    val utfall: String? = null,
    val grunn: String? = null,
    val hjemler: Set<String> = setOf(),
    val brevMottakere: Set<PartId> = setOf(), //TODO: Er ikke noe happy med at vi eksponerer en Entity her, burde mappet det til noe annet
    val finalized: LocalDateTime? = null,
    val content: String? = null
)
