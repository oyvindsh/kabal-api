package no.nav.klage.oppgave.clients.kaka.model.response

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.util.*

@JsonIgnoreProperties(ignoreUnknown = true)
data class KakaOutput(
    val id: UUID,
)

data class ValidationErrors(
    val validationErrors: List<InvalidProperty>
) {
    data class InvalidProperty(val field: String, val reason: String)
}