package no.nav.klage.oppgave.domain.klage

import java.util.*
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Table(name = "saksdokument", schema = "klage")
class Saksdokument(
    @Id
    val id: UUID = UUID.randomUUID(),
    @Column(name = "journalpost_id")
    val journalpostId: String
) {
    override fun toString(): String {
        return "Mottak(id=$id, journalpostId=$journalpostId)"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Saksdokument

        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }
}
