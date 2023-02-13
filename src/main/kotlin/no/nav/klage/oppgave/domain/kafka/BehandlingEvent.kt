package no.nav.klage.oppgave.domain.kafka

import java.time.LocalDateTime
import java.util.*

data class BehandlingEvent(
    val eventId: UUID,
    val kildeReferanse: String,
    val kilde: String,
    val kabalReferanse: String,
    val type: BehandlingEventType,
    val detaljer: BehandlingDetaljer,
)

enum class BehandlingEventType {
    KLAGEBEHANDLING_AVSLUTTET, ANKEBEHANDLING_OPPRETTET, ANKEBEHANDLING_AVSLUTTET, ANKE_I_TRYGDERETTENBEHANDLING_OPPRETTET
}

data class BehandlingDetaljer(
    val klagebehandlingAvsluttet: KlagebehandlingAvsluttetDetaljer? = null,
    val ankebehandlingOpprettet: AnkebehandlingOpprettetDetaljer? = null,
    val ankebehandlingAvsluttet: AnkebehandlingAvsluttetDetaljer? = null,
    val ankeITrygderettenbehandlingOpprettet: AnkeITrygderettenbehandlingOpprettetDetaljer? = null,
)

data class KlagebehandlingAvsluttetDetaljer(
    val avsluttet: LocalDateTime,
    val utfall: ExternalUtfall,
    val journalpostReferanser: List<String>,
)


data class AnkebehandlingOpprettetDetaljer(
    val mottattKlageinstans: LocalDateTime
)

data class AnkeITrygderettenbehandlingOpprettetDetaljer(
    val sendtTilTrygderetten: LocalDateTime,
    val utfall: ExternalUtfall?,
)

data class AnkebehandlingAvsluttetDetaljer(
    val avsluttet: LocalDateTime,
    val utfall: ExternalUtfall,
    val journalpostReferanser: List<String>,
)
