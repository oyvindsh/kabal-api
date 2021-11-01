package no.nav.klage.oppgave.api.view

import java.time.LocalDateTime
import java.util.*

data class VedtakView(
    val id: UUID,
    val utfall: String? = null,
    val hjemler: Set<String> = setOf(),
    val file: VedleggView? = null,
    val opplastet: LocalDateTime? = null,
)