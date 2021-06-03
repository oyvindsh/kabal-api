package no.nav.klage.oppgave.api.view

import java.time.LocalDate
import java.time.LocalDateTime

data class SendtMedunderskriverView(
    val klagebehandlingVersjon: Long,
    val modified: LocalDateTime,
    val datoSendtMedunderskriver: LocalDate
)
