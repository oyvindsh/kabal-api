package no.nav.klage.oppgave.api.view

import java.time.LocalDate
import java.time.LocalDateTime

data class TildelingEditedView(val modified: LocalDateTime, val tildelt: LocalDate)