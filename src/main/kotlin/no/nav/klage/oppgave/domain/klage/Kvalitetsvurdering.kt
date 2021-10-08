package no.nav.klage.oppgave.domain.klage

import no.nav.klage.oppgave.domain.kodeverk.*
import no.nav.klage.oppgave.exceptions.ValidationException
import java.time.LocalDateTime
import java.util.*
import javax.persistence.*

@Entity
@Table(name = "kvalitetsvurdering", schema = "klage")
class Kvalitetsvurdering(
    @Id
    val id: UUID = UUID.randomUUID(),

    @Column(name = "oversendelsesbrev_bra")
    var oversendelsesbrevBra: Boolean? = null,
    @ElementCollection(targetClass = KvalitetsavvikOversendelsesbrev::class, fetch = FetchType.EAGER)
    @CollectionTable(
        name = "kvalitetsvurdering_avvik_oversendelsesbrev",
        schema = "klage",
        joinColumns = [JoinColumn(name = "kvalitetsvurdering_id", referencedColumnName = "id", nullable = false)]
    )
    @Convert(converter = KvalitetsavvikOversendelsesbrevConverter::class)
    @Column(name = "id")
    var kvalitetsavvikOversendelsesbrev: MutableSet<KvalitetsavvikOversendelsesbrev> = mutableSetOf(),
    @Column(name = "kommentar_oversendelsesbrev")
    var kommentarOversendelsesbrev: String? = null,

    @Column(name = "utredning_bra")
    var utredningBra: Boolean? = null,
    @ElementCollection(targetClass = KvalitetsavvikUtredning::class, fetch = FetchType.EAGER)
    @CollectionTable(
        name = "kvalitetsvurdering_avvik_utredning",
        schema = "klage",
        joinColumns = [JoinColumn(name = "kvalitetsvurdering_id", referencedColumnName = "id", nullable = false)]
    )
    @Convert(converter = KvalitetsavvikUtredningConverter::class)
    @Column(name = "id")
    var kvalitetsavvikUtredning: MutableSet<KvalitetsavvikUtredning> = mutableSetOf(),
    @Column(name = "kommentar_utredning")
    var kommentarUtredning: String? = null,

    @Column(name = "vedtak_bra")
    var vedtakBra: Boolean? = null,
    @ElementCollection(targetClass = KvalitetsavvikVedtak::class, fetch = FetchType.EAGER)
    @CollectionTable(
        name = "kvalitetsvurdering_avvik_vedtak",
        schema = "klage",
        joinColumns = [JoinColumn(name = "kvalitetsvurdering_id", referencedColumnName = "id", nullable = false)]
    )
    @Convert(converter = KvalitetsavvikVedtakConverter::class)
    @Column(name = "id")
    var kvalitetsavvikVedtak: MutableSet<KvalitetsavvikVedtak> = mutableSetOf(),
    @Column(name = "kommentar_vedtak")
    var kommentarVedtak: String? = null,

    @Column(name = "avvik_stor_konsekvens")
    var avvikStorKonsekvens: Boolean? = null,

    @Column(name = "bruk_som_eksempel_i_opplaering")
    var brukSomEksempelIOpplaering: Boolean? = null,

    @Column(name = "created")
    val created: LocalDateTime = LocalDateTime.now(),
    @Column(name = "modified")
    var modified: LocalDateTime = LocalDateTime.now()
) {

    override fun toString(): String {
        return "Kvalitetsvurdering(id=$id, " +
                "oversendelsesbrevBra=$oversendelsesbrevBra, " +
                "kvalitetsavvikOversendelsesbrev=$kvalitetsavvikOversendelsesbrev, " +
                "kommentarOversendingsbrev=$kommentarOversendelsesbrev, " +
                "utredningBra=$utredningBra, " +
                "kvalitetsavvikUtredning=$kvalitetsavvikUtredning, " +
                "kommentarUtredning=$kommentarUtredning, " +
                "vedtakBra=$vedtakBra, " +
                "kvalitetsavvikVedtak=$kvalitetsavvikVedtak, " +
                "kommentarVedtak=$kommentarVedtak, " +
                "avvikStorKonsekvens=$avvikStorKonsekvens, " +
                "brukSomEksempelIOpplaering=$brukSomEksempelIOpplaering, " +
                "created=$created, " +
                "modified=$modified)"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Kvalitetsvurdering

        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }

    fun validate() {
        validateBooleanAndAvviklistSize(oversendelsesbrevBra, kvalitetsavvikOversendelsesbrev.size)
        validateBooleanAndAvviklistSize(utredningBra, kvalitetsavvikUtredning.size)
        validateBooleanAndAvviklistSize(vedtakBra, kvalitetsavvikVedtak.size)
    }

    private fun validateBooleanAndAvviklistSize(kategoriBra: Boolean?, kategoriSize: Int) {
        if ((kategoriBra == false && kategoriSize == 0) ||
            (kategoriBra == null)
        ) {
            throw ValidationException("Manglende utfylling av kvalitetsskjema.")
        }
    }
}
