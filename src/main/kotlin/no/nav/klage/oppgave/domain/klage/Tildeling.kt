package no.nav.klage.oppgave.domain.klage

import java.time.LocalDateTime
import javax.persistence.Column
import javax.persistence.Embeddable

@Embeddable
data class Tildeling(
    @Column(name = "saksbehandlerident")
    val saksbehandlerident: String? = null,
    @Column(name = "enhet")
    val enhet: String? = null,
    @Column(name = "tidspunkt")
    val tidspunkt: LocalDateTime
)
