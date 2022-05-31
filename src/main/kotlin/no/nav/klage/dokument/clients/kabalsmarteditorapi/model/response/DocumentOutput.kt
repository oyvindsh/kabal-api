package no.nav.klage.dokument.clients.kabalsmarteditorapi.model.response

import java.time.LocalDateTime
import java.util.*

data class DocumentOutput(
    val id: UUID,
    val json: String?,
    val created: LocalDateTime,
    val modified: LocalDateTime,
)