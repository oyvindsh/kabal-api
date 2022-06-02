package no.nav.klage.dokument.api.view

import com.fasterxml.jackson.databind.JsonNode
import java.time.LocalDateTime
import java.util.*

data class DokumentView(
    val id: UUID,
    val tittel: String,
    val dokumentTypeId: String,
    val opplastet: LocalDateTime,
    val isSmartDokument: Boolean,
    val templateId: String?,
    val version: Int?,
    val isMarkertAvsluttet: Boolean,
    val parent: UUID?,
)

data class SmartEditorDocumentView(
    val id: UUID,
    val tittel: String,
    val content: JsonNode,
    val created: LocalDateTime,
    val modified: LocalDateTime,
    val version: Int?,
    val templateId: String?,
    val dokumentTypeId: String,
    val parent: UUID?,
)