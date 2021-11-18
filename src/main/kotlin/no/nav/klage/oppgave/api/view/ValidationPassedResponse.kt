package no.nav.klage.oppgave.api.view

import com.fasterxml.jackson.annotation.JsonProperty
import no.nav.klage.oppgave.exceptions.ValidationSection

/** Only used when validation passed */
data class ValidationPassedResponse(
    val title: String = "Validation success",
    val status: Int = 200,
    @JsonProperty("sections")
    val sections: List<ValidationSection> = emptyList()
)