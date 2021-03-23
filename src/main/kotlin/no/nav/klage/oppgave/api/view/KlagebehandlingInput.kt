package no.nav.klage.oppgave.api.view

import no.nav.klage.oppgave.domain.kodeverk.Sakstype
import no.nav.klage.oppgave.domain.kodeverk.Tema
import java.time.LocalDate


data class KlagebehandlingSakstypeInput(val sakstype: Sakstype)

data class KlagebehandlingTemaInput(val tema: Tema)

data class KlagebehandlingInnsendtInput(val innsendt: LocalDate)

data class KlagebehandlingMottattFoersteinstansInput(val mottattFoersteinstans: LocalDate)

data class KlagebehandlingMottattKlageinstansInput(val mottattKlageinstans: LocalDate)

data class KlagebehandlingFristInput(val frist: LocalDate)

data class KlagebehandlingAvsenderSaksbehandleridentFoersteinstansInput(val avsenderSaksbehandlerident: String)

data class KlagebehandlingAvsenderEnhetFoersteinstansInput(val avsenderEnhet: String)

data class KlagebehandlingTildeltSaksbehandleridentInput(val tildeltSaksbehandlerident: String)

data class KlagebehandlingTildeltEnhetInput(val tildeltEnhet: String)
