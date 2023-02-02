package no.nav.klage.oppgave.api.view

data class UpdateDocumentTitleInput(
    val journalpostId: String,
    val dokumentInfoId: String,
    val newTitle: String
)