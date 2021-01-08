package no.nav.klage.oppgave.domain.klage

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Table(name = "dokument", schema = "klage")
class Dokument(
    @Id
    val id: Int,
    @Column(name = "referanse")
    val referanse: String
) {
    override fun toString(): String {
        return "Mottak(id=$id, " +
                "referanse=$referanse)"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Dokument

        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }
}
