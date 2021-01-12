package no.nav.klage.oppgave.domain.klage

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Table(name = "arbeidsdokument", schema = "klage")
class Arbeidsdokument(
    @Id
    val id: Int,
    @Column(name = "dokument")
    val dokument: ByteArray
) {
    override fun toString(): String {
        return "Mottak(id=$id, " +
                "dokument=${dokument.size})"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Arbeidsdokument

        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }
}
