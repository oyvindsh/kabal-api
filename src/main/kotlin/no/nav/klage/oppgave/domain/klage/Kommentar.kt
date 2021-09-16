package no.nav.klage.oppgave.domain.klage

import java.time.LocalDateTime
import java.util.*
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Table(name = "kommentar", schema = "klage")
class Kommentar(
    @Id
    val id: UUID = UUID.randomUUID(),
    @Column(name = "kommentar")
    val kommentar: String,
    @Column(name = "saksbehandlerident")
    val saksbehandlerident: String? = null,
    @Column(name = "tidspunkt")
    val tidspunkt: LocalDateTime
) : Comparable<Kommentar> {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Kommentar

        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }

    override fun toString(): String {
        return "Kommentar(id=$id, kommentar=$kommentar, tidspunkt=$tidspunkt)"
    }

    override fun compareTo(other: Kommentar): Int {
        return this.tidspunkt.compareTo(other.tidspunkt)
    }
}
