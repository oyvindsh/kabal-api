package no.nav.klage.oppgave.api.view

import no.nav.klage.oppgave.domain.klage.Mottak
import no.nav.klage.oppgave.domain.kodeverk.Kilde
import no.nav.klage.oppgave.domain.kodeverk.Sakstype
import no.nav.klage.oppgave.domain.kodeverk.Tema
import java.time.LocalDate
import java.time.LocalDateTime

data class InnsendtKlage(
    val uuid: String,
    val tema: Tema,
    val eksternReferanse: String,
    val innsynUrl: String,
    val fodselsnummer: String,
    val beskrivelse: String?,
    val avsenderSaksbehandlerIdent: String,
    val avsenderEnhet: String,
    val hjemler: List<String>,
    val mottattFoersteinstans: LocalDateTime
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
