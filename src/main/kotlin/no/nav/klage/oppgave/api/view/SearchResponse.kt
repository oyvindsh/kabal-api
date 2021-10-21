package no.nav.klage.oppgave.api.view

/**
 * When searching for a specific fnr
 */
data class FnrSearchResponse(
    val fnr: String,
    val name: String,
    val klagebehandlinger: List<KlagebehandlingListView> = emptyList(),
    val aapneKlagebehandlinger: List<KlagebehandlingListView> = emptyList(),
    val avsluttedeKlagebehandlinger: List<KlagebehandlingListView> = emptyList()
)

/**
 * Used for name search
 */
data class PersonView(
    val fnr: String,
    val name: String
)