package no.nav.klage.oppgave.api.view

import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty
import no.nav.klage.oppgave.domain.klage.*
import no.nav.klage.oppgave.domain.kodeverk.Tema
import no.nav.klage.oppgave.domain.kodeverk.Type
import org.springframework.format.annotation.DateTimeFormat
import java.time.LocalDate
import javax.validation.constraints.Past

@ApiModel
data class OversendtKlage(
    @ApiModelProperty(
        required = true,
        example = "OMS"
    )
    val tema: Tema,
    @ApiModelProperty(
        required = true,
        example = "KLAGE"
    )
    val type: Type,
    val klager: OversendtKlager,
    val sakenGjelder: OversendtSakenGjelder? = null,
    val sakReferanse: String? = null,
    val kildeReferanse: String,
    val dvhReferanse: String? = null,
    val innsynUrl: String?,
    val hjemler: List<HjemmelFraFoersteInstans>,
    val avsenderSaksbehandlerIdent: String,
    val avsenderEnhet: String,
    val oversendtEnhet: String? = null,
    val tilknyttedeJournalposter: List<OversendtDokumentReferanse>,
    @field:Past(message = "Dato for mottatt førsteinstans må være i fortiden")
    @field:DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    val mottattFoersteinstans: LocalDate,
    val innsendtTilNav: LocalDate,
    val frist: LocalDate? = null,
    val kilde: String
) {
    fun toMottak() = Mottak(
        tema = tema,
        type = type,
        klager = klager.toKlagepart(),
        sakenGjelder = sakenGjelder?.toSakenGjelder(),
        innsynUrl = innsynUrl,
        sakReferanse = sakReferanse,
        kildeReferanse = kildeReferanse,
        dvhReferanse = dvhReferanse,
        hjemmelListe = hjemler.map { it.toMottakHjemmel() }.toMutableSet(),
        avsenderSaksbehandlerident = avsenderSaksbehandlerIdent,
        avsenderEnhet = avsenderEnhet,
        oversendtKaEnhet = oversendtEnhet,
        mottakDokument = tilknyttedeJournalposter.map { it.toMottakDokument() }.toMutableSet(),
        innsendtDato = innsendtTilNav,
        mottattNavDato = mottattFoersteinstans,
        oversendtKaDato = LocalDate.now(),
        fristFraFoersteinstans = frist,
        kilde = kilde
    )
}

class HjemmelFraFoersteInstans private constructor(
    val kapittel: Int?,
    val paragraf: Int?,
    val lov: Lov,
) {
    constructor(lov: Lov) : this(null, null, lov)
    constructor(lov: Lov, kapittel: Int) : this(kapittel, null, lov)
    constructor(lov: Lov, kapittel: Int, paragraf: Int) : this(kapittel, paragraf, lov)

    override fun toString(): String {
        if (kapittel != null && paragraf != null) {
            return "$lov $kapittel-$paragraf"
        } else if (kapittel != null) {
            return "$lov $kapittel"
        } else {
            return "$lov"
        }
    }

    fun toMottakHjemmel() = MottakHjemmel(lov = lov, kapittel = kapittel, paragraf = paragraf)
}

enum class Lov {
    FOLKETRYGDLOVEN, FORVALTNINGSLOVEN
}

data class OversendtSakenGjelder(
    val id: OversendtPartId,
    val skalMottaKopi: Boolean
) {
    fun toSakenGjelder() = SakenGjelder(
        partId = id.toPartId(),
        skalMottaKopi = skalMottaKopi
    )
}

data class OversendtKlager(
    @ApiModelProperty(
        required = true
    )
    val id: OversendtPartId,
    @ApiModelProperty(
        name = "klagersProsessfullmektig",
        notes = "Kan settes dersom klager har en prosessfullmektig",
        required = false
    )
    val klagersProsessfullmektig: OversendtProsessfullmektig? = null
) {
    fun toKlagepart() = Klager(
        partId = id.toPartId(),
        prosessfullmektig = klagersProsessfullmektig?.toProsessfullmektig()
    )
}

data class OversendtProsessfullmektig(
    @ApiModelProperty(
        required = true
    )
    val id: OversendtPartId,
    @ApiModelProperty(
        required = true,
        example = "true"
    )
    val skalKlagerMottaKopi: Boolean
) {
    fun toProsessfullmektig() = Prosessfullmektig(
        partId = id.toPartId(),
        skalKlagerMottaKopi = skalKlagerMottaKopi
    )
}

data class OversendtPartId(
    @ApiModelProperty(
        required = true,
        example = "PERSON / VIRKSOMHET"
    )
    val type: PartIdType,
    @ApiModelProperty(
        required = true,
        example = "12345678910"
    )
    val verdi: String
) {
    fun toPartId() = PartId(
        type = type,
        value = verdi
    )
}

data class OversendtDokumentReferanse(
    @ApiModelProperty(
        required = true,
        example = "BRUKERS_KLAGE"
    )
    val type: MottakDokumentType,
    val journalpostId: String
) {
    fun toMottakDokument() = MottakDokument(
        type = type,
        journalpostId = journalpostId
    )
}
