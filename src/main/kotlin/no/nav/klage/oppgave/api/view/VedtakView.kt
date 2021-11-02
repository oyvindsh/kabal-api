package no.nav.klage.oppgave.api.view

import java.util.*

data class VedtakView(
    val id: UUID,
    val utfall: String? = null,
    val hjemler: Set<String> = setOf(),
    val file: VedleggView? = null
)