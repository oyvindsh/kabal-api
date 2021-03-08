package no.nav.klage.oppgave.domain.klage

import no.nav.klage.oppgave.domain.kodeverk.Utfall
import no.nav.klage.oppgave.domain.kodeverk.UtfallConverter
import java.time.LocalDateTime
import java.util.*
import javax.persistence.*

@Entity
@Table(name = "vedtak", schema = "klage")
class Vedtak(
    @Id
    val id: UUID = UUID.randomUUID(),
    @Column(name = "utfall_id")
    @Convert(converter = UtfallConverter::class)
    val utfall: Utfall,
    @OneToMany(cascade = [CascadeType.ALL])
    @JoinTable(
        name = "vedtak_hjemmel",
        schema = "klage",
        joinColumns = [JoinColumn(name = "vedtak_id", referencedColumnName = "id")],
        inverseJoinColumns = [JoinColumn(name = "hjemmel_id", referencedColumnName = "id")]
    )
    val hjemler: MutableSet<Hjemmel> = mutableSetOf(),
    @Column(name = "modified")
    val modified: LocalDateTime = LocalDateTime.now(),
    @Column(name = "created")
    val created: LocalDateTime = LocalDateTime.now()
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
