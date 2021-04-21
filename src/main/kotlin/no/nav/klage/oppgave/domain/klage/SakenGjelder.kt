package no.nav.klage.oppgave.domain.klage

import java.util.*
import javax.persistence.*

@Entity
@Table(name = "saken_gjelder", schema = "klage")
class SakenGjelder(
    @Id
    val id: UUID = UUID.randomUUID(),
    @OneToOne(cascade = [CascadeType.ALL], orphanRemoval = true)
    @JoinColumn(name = "part_id", nullable = false)
    val partId: PartId,
    @Column(name = "skal_motta_kopi")
    val skalMottaKopi: Boolean
) {
    fun erPerson() = partId.type == PartIdType.PERSON

    fun erVirksomhet() = partId.type == PartIdType.VIRKSOMHET

    fun copy() = SakenGjelder(
        partId = this.partId.copy(),
        skalMottaKopi = this.skalMottaKopi
    )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SakenGjelder

        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }
}
