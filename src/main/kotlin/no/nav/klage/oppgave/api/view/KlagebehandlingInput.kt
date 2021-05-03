package no.nav.klage.oppgave.api.view

import java.time.LocalDate


data class KlagebehandlingTypeInput(val type: String, val klagebehandlingVersjon: Long? = null)

data class KlagebehandlingTemaInput(val tema: String, val klagebehandlingVersjon: Long? = null)

data class KlagebehandlingInnsendtInput(val innsendt: LocalDate, val klagebehandlingVersjon: Long? = null)

data class KlagebehandlingMottattFoersteinstansInput(
    val mottattFoersteinstans: LocalDate,
    val klagebehandlingVersjon: Long? = null
)

data class KlagebehandlingMottattKlageinstansInput(
    val mottattKlageinstans: LocalDate,
    val klagebehandlingVersjon: Long? = null
)

data class KlagebehandlingFristInput(val frist: LocalDate, val klagebehandlingVersjon: Long? = null)

data class KlagebehandlingMedunderskriveridentInput(
    val medunderskriverident: String,
    val klagebehandlingVersjon: Long? = null
)

data class KlagebehandlingAvsenderSaksbehandleridentFoersteinstansInput(
    val avsenderSaksbehandlerident: String,
    val klagebehandlingVersjon: Long? = null
)

data class KlagebehandlingAvsenderEnhetFoersteinstansInput(
    val avsenderEnhet: String,
    val klagebehandlingVersjon: Long? = null
)

data class KlagebehandlingTildeltSaksbehandleridentInput(
    val tildeltSaksbehandlerident: String,
    val klagebehandlingVersjon: Long? = null
)

data class KlagebehandlingTildeltEnhetInput(val tildeltEnhet: String, val klagebehandlingVersjon: Long? = null)
