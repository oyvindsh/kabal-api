package no.nav.klage.oppgave.api.view

import no.nav.klage.oppgave.domain.klage.*
import no.nav.klage.oppgave.domain.kodeverk.Sakstype
import no.nav.klage.oppgave.domain.kodeverk.Tema
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
    val hjemler: List<String>,
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
        hjemmelListe = hjemler.joinToString(separator = ","),
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
