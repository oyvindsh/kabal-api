package no.nav.klage.oppgave.domain.klage

import jakarta.persistence.Column
import jakarta.persistence.Convert
import jakarta.persistence.Embeddable
import no.nav.klage.kodeverk.Fagsystem
import java.time.LocalDateTime

@Embeddable
data class Feilregistrering(
    @Column(name = "nav_ident")
    val navIdent: String,
    val registered: LocalDateTime,
    val reason: String,
    @Column(name = "fagsystem_id")
    @Convert(converter = FagsystemConverter::class)
    val fagsystem: Fagsystem
)