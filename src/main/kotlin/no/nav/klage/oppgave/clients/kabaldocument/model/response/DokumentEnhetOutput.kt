package no.nav.klage.oppgave.clients.kabaldocument.model.response

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class DokumentEnhetOutput(
    val id: String,
)
