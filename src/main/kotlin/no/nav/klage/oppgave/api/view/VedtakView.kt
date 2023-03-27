package no.nav.klage.oppgave.api.view

import java.util.*

data class VedtakView(
    val id: UUID,
    val utfall: String? = null,
    val utfallId: String? = null,
    val hjemler: Set<String> = setOf(),
    val hjemmelIdSet: Set<String> = setOf(),
)