package no.nav.klage.oppgave.domain.klage

import jakarta.persistence.Embeddable
import java.time.LocalDateTime

@Embeddable
data class Tildeling(
    val saksbehandlerident: String? = null,
    val enhet: String? = null,
    val tidspunkt: LocalDateTime,
)
