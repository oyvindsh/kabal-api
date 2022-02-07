package no.nav.klage.oppgave.domain.kafka

import java.util.*

data class KlageAnkeEvent(
    val eventId: UUID,
    val kildeReferanse: String,
    val kilde: String,
    val kabalReferanse: String,
    val type: KlageAnkeEventType,
    val detaljer: BehandlingDetaljer,
)

enum class KlageAnkeEventType {
    KLAGEBEHANDLING_AVSLUTTET, ANKEBEHANDLING_AVSLUTTET
}

data class BehandlingDetaljer(
    val klagebehandlingAvsluttet: KlagebehandlingAvsluttetDetaljer? = null,
    val ankebehandlingAvsluttet: AnkebehandlingAvsluttetDetaljer? = null,
)

data class KlagebehandlingAvsluttetDetaljer(
    val utfall: ExternalUtfall,
    val journalpostReferanser: List<String>,
)

data class AnkebehandlingAvsluttetDetaljer(
    val utfall: ExternalUtfall,
    val journalpostReferanser: List<String>,
)