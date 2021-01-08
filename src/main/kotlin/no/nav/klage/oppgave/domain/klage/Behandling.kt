package no.nav.klage.oppgave.domain.klage

import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Table(name = "behandling", schema = "klage")
class Behandling(
    @Id
    val id: UUID,
    @Column(name = "klagesak_id")
    val klagesakId: UUID?,
    @Column(name = "dato_mottatt_fra_foersteinstans")
    val mottatt: LocalDate?,
    @Column(name = "dato_behandling_startet")
    val startet: LocalDate?,
    @Column(name = "dato_behandling_avsluttet")
    val avsluttet: LocalDate?,
    @Column(name = "frist")
    val frist: LocalDate?,
    @Column(name = "tildelt_saksbehandlerident")
    val tildeltSaksbehandlerident: String?,
    @Column(name = "vedtak_id")
    val vedtakId: Int,
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

        other as Behandling

        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }
}
