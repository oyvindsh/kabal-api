package no.nav.klage.oppgave.api.view

import com.fasterxml.jackson.annotation.JsonProperty
import no.nav.klage.oppgave.exceptions.InvalidProperty

/** Only used when validation passed */
data class ValidationPassedResponse(
    val title: String = "Validation success",
    val status: Int = 200,
    @JsonProperty("invalid-properties")
    val invalidProperties: List<InvalidProperty> = emptyList()
)