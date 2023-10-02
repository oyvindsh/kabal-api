package no.nav.klage.oppgave.api.view

import java.util.*

data class VedtakView(
    val id: UUID,
    val utfallId: String?,
    val utfallIdSet: Set<String>,
    val hjemmelIdSet: Set<String>,
)