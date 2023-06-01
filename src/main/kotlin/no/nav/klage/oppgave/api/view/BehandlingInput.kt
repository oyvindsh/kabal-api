package no.nav.klage.oppgave.api.view

import java.time.LocalDate
import java.time.LocalDateTime

data class BehandlingDateInput(
    val date: LocalDate
)

data class SattPaaVentInput(
    val start: LocalDate,
    val expires: LocalDate,
    val reason: String
)
