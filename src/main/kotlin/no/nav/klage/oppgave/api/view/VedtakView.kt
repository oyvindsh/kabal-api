package no.nav.klage.oppgave.api.view

import java.util.*

data class VedtakView(
    val id: UUID,
    val utfallIdSet: Set<String>,
    val hjemmelIdSet: Set<String>,
)