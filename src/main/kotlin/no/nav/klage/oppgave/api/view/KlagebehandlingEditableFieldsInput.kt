package no.nav.klage.oppgave.api.view

data class KlagebehandlingEditableFieldsInput(
    val klagebehandlingVersjon: Long,
    val utfall: String?,
    val hjemler: Set<String>?,
    val tilknyttedeDokumenter: Set<TilknyttetDokument>,
)

data class TilknyttetDokument(val journalpostId: String, val dokumentInfoId: String)
