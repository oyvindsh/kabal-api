package no.nav.klage.oppgave.domain.klage

import no.nav.klage.oppgave.domain.kodeverk.*
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*
import javax.persistence.*

@Entity
@Table(name = "klagebehandling", schema = "klage")
class Klagebehandling(
    @Id
    val id: UUID = UUID.randomUUID(),
    @Column(name = "foedselsnummer")
    val foedselsnummer: String,
    @Column(name = "tema")
    val tema: String,
    @Column(name = "sakstype_id")
    @Convert(converter = SakstypeConverter::class)
    val sakstype: Sakstype,
    @Column(name = "dato_mottatt_fra_foersteinstans")
    val mottatt: LocalDate = LocalDate.now(),
    @Column(name = "dato_behandling_startet")
    val startet: LocalDate? = null,
    @Column(name = "dato_behandling_avsluttet")
    val avsluttet: LocalDate? = null,
    @Column(name = "frist")
    val frist: LocalDate,
    @Column(name = "tildelt_saksbehandlerident")
    val tildeltSaksbehandlerident: String? = null,
    @OneToOne
    @JoinColumn(name = "vedtak_id", nullable = true)
    val vedtak: Vedtak? = null,
    @Column(name = "eoes_id")
    @Convert(converter = EoesConverter::class)
    val eoes: Eoes? = null,
    @Column(name = "raadfoert_med_lege_id")
    @Convert(converter = RaadfoertMedLegeConverter::class)
    val raadfoertMedLege: RaadfoertMedLege? = null,
    @OneToMany(cascade = [CascadeType.ALL], orphanRemoval = true)
    @JoinColumn(name = "klagebehandling_id", referencedColumnName = "id", nullable = false)
    val hjemler: MutableList<Hjemmel>,
    @OneToMany(cascade = [CascadeType.ALL])
    @JoinColumn(name = "klagebehandling_id", referencedColumnName = "id", nullable = false)
    val oppgavereferanser: MutableList<Oppgavereferanse>,
    @Column(name = "modified")
    val modified: LocalDateTime = LocalDateTime.now(),
    @Column(name = "created")
    val created: LocalDateTime = LocalDateTime.now(),
    @OneToMany(cascade = [CascadeType.ALL], orphanRemoval = true)
    @JoinColumn(name = "klagebehandling_id", referencedColumnName = "id", nullable = false)
    val saksdokumenter: MutableList<Saksdokument> = mutableListOf()
) {
    override fun toString(): String {
        return "Behandling(id=$id, " +
                "modified=$modified, " +
                "created=$created)"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Klagebehandling

        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }
}
