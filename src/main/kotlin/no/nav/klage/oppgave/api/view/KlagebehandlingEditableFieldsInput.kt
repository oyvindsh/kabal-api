package no.nav.klage.oppgave.api.view

data class KlagebehandlingEditableFieldsInput(
    val klagebehandlingVersjon: Long,
    val internVurdering: String,
    //val sendTilbakemelding: Boolean?,
    //val tilbakemelding: String?,
    val utfall: String?,
    val grunn: String?,
    val hjemler: Set<String>?,
    val tilknyttedeDokumenter: Set<TilknyttetDokument>,
)

data class TilknyttetDokument(val journalpostId: String, val dokumentInfoId: String)
