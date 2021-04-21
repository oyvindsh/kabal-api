package no.nav.klage.oppgave.api.view

import no.nav.klage.oppgave.domain.klage.*
import no.nav.klage.oppgave.domain.kodeverk.Tema
import no.nav.klage.oppgave.domain.kodeverk.Type
import org.springframework.format.annotation.DateTimeFormat
import java.time.LocalDate
import java.util.*
import javax.validation.constraints.Past

data class OversendtKlage(
    val uuid: UUID,
    val tema: Tema,
    val sakstype: Type,
    val klager: OversendtKlager,
    val sakenGjelder: OversendtSakenGjelder? = null,
    val sakReferanse: String? = null,
    val kildeReferanse: String,
    val dvhReferanse: String? = null,
    val innsynUrl: String,
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
        id = uuid,
        tema = tema,
        sakstype = sakstype,
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
    val id: OversendtPartId,
    val klagersProsessfullmektig: OversendtProsessfullmektig? = null
) {
    fun toKlagepart() = Klager(
        partId = id.toPartId(),
        prosessfullmektig = klagersProsessfullmektig?.toProsessfullmektig()
    )
}

data class OversendtProsessfullmektig(
    val id: OversendtPartId,
    val skalKlagerMottaKopi: Boolean
) {
    fun toProsessfullmektig() = Prosessfullmektig(
        partId = id.toPartId(),
        skalKlagerMottaKopi = skalKlagerMottaKopi
    )
}

data class OversendtPartId(
    val type: PartIdType,
    val verdi: String
) {
    fun toPartId() = PartId(
        type = type,
        value = verdi
    )
}

data class OversendtDokumentReferanse(
    val type: MottakDokumentType,
    val journalpostId: String
) {
    fun toMottakDokument() = MottakDokument(
        type = type,
        journalpostId = journalpostId
    )
}
