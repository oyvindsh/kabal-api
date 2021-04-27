package no.nav.klage.oppgave.domain.kafka

import no.nav.klage.oppgave.domain.kodeverk.Utfall

data class KlagevedtakFattet (
    val kildeReferanse: String,
    val kilde: String,
    val utfall: Utfall,
    val vedtaksbrevReferanse: String?,
    val kabalReferanse: String
)
