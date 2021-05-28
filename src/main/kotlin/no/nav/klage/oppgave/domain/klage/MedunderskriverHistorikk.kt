package no.nav.klage.oppgave.domain.klage

import java.util.*
import javax.persistence.Embedded
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Table(name = "medunderskriverhistorikk", schema = "klage")
class MedunderskriverHistorikk(
    @Id
    val id: UUID = UUID.randomUUID(),
    @Embedded
    val medunderskriver: Tildeling
) : Comparable<MedunderskriverHistorikk> {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as MedunderskriverHistorikk

        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }

    override fun toString(): String {
        return "MedunderskriverHistorikk(id=$id, medunderskriver=$medunderskriver)"
    }

    override fun compareTo(other: MedunderskriverHistorikk): Int {
        return this.medunderskriver.tidspunkt.compareTo(other.medunderskriver.tidspunkt)
    }
}
