package no.nav.klage.oppgave.api.view

data class KvalitetsvurderingView(
    val grunn: String?,
    val eoes: String?,
    val raadfoertMedLege: String?,
    val internVurdering: String?,
    val sendTilbakemelding: Boolean?,
    val tilbakemelding: String?,
    val klagebehandlingVersjon: Long
)