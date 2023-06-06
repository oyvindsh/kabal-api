package no.nav.klage.oppgave.api.view

import java.util.*

data class VedtakView(
    val id: UUID,
    val utfallId: String? = null,
    val hjemmelIdSet: Set<String> = setOf(),
)