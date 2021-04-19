package no.nav.klage.oppgave.domain.klage

import no.nav.klage.oppgave.domain.kodeverk.*
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*
import javax.persistence.*

@Entity
@Table(name = "mottak", schema = "klage")
class Mottak(
    @Id
    val id: UUID = UUID.randomUUID(),
    @Version
    @Column(name = "versjon")
    val versjon: Long = 1L,
    @Column(name = "tema_id")
    @Convert(converter = TemaConverter::class)
    var tema: Tema,
    @Column(name = "sakstype_id")
    @Convert(converter = SakstypeConverter::class)
    var sakstype: Sakstype,
    @OneToOne(cascade = [CascadeType.ALL], orphanRemoval = true)
    @JoinColumn(name = "klager_id", nullable = false)
    var klager: Klager,
    @OneToOne(cascade = [CascadeType.ALL], orphanRemoval = true)
    @JoinColumn(name = "saken_gjelder_id", nullable = true)
    var sakenGjelder: Klager? = null,
    @Column(name = "sak_referanse")
    var sakReferanse: String? = null,
    @Column(name = "kilde_referanse")
    var kildeReferanse: String,
    @Column(name = "dvh_referanse")
    var dvhReferanse: String? = null,
    @Column(name = "innsyn_url")
    val innsynUrl: String? = null,
    @Column(name = "hjemmel_liste")
    var hjemmelListe: String? = null,
    @Column(name = "avsender_saksbehandlerident")
    var avsenderSaksbehandlerident: String? = null,
    @Column(name = "avsender_enhet")
    var avsenderEnhet: String? = null,
    @Column(name = "oversendt_klageinstans_enhet")
    var oversendtKaEnhet: String? = null,
    @OneToMany(cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "mottak_id", referencedColumnName = "id", nullable = false)
    val mottakDokument: MutableSet<MottakDokument> = mutableSetOf(),
    @OneToMany(cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinTable(
        name = "mottak_brevmottaker",
        schema = "klage",
        joinColumns = [JoinColumn(name = "mottak_id", referencedColumnName = "id")],
        inverseJoinColumns = [JoinColumn(name = "mottaker_part_id", referencedColumnName = "id")]
    )
    val brevmottakere: MutableSet<PartId> = mutableSetOf(),
    @Column(name = "dato_innsendt")
    val innsendtDato: LocalDate? = null,
    @Column(name = "dato_mottatt_foersteinstans")
    val mottattNavDato: LocalDate? = null,
    @Column(name = "dato_oversendt_klageinstans")
    var oversendtKaDato: LocalDate,
    @Column(name = "dato_frist_fra_foersteinstans")
    var fristFraFoersteinstans: LocalDate? = null,
    @Column(name = "created")
    val created: LocalDateTime = LocalDateTime.now(),
    @Column(name = "modified")
    val modified: LocalDateTime = LocalDateTime.now(),
    @Column(name = "kilde")
    val kilde: String,
) {

    fun hjemler(): List<String> = hjemmelListe?.split(",") ?: emptyList()

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
