package no.nav.klage.oppgave.clients.kabaldocument.model.request

data class UpdateTitleInput(
    val dokumentInfoId: String,
    val title: String
)