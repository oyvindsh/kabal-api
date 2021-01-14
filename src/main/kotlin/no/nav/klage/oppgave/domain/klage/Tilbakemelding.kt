package no.nav.klage.oppgave.domain.klage

import java.time.LocalDateTime
import java.util.*
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Table(name = "tilbakemelding", schema = "klage")
class Tilbakemelding(
    @Id
    val id: UUID = UUID.randomUUID(),
    @Column(name = "mottaker_saksbehandlerident")
    val mottakerSaksbehandlerident: String? = null,
    @Column(name = "tilbakemelding")
    val tilbakemelding: String,
    @Column(name = "modified")
    val modified: LocalDateTime,
    @Column(name = "created")
    val created: LocalDateTime
) {
    override fun toString(): String {
        return "Tilbakemelding(id=$id, " +
                "modified=$modified, " +
                "created=$created)"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Tilbakemelding

        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }
}
