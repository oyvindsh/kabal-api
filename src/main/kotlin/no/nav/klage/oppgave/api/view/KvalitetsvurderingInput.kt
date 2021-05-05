package no.nav.klage.oppgave.api.view

import no.nav.klage.oppgave.domain.kodeverk.Eoes
import no.nav.klage.oppgave.domain.kodeverk.RaadfoertMedLege

data class KvalitetsvurderingEoesInput(
    val eoes: Eoes?,
    val klagebehandlingVersjon: Long
)

data class KvalitetsvurderingRaadfoertMedLegeInput(
    val raadfoertMedLege: RaadfoertMedLege?,
    val klagebehandlingVersjon: Long
)

data class KvalitetsvurderingInternVurderingInput(
    val internVurdering: String?,
    val klagebehandlingVersjon: Long
)

data class KvalitetsvurderingSendTilbakemeldingInput(
    val sendTilbakemelding: Boolean?,
    val klagebehandlingVersjon: Long
)

data class KvalitetsvurderingTilbakemeldingInput(
    val tilbakemelding: String?,
    val klagebehandlingVersjon: Long
)