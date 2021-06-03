package no.nav.klage.oppgave.api.view

data class EditerbareFelterInput(
    val klagebehandlingVersjon: Long,
    val internVurdering: String?,
    val sendTilbakemelding: Boolean?,
    val tilbakemelding: String?,
    val utfall: String?,
    val grunn: String?,
    val hjemler: List<String>?,
    val dokumentReferanser: List<DokumentReferanse>,
    val tilknyttedeDokumenter: List<TilknyttetDokument>,
)

data class TilknyttetDokument(val journalpostId: String, val dokumentInfoId: String)
