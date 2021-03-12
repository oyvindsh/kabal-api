package no.nav.klage.oppgave.domain.klage

import no.nav.klage.oppgave.domain.kodeverk.*
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*
import javax.persistence.*

const val KLAGEENHET_PREFIX = "42"

@Entity
@Table(name = "klagebehandling", schema = "klage")
class Klagebehandling(
    @Id
    val id: UUID = UUID.randomUUID(),
    @Version
    @Column(name = "versjon")
    val versjon: Long = 1L,
    @Column(name = "foedselsnummer")
    val foedselsnummer: String? = null,
    @Column(name = "tema_id")
    @Convert(converter = TemaConverter::class)
    val tema: Tema,
    @Column(name = "sakstype_id")
    @Convert(converter = SakstypeConverter::class)
    val sakstype: Sakstype,
    @Column(name = "referanse_id")
    var referanseId: String? = null,
    @Column(name = "dato_innsendt")
    val innsendt: LocalDate? = null,
    @Column(name = "dato_mottatt_foersteinstans")
    val mottattFoersteinstans: LocalDate? = null,
    @Column(name = "avsender_saksbehandlerident_foersteinstans")
    val avsenderSaksbehandleridentFoersteinstans: String? = null,
    @Column(name = "avsender_enhet_foersteinstans")
    val avsenderEnhetFoersteinstans: String? = null,
    @Column(name = " dato_mottatt_klageinstans")
    val mottattKlageinstans: LocalDate,
    @Column(name = "dato_behandling_startet")
    val startet: LocalDate? = null,
    @Column(name = "dato_behandling_avsluttet")
    val avsluttet: LocalDate? = null,
    @Column(name = "frist")
    val frist: LocalDate? = null,
    @Column(name = "tildelt_saksbehandlerident")
    var tildeltSaksbehandlerident: String? = null,
    @Column(name = "tildelt_enhet")
    var tildeltEnhet: String? = null,
    @Column(name = "mottak_id")
    val mottakId: UUID,
    @OneToOne(cascade = [CascadeType.ALL], orphanRemoval = true)
    @JoinColumn(name = "kvalitetsvurdering_id", nullable = true)
    var kvalitetsvurdering: Kvalitetsvurdering? = null,
    @OneToMany(cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinTable(
        name = "klagebehandling_hjemmel",
        schema = "klage",
        joinColumns = [JoinColumn(name = "klagebehandling_id", referencedColumnName = "id")],
        inverseJoinColumns = [JoinColumn(name = "hjemmel_id", referencedColumnName = "id")]
    )
    val hjemler: MutableSet<Hjemmel> = mutableSetOf(),
    @OneToMany(cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "klagebehandling_id", referencedColumnName = "id", nullable = false)
    val vedtak: MutableSet<Vedtak> = mutableSetOf(),
    @OneToMany(cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "klagebehandling_id", referencedColumnName = "id", nullable = false)
    val saksdokumenter: MutableSet<Saksdokument> = mutableSetOf(),
    @Column(name = "created")
    val created: LocalDateTime = LocalDateTime.now(),
    @Column(name = "modified")
    var modified: LocalDateTime = LocalDateTime.now(),
    @Column(name = "kilde")
    @Enumerated(EnumType.STRING)
    val kilde: Kilde
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
