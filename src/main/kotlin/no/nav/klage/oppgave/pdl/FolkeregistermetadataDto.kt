package no.nav.klage.oppgave.pdl

import java.time.LocalDateTime

data class FolkeregistermetadataDto(
    val ajourholdstidspunkt: LocalDateTime,
    val gyldighetstidspunkt: LocalDateTime,
    val opphoerstidspunkt: LocalDateTime,
    val kilde: String,
    val aarsak: String,
    val sekvens: Int
)