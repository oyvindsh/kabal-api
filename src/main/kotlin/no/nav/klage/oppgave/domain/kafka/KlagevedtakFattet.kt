package no.nav.klage.oppgave.domain.kafka

import no.nav.klage.oppgave.domain.kodeverk.Utfall

data class KlagevedtakFattet (
    val id: String,
    val utfall: Utfall,
    val vedtaksbrevReferanse: String
)
