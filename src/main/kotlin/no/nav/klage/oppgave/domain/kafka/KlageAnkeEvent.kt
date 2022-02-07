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