package no.nav.klage.oppgave.api.view

import no.nav.klage.oppgave.domain.kodeverk.Eoes
import no.nav.klage.oppgave.domain.kodeverk.Grunn
import no.nav.klage.oppgave.domain.kodeverk.RaadfoertMedLege

data class KvalitetsvurderingGrunnInput(val grunn: Grunn?, val klagebehandlingVersjon: Long? = null)

data class KvalitetsvurderingEoesInput(val eoes: Eoes?, val klagebehandlingVersjon: Long? = null)

data class KvalitetsvurderingRaadfoertMedLegeInput(
    val raadfoertMedLege: RaadfoertMedLege?,
    val klagebehandlingVersjon: Long? = null
)

data class KvalitetsvurderingInternVurderingInput(
    val internVurdering: String?,
    val klagebehandlingVersjon: Long? = null
)

data class KvalitetsvurderingSendTilbakemeldingInput(
    val sendTilbakemelding: Boolean?,
    val klagebehandlingVersjon: Long? = null
)

data class KvalitetsvurderingTilbakemeldingInput(val tilbakemelding: String?, val klagebehandlingVersjon: Long? = null)