package no.nav.klage.oppgave.domain.klage

import java.util.*
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Table(name = "klage_oppgave", schema = "klage")
class Oppgavereferanse(
    @Id
    private val id: UUID = UUID.randomUUID(),
    @Column(name = "oppgave_id")
    private val oppgaveId: Long
) {
}
