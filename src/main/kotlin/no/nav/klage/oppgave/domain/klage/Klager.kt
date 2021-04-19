package no.nav.klage.oppgave.domain.klage

import java.util.*
import javax.persistence.*

@Entity
@Table(name = "klager", schema = "klage")
class Klager(
    @Id
    val id: UUID = UUID.randomUUID(),
    @OneToOne(cascade = [CascadeType.ALL], orphanRemoval = true)
    @JoinColumn(name = "part_id", nullable = false)
    val partId: PartId,
    @OneToOne(cascade = [CascadeType.ALL], orphanRemoval = true)
    @JoinColumn(name = "prosessfullmektig_part_id", nullable = true)
    val prosessfullmektig: PartId? = null,
    @Column(name = "skal_motta_kopi")
    val skalMottaKopi: Boolean? = null
) {
    fun erPerson() = partId.type == PartIdType.PERSON

    fun erOrganisasjon() = partId.type == PartIdType.ORGANISASJON

    fun erVirksomhet() = partId.type == PartIdType.VIRKSOMHET

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Klager

        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }
}
