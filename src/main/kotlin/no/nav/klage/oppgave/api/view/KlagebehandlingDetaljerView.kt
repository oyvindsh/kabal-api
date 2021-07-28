package no.nav.klage.oppgave.api.view

import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

data class KlagebehandlingDetaljerView(
    val id: UUID,
    val klageInnsendtdato: LocalDate?,
    val fraNAVEnhet: String?,
    val fraNAVEnhetNavn: String?,
    val mottattFoersteinstans: LocalDate? = null,
    val sakenGjelderFoedselsnummer: String?,
    val sakenGjelderNavn: Navn?,
    val sakenGjelderKjoenn: String?,
    val sakenGjelderVirksomhetsnummer: String?,
    val sakenGjelderVirksomhetsnavn: String?,
    val klagerFoedselsnummer: String?,
    val klagerVirksomhetsnummer: String?,
    val klagerVirksomhetsnavn: String?,
    val klagerNavn: Navn?,
    val klagerKjoenn: String?,
    val tema: String,
    val type: String,
    val mottatt: LocalDate?,
    val mottattKlageinstans: LocalDate?,
    val tildelt: LocalDate? = null,
    val avsluttetAvSaksbehandler: LocalDate?,
    val frist: LocalDate? = null,
    val tildeltSaksbehandlerident: String? = null,
    val medunderskriverident: String? = null,
    val datoSendtMedunderskriver: LocalDate?,
    val hjemler: List<String>,
    val modified: LocalDateTime,
    val created: LocalDateTime,
    val fraSaksbehandlerident: String? = null,
    val eoes: String?,
    val raadfoertMedLege: String?,
    val internVurdering: String,
    val sendTilbakemelding: Boolean?,
    val tilbakemelding: String?,
    val klagebehandlingVersjon: Long,
    val vedtak: List<VedtakView>,
    val kommentarFraFoersteinstans: String?,
    val tilknyttedeDokumenter: Set<TilknyttetDokument>,
    val egenAnsatt: Boolean,
    val fortrolig: Boolean,
    val strengtFortrolig: Boolean
) {
    data class Navn(
        val fornavn: String?,
        val mellomnavn: String?,
        val etternavn: String?,
    )
}
