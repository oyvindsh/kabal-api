package no.nav.klage.oppgave.domain.klage

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Table(name = "hjemmel", schema = "klage")
class Hjemmel(
    @Id
    val id: Int,
    @Column(name = "lov_id")
    val lovId: Int?,
    @Column(name = "kapittel")
    val kapittel: Int?,
    @Column(name = "paragraf")
    val paragraf: String,
    @Column(name = "ledd")
    val ledd: String?,
    @Column(name = "bokstav")
    val bokstav: String?
) {
    override fun toString(): String {
        return "Hjemmel(id=$id, " +
                "lov=$lovId, " +
                "kapittel=$kapittel, " +
                "paragraf=$paragraf, " +
                "ledd=$ledd ," +
                "bokstav=$bokstav)"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Hjemmel

        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }
}
