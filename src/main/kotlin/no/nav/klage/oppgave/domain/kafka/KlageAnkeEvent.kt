package no.nav.klage.oppgave.domain.kafka

import java.util.*

data class KlageAnkeEvent(
    val eventId: UUID,
    val kildeReferanse: String,
    val kilde: String,
    val kabalReferanse: String,
    val detaljer: KlageAnkeEventDetaljer
)

data class KlageAnkeEventDetaljer(
    val behandlingAvsluttet: BehandlingAvsluttetDetaljer?,
)

data class BehandlingAvsluttetDetaljer(
    val utfall: ExternalUtfall,
    val journalpostReferanser: List<String>,
)