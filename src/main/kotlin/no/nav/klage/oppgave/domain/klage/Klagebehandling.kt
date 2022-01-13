package no.nav.klage.oppgave.domain.klage

import no.nav.klage.kodeverk.*
import no.nav.klage.kodeverk.hjemmel.Hjemmel
import no.nav.klage.kodeverk.hjemmel.HjemmelConverter
import no.nav.klage.oppgave.domain.klage.Klagebehandling.Status.*
import org.hibernate.annotations.BatchSize
import org.hibernate.annotations.Fetch
import org.hibernate.annotations.FetchMode
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
    @Embedded
    var klager: Klager,
    @Embedded
    var sakenGjelder: SakenGjelder,
    @Column(name = "ytelse_id")
    @Convert(converter = YtelseConverter::class)
    val ytelse: Ytelse,
    @Column(name = "type_id")
    @Convert(converter = TypeConverter::class)
    var type: Type,
    @Column(name = "kilde_referanse")
    val kildeReferanse: String,
    @Column(name = "dvh_referanse")
    val dvhReferanse: String? = null,
    @Column(name = "sak_fagsystem")
    @Convert(converter = FagsystemConverter::class)
    val sakFagsystem: Fagsystem? = null,
    @Column(name = "sak_fagsak_id")
    val sakFagsakId: String? = null,
    //Umulig å vite innsendt-dato.
    @Column(name = "dato_innsendt")
    val innsendt: LocalDate? = null,
    //Brukes ikke i anke
    @Column(name = "dato_mottatt_foersteinstans")
    val mottattFoersteinstans: LocalDate,

    //Mulig at identen ikke brukes. Sjekk om dette kan droppes.
    @Column(name = "avsender_saksbehandlerident_foersteinstans")
    val avsenderSaksbehandleridentFoersteinstans: String? = null,
    //Vises i GUI.
    @Column(name = "avsender_enhet_foersteinstans")
    val avsenderEnhetFoersteinstans: String,

    //Settes automatisk i klage, må kunne justeres i anke. Bør også representeres i delbehandling. Må gjøres entydig i anke, hører antageligvis ikke hjemme i felles klasse.
    @Column(name = "dato_mottatt_klageinstans")
    val mottattKlageinstans: LocalDateTime,

    //Teknisk avsluttet, når alle prosesser er gjennomførte. Bør muligens heller utledes av status på delbehandlinger.
    @Column(name = "dato_behandling_avsluttet")
    var avsluttet: LocalDateTime? = null,

    //Bør være i delbehandling
    @Column(name = "dato_behandling_avsluttet_av_saksbehandler")
    var avsluttetAvSaksbehandler: LocalDateTime? = null,

    //TODO: Trenger denne være nullable? Den blir da alltid satt i createKlagebehandlingFromMottak?
    //Litt usikkert om dette hører mest hjemme her eller på delbehandling.
    @Column(name = "frist")
    var frist: LocalDate? = null,

    //Hører hjemme på delbehandling
    @Embedded
    @AttributeOverrides(
        value = [
            AttributeOverride(name = "saksbehandlerident", column = Column(name = "medunderskriverident")),
            AttributeOverride(name = "tidspunkt", column = Column(name = "dato_sendt_medunderskriver"))
        ]
    )
    var medunderskriver: MedunderskriverTildeling? = null,
    //Hører hjemme på delbehandling
    @Column(name = "medunderskriverflyt_id")
    @Convert(converter = MedunderskriverflytConverter::class)
    var medunderskriverFlyt: MedunderskriverFlyt = MedunderskriverFlyt.IKKE_SENDT,
    //Hører hjemme på delbehandling, men her er det mer usikkerhet enn for medunderskriver. Litt om pragmatikken, bør se hva som er enklest å få til.
    @Embedded
    @AttributeOverrides(
        value = [
            AttributeOverride(name = "saksbehandlerident", column = Column(name = "tildelt_saksbehandlerident")),
            AttributeOverride(name = "enhet", column = Column(name = "tildelt_enhet")),
            AttributeOverride(name = "tidspunkt", column = Column(name = "dato_behandling_tildelt"))
        ]
    )
    var tildeling: Tildeling? = null,
    //Hører hjemme på delbehandling, men her er det mer usikkerhet enn for medunderskriver
    @OneToMany(cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "klagebehandling_id", referencedColumnName = "id", nullable = false)
    @Fetch(FetchMode.SELECT)
    @BatchSize(size = 100)
    val tildelingHistorikk: MutableSet<TildelingHistorikk> = mutableSetOf(),
    //Hører hjemme på delbehandling
    @OneToMany(cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "klagebehandling_id", referencedColumnName = "id", nullable = false)
    @Fetch(FetchMode.SELECT)
    @BatchSize(size = 100)
    val medunderskriverHistorikk: MutableSet<MedunderskriverHistorikk> = mutableSetOf(),
    //Hovedbehandling
    @Column(name = "mottak_id")
    val mottakId: UUID,
    //Skal være en kvalitetsvurdering per hovedbehandling, derfor er dette riktig sted.
    @Column(name = "kaka_kvalitetsvurdering_id", nullable = true)
    var kakaKvalitetsvurderingId: UUID? = null,
    //Dette er søkehjemler, input fra førsteinstans. For anker bør vel dette være samme detaljnivået som i registreringshjemler? Blir det da ulike typer for klage og anke? Må ta diskusjonen videre.
    @ElementCollection(targetClass = Hjemmel::class, fetch = FetchType.EAGER)
    @CollectionTable(
        name = "klagebehandling_hjemmel",
        schema = "klage",
        joinColumns = [JoinColumn(name = "klagebehandling_id", referencedColumnName = "id", nullable = false)]
    )
    @Convert(converter = HjemmelConverter::class)
    @Column(name = "id")
    val hjemler: MutableSet<Hjemmel> = mutableSetOf(),
    //Her går vi mot en løsning der en behandling har flere delbehandlinger, som nok er bedre begrep enn vedtak.
    //Trenger en markering av hvilken delbehandling som er den gjeldende.
    @OneToOne(cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "vedtak_id", referencedColumnName = "id")
    val vedtak: Vedtak,
    //Liste med dokumenter fra Joark. De dokumentene saksbehandler krysser av for havner her. Bør være i delbehandling. Kopierer fra forrige når ny delbehandling opprettes.
    @OneToMany(cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "klagebehandling_id", referencedColumnName = "id", nullable = false)
    @Fetch(FetchMode.SELECT)
    @BatchSize(size = 100)
    val saksdokumenter: MutableSet<Saksdokument> = mutableSetOf(),
    @Column(name = "created")
    val created: LocalDateTime = LocalDateTime.now(),
    @Column(name = "modified")
    var modified: LocalDateTime = LocalDateTime.now(),
    //Kommer fra innsending
    @Column(name = "kildesystem")
    @Convert(converter = FagsystemConverter::class)
    val kildesystem: Fagsystem,
    //Kommer fra innsending
    @Column(name = "kommentar_fra_foersteinstans")
    val kommentarFraFoersteinstans: String? = null
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

    /**
     * Brukes til ES og statistikk per nå
     */
    fun getStatus(): Status {
        return when {
            avsluttet != null -> FULLFOERT
            avsluttetAvSaksbehandler != null -> AVSLUTTET_AV_SAKSBEHANDLER
            medunderskriverFlyt == MedunderskriverFlyt.OVERSENDT_TIL_MEDUNDERSKRIVER -> SENDT_TIL_MEDUNDERSKRIVER
            medunderskriverFlyt == MedunderskriverFlyt.RETURNERT_TIL_SAKSBEHANDLER -> RETURNERT_TIL_SAKSBEHANDLER
            medunderskriver?.saksbehandlerident != null -> MEDUNDERSKRIVER_VALGT
            tildeling?.saksbehandlerident != null -> TILDELT
            tildeling?.saksbehandlerident == null -> IKKE_TILDELT
            else -> UKJENT
        }
    }

    enum class Status {
        IKKE_TILDELT, TILDELT, MEDUNDERSKRIVER_VALGT, SENDT_TIL_MEDUNDERSKRIVER, RETURNERT_TIL_SAKSBEHANDLER, AVSLUTTET_AV_SAKSBEHANDLER, FULLFOERT, UKJENT
    }
}
