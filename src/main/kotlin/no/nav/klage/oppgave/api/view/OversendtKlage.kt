package no.nav.klage.oppgave.api.view

import no.nav.klage.oppgave.domain.klage.*
import no.nav.klage.oppgave.domain.kodeverk.Sakstype
import no.nav.klage.oppgave.domain.kodeverk.Tema
import no.nav.klage.oppgave.util.getLogger
import org.springframework.format.annotation.DateTimeFormat
import java.time.LocalDate
import java.util.*
import javax.validation.constraints.Past

data class OversendtKlage(
    val uuid: UUID,
    val tema: Tema,
    val sakstype: Sakstype,
    val klagerPartId: OversendtKlagerPartId,
    val sakReferanse: String? = null,
    val internReferanse: String,
    val dvhReferanse: String? = null,
    val innsynUrl: String,
    val hjemler: List<HjemmelFraFoersteInstans>,
    val avsenderSaksbehandlerIdent: String,
    val avsenderEnhet: String,
    val oversendtEnhet: String? = null,
    val tilknyttedeJournalposter: List<OversendtDokumentReferanse>,
    val brevmottakere: List<OversendtKlagerPartId>? = null,
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
        klagerPartId = klagerPartId.toPartId(),
        innsynUrl = innsynUrl,
        sakReferanse = sakReferanse,
        internReferanse = internReferanse,
        dvhReferanse = dvhReferanse,
        hjemmelListe = hjemler.joinToString(separator = ",") { it.toString() },
        avsenderSaksbehandlerident = avsenderSaksbehandlerIdent,
        avsenderEnhet = avsenderEnhet,
        oversendtKaEnhet = oversendtEnhet,
        mottakDokument = tilknyttedeJournalposter.map { it.toMottakDokument() }.toMutableSet(),
        brevmottakere = brevmottakere?.map { it.toPartId() }?.toMutableSet() ?: mutableSetOf(),
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

    companion object {

        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)

        fun fromString(string: String): HjemmelFraFoersteInstans {
            try {
                if (string.contains(" ")) {
                    val lov = string.split(" ").first()
                    val kapittelOgParagraf = string.split(" ").last()
                    if (kapittelOgParagraf.contains("-")) {
                        val kapittel = kapittelOgParagraf.split("-").first()
                        val paragraf = kapittelOgParagraf.split("-").last()
                        return HjemmelFraFoersteInstans(Lov.valueOf(lov), kapittel.toInt(), paragraf.toInt())
                    } else {
                        return HjemmelFraFoersteInstans(Lov.valueOf(lov), kapittelOgParagraf.toInt())
                    }
                } else {
                    return HjemmelFraFoersteInstans(Lov.valueOf(string))
                }
            } catch (e: Exception) {
                logger.error("Exception parsing HjemmelFraFoersteInstans from String {}", string, e)
                throw IllegalArgumentException("Exception parsing HjemmelFraFoersteInstans from $string")
            }
        }
    }
}

enum class Lov {
    FOLKETRYGDLOVEN, FORVALTNINGSLOVEN
}

data class OversendtKlagerPartId(
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
