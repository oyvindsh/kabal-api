package no.nav.klage.oppgave.domain.klage

import java.time.LocalDateTime
import java.util.*
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Table(name = "mottak", schema = "klage")
class Mottak(
    @Id
    val id: UUID = UUID.randomUUID(),
    @Column(name = "referanse_id")
    val referanseId: String,
    @Column(name = "foedselsnummer")
    val foedselsnummer: String,
    @Column(name = "hjemmel_liste")
    val hjemmelListe: String?,
    @Column(name = "avsender_enhet")
    val avsenderEnhet: Int,
    @Column(name = "avsender_saksbehandler")
    val avsenderSaksbehandler: String,
    @Column(name = "tema")
    val tema: String,
    @Column(name = "innsyn_url")
    val innsynUrl: String? = null,
    @Column(name = "created")
    val created: LocalDateTime
) {

    fun hjemler(): List<String> = hjemmelListe?.split(",") ?: listOf()

    override fun toString(): String {
        return "Mottak(id=$id, " +
                "created=$created)"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Mottak

        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }

}
