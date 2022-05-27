package no.nav.klage.dokument.clients.kabalsmarteditorapi.model.response

import com.fasterxml.jackson.databind.JsonNode
import java.time.LocalDateTime
import java.util.*

data class DocumentOutput(
    val id: UUID,
    val json: String?,
    var content: JsonNode?,
    var templateId: String?,
    var version: Int?,
    val created: LocalDateTime,
    val modified: LocalDateTime
)