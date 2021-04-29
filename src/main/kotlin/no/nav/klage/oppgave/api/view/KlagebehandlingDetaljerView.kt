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
    // TODO Legge til virksomhetsnavn (mangler ereg integrasjon)
    val foedselsnummer: String?,
    val virksomhetsnummer: String?,
    val tema: String,
    val type: String,
    val mottatt: LocalDate?,
    val startet: LocalDate? = null,
    val avsluttet: LocalDate? = null,
    val frist: LocalDate? = null,
    val tildeltSaksbehandlerident: String? = null,
    val medunderskriverident: String? = null,
    val hjemler: List<String>,
    val modified: LocalDateTime,
    val created: LocalDateTime,
    val fraSaksbehandlerident: String? = null,
    val grunn: String?,
    val eoes: String?,
    val raadfoertMedLege: String?,
    val internVurdering: String?,
    val sendTilbakemelding: Boolean?,
    val tilbakemelding: String?,
    val klagebehandlingVersjon: Long,
    val vedtak: List<VedtakView>,
    val kommentarFraFoersteinstans: String?
) {
    data class Navn(
        val fornavn: String?,
        val mellomnavn: String?,
        val etternavn: String?,
    )
}
