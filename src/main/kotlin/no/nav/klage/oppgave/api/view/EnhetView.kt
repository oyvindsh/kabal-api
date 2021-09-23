package no.nav.klage.oppgave.api.view

data class EnhetView(
    val id: String,
    val navn: String,
    val lovligeTemaer: List<String>
)