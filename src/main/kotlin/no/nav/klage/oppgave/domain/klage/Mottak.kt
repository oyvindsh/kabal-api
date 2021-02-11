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
    @Column(name = "tema_id")
    @Convert(converter = TemaConverter::class)
    val tema: Tema,
    @Column(name = "sakstype_id")
    @Convert(converter = SakstypeConverter::class)
    val sakstype: Sakstype,
    @Column(name = "referanse_id")
    val referanseId: String? = null,
    @Column(name = "innsyn_url")
    val innsynUrl: String? = null,
    @Column(name = "foedselsnummer")
    val foedselsnummer: String? = null,
    @Column(name = "organisasjonsnummer")
    val organisasjonsnummer: String? = null,
    @Column(name = "virksomhetsnummer")
    val virksomhetsnummer: String? = null,
    @Column(name = "hjemmel_liste")
    val hjemmelListe: String? = null,
    @Column(name = "avsender_saksbehandlerident")
    val avsenderSaksbehandlerident: String? = null,
    @Column(name = "avsender_enhet")
    val avsenderEnhet: String? = null,
    @Column(name = "oversendt_klageinstans_enhet")
    val oversendtKaEnhet: String? = null,
    @Column(name = "dato_innsendt")
    val innsendtDato: LocalDate? = null,
    @Column(name = "dato_mottatt_foersteinstans")
    val mottattNavDato: LocalDate? = null,
    @Column(name = "dato_oversendt_klageinstans")
    val oversendtKaDato: LocalDate? = null,
    @Column(name = "dato_frist_fra_foersteinstans")
    val fristFraFoersteinstans: LocalDate? = null,
    @Column(name = "created")
    val created: LocalDateTime = LocalDateTime.now(),
    @Column(name = "modified")
    val modified: LocalDateTime = LocalDateTime.now(),
    @Column(name = "kilde")
    @Enumerated(EnumType.STRING)
    val kilde: Kilde
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
