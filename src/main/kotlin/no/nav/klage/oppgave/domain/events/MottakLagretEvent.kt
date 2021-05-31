package no.nav.klage.oppgave.domain.events

import no.nav.klage.oppgave.domain.klage.Mottak

data class MottakLagretEvent(
    val mottak: Mottak
)