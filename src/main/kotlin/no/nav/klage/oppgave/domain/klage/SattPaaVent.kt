package no.nav.klage.oppgave.domain.klage

import jakarta.persistence.Embeddable
import java.time.LocalDate

@Embeddable
data class SattPaaVent(
    val start: LocalDate,
    val expires: LocalDate,
    val reason: String,
)