package no.nav.klage.oppgave.domain.klage

import java.time.LocalDateTime
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Table(name = "behandlingslogg", schema = "klage")
class Behandlingslogg(
    @Id
    val id: Int,
    @Column(name = "behandling_id")
    val behandlingId: Int,
    @Column(name = "created")
    val created: LocalDateTime,
    @Column(name = "beskrivelse")
    val beskrivelse: String
) {
    override fun toString(): String {
        return "Behandlingslogg(id=$id, " +
                "created=$created)"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Behandlingslogg

        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }
}
