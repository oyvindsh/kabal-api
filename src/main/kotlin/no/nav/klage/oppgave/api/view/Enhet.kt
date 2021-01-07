package no.nav.klage.oppgave.api.view

data class Enhet(
    val id: String,
    val navn: String,
    val lovligeYtelser: List<String>
)