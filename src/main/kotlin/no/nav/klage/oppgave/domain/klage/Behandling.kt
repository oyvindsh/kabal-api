package no.nav.klage.oppgave.domain.klage

import no.nav.klage.oppgave.domain.kodeverk.Eoes
import no.nav.klage.oppgave.domain.kodeverk.RaadfoertMedLege
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*
import javax.persistence.*

@Entity
@Table(name = "behandling", schema = "klage")
class Behandling(
    @Id
    val id: UUID = UUID.randomUUID(),
    @Column(name = "dato_mottatt_fra_foersteinstans")
    val mottatt: LocalDate? = null,
    @Column(name = "dato_behandling_startet")
    val startet: LocalDate? = null,
    @Column(name = "dato_behandling_avsluttet")
    val avsluttet: LocalDate? = null,
    @Column(name = "frist")
    val frist: LocalDate? = null,
    @Column(name = "tildelt_saksbehandlerident")
    val tildeltSaksbehandlerident: String? = null,
    @OneToOne
    @JoinColumn(name = "vedtak_id", nullable = true)
    val vedtak: Vedtak? = null,
    @OneToOne
    @JoinColumn(name = "eoes_id")
    val eoes: Eoes? = null,
    @OneToOne
    @JoinColumn(name = "raadfoert_med_lege_id")
    val raadfoertMedLege: RaadfoertMedLege? = null,
    @OneToMany
    @JoinColumn(name = "behandling_id")
    val hjemler: List<Hjemmel>,
    @Column(name = "modified")
    val modified: LocalDateTime = LocalDateTime.now(),
    @Column(name = "created")
    val created: LocalDateTime = LocalDateTime.now()
) {
    override fun toString(): String {
        return "Behandling(id=$id, " +
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
