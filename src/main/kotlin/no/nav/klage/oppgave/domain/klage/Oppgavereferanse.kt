package no.nav.klage.oppgave.domain.klage

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.util.*

@Entity
@Table(name = "mottak_oppgave", schema = "klage")
class Oppgavereferanse(
    @Id
    val id: UUID = UUID.randomUUID(),
    @Column(name = "oppgave_id")
    val oppgaveId: Long
)
