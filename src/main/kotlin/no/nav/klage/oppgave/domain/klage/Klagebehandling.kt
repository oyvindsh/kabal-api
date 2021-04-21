package no.nav.klage.oppgave.domain.klage

import no.nav.klage.oppgave.domain.kodeverk.*
import no.nav.klage.oppgave.exceptions.KlagebehandlingSamtidigEndretException
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
    @OneToOne(cascade = [CascadeType.ALL], orphanRemoval = true)
    @JoinColumn(name = "klager_id", nullable = false)
    var klager: Klager,
    @OneToOne(cascade = [CascadeType.ALL], orphanRemoval = true)
    @JoinColumn(name = "saken_gjelder_id", nullable = false)
    var sakenGjelder: SakenGjelder,
    @Column(name = "tema_id")
    @Convert(converter = TemaConverter::class)
    var tema: Tema,
    @Column(name = "sakstype_id")
    @Convert(converter = SakstypeConverter::class)
    var sakstype: Sakstype,
    @Column(name = "referanse_id")
    var referanseId: String? = null,
    @Column(name = "dato_innsendt")
    var innsendt: LocalDate? = null,
    @Column(name = "dato_mottatt_foersteinstans")
    var mottattFoersteinstans: LocalDate? = null,
    @Column(name = "avsender_saksbehandlerident_foersteinstans")
    var avsenderSaksbehandleridentFoersteinstans: String? = null,
    @Column(name = "avsender_enhet_foersteinstans")
    var avsenderEnhetFoersteinstans: String? = null,
    @Column(name = " dato_mottatt_klageinstans")
    var mottattKlageinstans: LocalDate,
    @Column(name = "dato_behandling_startet")
    var startet: LocalDate? = null,
    @Column(name = "dato_behandling_avsluttet")
    var avsluttet: LocalDate? = null,
    @Column(name = "frist")
    var frist: LocalDate? = null,
    @Column(name = "tildelt_saksbehandlerident")
    var tildeltSaksbehandlerident: String? = null,
    @Column(name = "medunderskriverident")
    var medunderskriverident: String? = null,
    @Column(name = "tildelt_enhet")
    var tildeltEnhet: String? = null,
    @Column(name = "mottak_id")
    val mottakId: UUID,
    @OneToOne(cascade = [CascadeType.ALL], orphanRemoval = true)
    @JoinColumn(name = "kvalitetsvurdering_id", nullable = true)
    var kvalitetsvurdering: Kvalitetsvurdering? = null,
    @ElementCollection(targetClass = Hjemmel::class, fetch = FetchType.EAGER)
    @CollectionTable(
        name = "klagebehandling_hjemmel",
        schema = "klage",
        joinColumns = [JoinColumn(name = "klagebehandling_id", referencedColumnName = "id", nullable = false)]
    )
    @Convert(converter = HjemmelConverter::class)
    @Column(name = "id")
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
    val kilde: String
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

    fun checkOptimisticLocking(klagebehandlingVersjon: Long?) {
        if (klagebehandlingVersjon != null) {
            if (klagebehandlingVersjon != this.versjon) throw KlagebehandlingSamtidigEndretException("Angitt versjon er $klagebehandlingVersjon, men nyeste klagebehandling har versjon ${this.versjon}")
        }

    }
}
