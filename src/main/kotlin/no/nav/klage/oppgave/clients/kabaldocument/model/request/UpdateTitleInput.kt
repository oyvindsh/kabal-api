package no.nav.klage.oppgave.clients.kabaldocument.model.request

data class UpdateTitleInput(
    val journalpostId: String,
    val dokumentInfoId: String,
    val newTitle: String
)