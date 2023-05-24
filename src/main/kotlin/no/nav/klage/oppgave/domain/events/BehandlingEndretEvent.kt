package no.nav.klage.oppgave.domain.events

import no.nav.klage.oppgave.domain.klage.Behandling
import no.nav.klage.oppgave.domain.klage.Endringslogginnslag

data class BehandlingEndretEvent(
    val behandling: Behandling,
    val endringslogginnslag: List<Endringslogginnslag>
)