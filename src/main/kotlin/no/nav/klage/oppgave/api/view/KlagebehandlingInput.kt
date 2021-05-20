package no.nav.klage.oppgave.api.view

import java.time.LocalDate
import java.time.LocalDateTime


data class KlagebehandlingTypeInput(
    val type: String,
    val klagebehandlingVersjon: Long
)

data class KlagebehandlingTemaInput(
    val tema: String,
    val klagebehandlingVersjon: Long
)

data class KlagebehandlingInnsendtInput(
    val innsendt: LocalDate,
    val klagebehandlingVersjon: Long
)

data class KlagebehandlingMottattFoersteinstansInput(
    val mottattFoersteinstans: LocalDate,
    val klagebehandlingVersjon: Long
)

data class KlagebehandlingMottattKlageinstansInput(
    val mottattKlageinstans: LocalDateTime,
    val klagebehandlingVersjon: Long
)

data class KlagebehandlingFristInput(
    val frist: LocalDate,
    val klagebehandlingVersjon: Long
)

data class KlagebehandlingMedunderskriveridentInput(
    val medunderskriverident: String,
    val klagebehandlingVersjon: Long
)

data class KlagebehandlingAvsenderSaksbehandleridentFoersteinstansInput(
    val avsenderSaksbehandlerident: String,
    val klagebehandlingVersjon: Long
)

data class KlagebehandlingAvsenderEnhetFoersteinstansInput(
    val avsenderEnhet: String,
    val klagebehandlingVersjon: Long
)

data class KlagebehandlingTildeltSaksbehandleridentInput(
    val tildeltSaksbehandlerident: String,
    val klagebehandlingVersjon: Long
)

data class KlagebehandlingTildeltEnhetInput(
    val tildeltEnhet: String,
    val klagebehandlingVersjon: Long
)
