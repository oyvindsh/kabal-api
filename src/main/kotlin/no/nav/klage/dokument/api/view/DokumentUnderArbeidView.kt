package no.nav.klage.dokument.api.view

import com.fasterxml.jackson.databind.JsonNode
import no.nav.klage.dokument.domain.dokumenterunderarbeid.DokumentUnderArbeid
import java.time.LocalDateTime
import java.util.*

data class JournalfoerteDokumenterResponse(
    val addedJournalfoerteDokumenter: List<DokumentView>,
    val failedJournalfoerteDokumenter: List<JournalfoertDokumentReference>,
)

data class DokumentView(
    val id: UUID,
    val tittel: String,
    val dokumentTypeId: String?,
    val opplastet: LocalDateTime,
    val newOpplastet: LocalDateTime?,
    val created: LocalDateTime,
    val type: DokumentUnderArbeid.DokumentUnderArbeidType,
    val isSmartDokument: Boolean,
    val templateId: String?,
    val version: Int?,
    val isMarkertAvsluttet: Boolean,
    //Deprecated
    val parent: UUID?,
    val parentId: UUID?,
    val journalfoertDokumentReference: JournalfoertDokumentReference?,
) {
    data class JournalfoertDokumentReference (
        val journalpostId: String,
        val dokumentInfoId: String,
    )
}

data class SmartEditorDocumentView(
    val id: UUID,
    val tittel: String,
    val content: JsonNode,
    val created: LocalDateTime,
    val modified: LocalDateTime,
    val version: Int?,
    val templateId: String?,
    val dokumentTypeId: String,
    //Deprecated
    val parent: UUID?,
    val parentId: UUID?,
)