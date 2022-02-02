package no.nav.klage.dokument.api.controller

data class BrevMottakerInput(
    val partId: PartIdInput,
    val navn: String?,
    val rolle: String,
)
