package no.nav.klage.oppgave.api.view

import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

data class KlagebehandlingView(
    val id: UUID,
    val klageInnsendtdato: LocalDate?,
    val fraNAVEnhet: String?,
    val mottattFoersteinstans: LocalDate? = null,
    val foedselsnummer: String?,
    val tema: String,
    val sakstype: String,
    val mottatt: LocalDate?,
    val startet: LocalDate? = null,
    val avsluttet: LocalDate? = null,
    val frist: LocalDate? = null,
    val tildeltSaksbehandlerident: String? = null,
    val hjemler: List<HjemmelView>,
    val modified: LocalDateTime,
    val created: LocalDateTime,
    val fraSaksbehandlerident: String? = null,
    val klagebehandlingVersjon: Long
)

data class HjemmelView(
    val kapittel: Int? = null,
    val paragraf: Int? = null,
    val ledd: Int? = null,
    val bokstav: String? = null,
    val original: String
)