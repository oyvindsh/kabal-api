package no.nav.klage.oppgave.clients.kabaldocument.model.response

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
class BrevmottakerOutput(
    val partId: PartIdOutput,
    val navn: String,
)