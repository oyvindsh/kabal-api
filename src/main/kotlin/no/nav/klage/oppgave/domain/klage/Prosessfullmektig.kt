package no.nav.klage.oppgave.domain.klage

import java.util.*
import javax.persistence.*

@Entity
@Table(name = "prosessfullmektig", schema = "klage")
class Prosessfullmektig(
    @Id
    val id: UUID = UUID.randomUUID(),
    @OneToOne(cascade = [CascadeType.ALL], orphanRemoval = true)
    @JoinColumn(name = "part_id", nullable = false)
    val partId: PartId,
    @Column(name = "skal_klager_motta_kopi")
    val skalKlagerMottaKopi: Boolean
) {
    fun copy() = Prosessfullmektig(
        partId = this.partId.copy(),
        skalKlagerMottaKopi = this.skalKlagerMottaKopi
    )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Prosessfullmektig

        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }
}
