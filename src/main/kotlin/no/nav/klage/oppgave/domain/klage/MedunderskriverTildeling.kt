package no.nav.klage.oppgave.domain.klage

import java.time.LocalDateTime
import javax.persistence.Column
import javax.persistence.Embeddable

@Embeddable
data class MedunderskriverTildeling(
    @Column(name = "saksbehandlerident")
    val saksbehandlerident: String? = null,
    @Column(name = "tidspunkt")
    val tidspunkt: LocalDateTime
)
