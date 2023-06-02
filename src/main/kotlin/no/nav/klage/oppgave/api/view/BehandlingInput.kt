package no.nav.klage.oppgave.api.view

import java.time.LocalDate

data class BehandlingDateInput(
    val date: LocalDate
)

data class SattPaaVentInput(
    val expires: LocalDate,
    val reason: String
)
