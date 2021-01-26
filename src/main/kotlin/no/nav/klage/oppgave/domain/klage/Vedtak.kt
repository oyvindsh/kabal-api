package no.nav.klage.oppgave.domain.klage

import no.nav.klage.oppgave.domain.kodeverk.Grunn
import no.nav.klage.oppgave.domain.kodeverk.Utfall
import java.time.LocalDateTime
import java.util.*
import javax.persistence.*

@Entity
@Table(name = "vedtak", schema = "klage")
class Vedtak(
    @Id
    val id: UUID = UUID.randomUUID(),
    @Column(name = "enhet")
    val enhet: Int,
    @OneToOne
    @JoinColumn(name = "utfall_id")
    val utfall: Utfall,
    @OneToOne
    @JoinColumn(name = "grunn_id")
    val grunn: Grunn,
    @OneToOne
    @JoinColumn(name = "tilbakemelding_id", nullable = true)
    val tilbakemelding: Tilbakemelding? = null,
    @Column(name = "modified")
    val modified: LocalDateTime,
    @Column(name = "created")
    val created: LocalDateTime
) {
    override fun toString(): String {
        return "Vedtak(id=$id, " +
                "modified=$modified, " +
                "created=$created)"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Vedtak

        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }
}
