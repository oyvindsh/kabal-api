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
    val mottattFoersteinstans: LocalDate
) {
    fun toMottak() = Mottak(
        kilde = Kilde.OPPGAVE,
        oversendtKaDato = LocalDate.now(),
        sakstype = Sakstype.KLAGE,
        status = "",
        statusKategori = "",
        tema = tema
    )
}
