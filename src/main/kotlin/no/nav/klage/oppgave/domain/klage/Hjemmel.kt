package no.nav.klage.oppgave.domain.klage

import java.util.*
import javax.persistence.*

@Entity
@Table(name = "hjemmel", schema = "klage")
class Hjemmel(
    @Id
    val id: UUID = UUID.randomUUID(),
    @Column(name = "lov_id")
    val lovId: Int? = null,
    @Column(name = "kapittel")
    val kapittel: Int? = null,
    @Column(name = "paragraf")
    val paragraf: Int? = null,
    @Column(name = "ledd")
    val ledd: Int? = null,
    @Column(name = "bokstav")
    val bokstav: String? = null,
    @Column(name = "original")
    val original: String
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
