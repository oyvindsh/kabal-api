package no.nav.klage.oppgave.api.view

data class KvalitetsvurderingEditableFieldsInput(
    val kvalitetOversendelsesbrevBra: Boolean?,
    val kvalitetsavvikOversendelsesbrev: Set<String> = emptySet(),
    val kommentarOversendelsesbrev: String?,
    val kvalitetUtredningBra: Boolean?,
    val kvalitetsavvikUtredning: Set<String> = emptySet(),
    val kommentarUtredning: String?,
    val kvalitetVedtakBra: Boolean?,
    val kvalitetsavvikVedtak: Set<String> = emptySet(),
    val kommentarVedtak: String?,
    val avvikStorKonsekvens: Boolean?,
    val brukSomEksempelIOpplaering: Boolean?
)
