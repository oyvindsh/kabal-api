package no.nav.klage.oppgave.api.view

import no.nav.klage.oppgave.domain.klage.Mottak
import no.nav.klage.oppgave.domain.kodeverk.Kilde
import no.nav.klage.oppgave.domain.kodeverk.Sakstype
import no.nav.klage.oppgave.domain.kodeverk.Tema
import org.springframework.format.annotation.DateTimeFormat
import java.time.LocalDate
import java.util.*
import javax.validation.constraints.Past
import javax.validation.constraints.Pattern

//TODO: Validere hjemmel-format
//TODO: Validere alle String, at de faktisk er det man forventer (fødselsnummer, url, enheter, identer etc)
//TODO: Det er en del felter som må være nullable fra Oppgave, men som vi burde kunne kreve at er satt fra moderne løsninger. Hvordan løse det?
data class OversendtKlage(
    val uuid: UUID,
    val tema: Tema,
    val eksternReferanse: String,
    val innsynUrl: String,
    @field:Pattern(regexp = "\\d{11}", message = "Fødselsnummer er ugyldig")
    val foedselsnummer: String,
    val beskrivelse: String?,
    val avsenderSaksbehandlerIdent: String,
    val avsenderEnhet: String,
    val hjemler: List<String>,
    @field:Past(message = "Dato for mottatt førsteinstans må være i fortiden")
    @field:DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    val mottattFoersteinstans: LocalDate?,
    val innsendtTilNav: LocalDate?,
    val sakstype: Sakstype,
    val oversendtEnhet: String,
    val oversendelsesbrevJournalpostId: String?,
    val brukersKlageJournalpostId: String?,
    val frist: LocalDate?,
    val kilde: Kilde
) {

    //TODO: Orgnr/virksomhetsnr?
    //TODO: Sender de oss referanse til en behandling, eller til en sak? (Betyr det noe??)
    //TODO: Hvis sakstype er ANKE, trenger vi da referansen til klagen?
    //TODO: Trenger vi dokumentId også, ikke bare journalpostId?
    fun toMottak() = Mottak(
        id = uuid,
        tema = tema,
        sakstype = sakstype,
        referanseId = eksternReferanse,
        innsynUrl = innsynUrl,
        foedselsnummer = foedselsnummer,
        organisasjonsnummer = null,
        virksomhetsnummer = null,
        hjemmelListe = hjemler.joinToString(separator = ","),
        beskrivelse = beskrivelse,
        avsenderSaksbehandlerident = avsenderSaksbehandlerIdent,
        avsenderEnhet = avsenderEnhet,
        oversendtKaEnhet = oversendtEnhet,
        brukersKlageJournalpostId = brukersKlageJournalpostId,
        oversendelsesbrevJournalpostId = oversendelsesbrevJournalpostId,
        innsendtDato = innsendtTilNav,
        mottattNavDato = mottattFoersteinstans,
        oversendtKaDato = LocalDate.now(),
        fristFraFoersteinstans = frist,
        kilde = kilde
    )
}
