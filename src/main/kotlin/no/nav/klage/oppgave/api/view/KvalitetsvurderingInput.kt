package no.nav.klage.oppgave.api.view

import no.nav.klage.oppgave.domain.kodeverk.Eoes
import no.nav.klage.oppgave.domain.kodeverk.Grunn
import no.nav.klage.oppgave.domain.kodeverk.RaadfoertMedLege

data class KvalitetsvurderingGrunnInput(val grunn: Grunn)

data class KvalitetsvurderingEoesInput(val eoes: Eoes)

data class KvalitetsvurderingRaadfoertMedLegeInput(val raadfoertMedLege: RaadfoertMedLege)

data class KvalitetsvurderingInternVurderingInput(val internVurdering: String)

data class KvalitetsvurderingSendTilbakemeldingInput(val sendTilbakemelding: Boolean)

data class KvalitetsvurderingTilbakemeldingInput(val tilbakemelding: String)