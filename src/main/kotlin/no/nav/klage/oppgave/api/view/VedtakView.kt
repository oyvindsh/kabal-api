package no.nav.klage.oppgave.api.view

import java.util.*

data class VedtakView(
    val id: UUID,
    val utfall: String? = null,
    val grunn: String? = null,
    val hjemler: Set<String> = setOf(),
    val brevMottakere: Set<BrevMottakerView> = setOf(),
    val content: String? = null
)

data class BrevMottakerView(
    val type: String,
    val id: String,
    val rolle: String
)