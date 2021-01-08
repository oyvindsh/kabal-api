package no.nav.klage.oppgave.domain.klage

import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*
import javax.persistence.*

@Entity
@Table(name = "behandling", schema = "klage")
class Behandling(
    @Id
    val id: UUID,
    @ManyToOne
    @JoinColumn(name = "klagesak_id", nullable = false)
    val klagesak: Klagesak,
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
    @OneToOne
    @JoinColumn(name = "vedtak_id", nullable = true)
    val vedtak: Vedtak?,
    @ManyToMany
    @JoinTable(
        name = "behandling_hjemmel", schema = "klage",
        joinColumns = [JoinColumn(name = "behandling_id")],
        inverseJoinColumns = [JoinColumn(name = "hjemmel_id")]
    )
    val hjemler: List<Hjemmel>,
    @ManyToMany
    @JoinTable(
        name = "behandling_dokument", schema = "klage",
        joinColumns = [JoinColumn(name = "behandling_id")],
        inverseJoinColumns = [JoinColumn(name = "dokument_id")]
    )
    val dokumenter: List<Dokument>,
    @OneToMany
    @JoinColumn(name = "behandling_id")
    val logg: List<Behandlingslogg>,
    @Column(name = "modified")
    val modified: LocalDateTime,
    @Column(name = "created")
    val created: LocalDateTime
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
