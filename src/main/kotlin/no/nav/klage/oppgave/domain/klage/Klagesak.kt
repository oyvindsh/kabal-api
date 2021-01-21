package no.nav.klage.oppgave.domain.klage

import no.nav.klage.oppgave.domain.kodeverk.Sakstype
import java.time.LocalDateTime
import java.util.*
import javax.persistence.*

@Entity
@Table(name = "klagesak", schema = "klage")
class Klagesak(

    @Id
    val id: UUID = UUID.randomUUID(),
    @OneToMany(mappedBy = "behandling", cascade = [CascadeType.ALL])
    val behandlinger: List<Behandling>,
    @Column(name = "foedselsnummer")
    val foedselsnummer: String,
    @OneToOne
    @JoinColumn(name = "sakstype_id")
    val sakstype: Sakstype,
    @Column(name = "modified")
    val modified: LocalDateTime = LocalDateTime.now(),
    @Column(name = "created")
    val created: LocalDateTime = LocalDateTime.now()
) {

    override fun toString(): String {
        return "Klagesak(id=$id, " +
                "modified=$modified, " +
                "created=$created)"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Klagesak

        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }
}
