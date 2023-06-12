package no.nav.klage.dokument.api.mapper

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import no.nav.klage.dokument.api.view.DokumentView
import no.nav.klage.dokument.api.view.DokumentViewWithList
import no.nav.klage.dokument.api.view.JournalfoertDokumentReference
import no.nav.klage.dokument.api.view.SmartEditorDocumentView
import no.nav.klage.dokument.clients.kabalsmarteditorapi.model.response.DocumentOutput
import no.nav.klage.dokument.domain.FysiskDokument
import no.nav.klage.dokument.domain.dokumenterunderarbeid.DokumentUnderArbeid
import no.nav.klage.oppgave.clients.saf.graphql.SafGraphQlClient
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Component

@Component
class DokumentMapper(
    private val safClient: SafGraphQlClient
) {

    fun mapToByteArray(fysiskDokument: FysiskDokument): ResponseEntity<ByteArray> =
        ResponseEntity(
            fysiskDokument.content,
            HttpHeaders().apply {
                contentType = fysiskDokument.contentType
                add("Content-Disposition", "inline; filename=${fysiskDokument.title}")
            },
            HttpStatus.OK
        )

    fun mapToDokumentView(dokumentUnderArbeid: DokumentUnderArbeid): DokumentView {
        val type = dokumentUnderArbeid.getType()
        val tittel = if (type == DokumentUnderArbeid.DokumentUnderArbeidType.JOURNALFOERT) {
            val journalpostInDokarkiv =
                safClient.getJournalpostAsSaksbehandler(dokumentUnderArbeid.journalfoertDokumentReference!!.journalpostId)

            val dokumentInDokarkiv =
                journalpostInDokarkiv.dokumenter?.find { it.dokumentInfoId == dokumentUnderArbeid.journalfoertDokumentReference.dokumentInfoId }
                    ?: throw RuntimeException("Document not found in Dokarkiv")

            dokumentInDokarkiv.tittel ?: "Tittel ikke funnet i SAF"
        } else dokumentUnderArbeid.name

        return DokumentView(
            id = dokumentUnderArbeid.id,
            tittel = tittel,
            dokumentTypeId = dokumentUnderArbeid.dokumentType?.id,
            created = dokumentUnderArbeid.created,
            opplastet = dokumentUnderArbeid.created,
            newOpplastet = dokumentUnderArbeid.opplastet,
            isSmartDokument = dokumentUnderArbeid.smartEditorId != null,
            templateId = dokumentUnderArbeid.smartEditorTemplateId,
            version = dokumentUnderArbeid.smartEditorVersion,
            isMarkertAvsluttet = dokumentUnderArbeid.markertFerdig != null,
            parent = dokumentUnderArbeid.parentId,
            parentId = dokumentUnderArbeid.parentId,
            type = type,
            journalfoertDokumentReference = dokumentUnderArbeid.journalfoertDokumentReference?.let {
                DokumentView.JournalfoertDokumentReference(
                    journalpostId = it.journalpostId,
                    dokumentInfoId = it.dokumentInfoId
                )
            }
        )
    }

    fun mapToDokumentListView(
        dokumentUnderArbeidList: List<DokumentUnderArbeid>,
        duplicateJournalfoerteDokumenter: List<JournalfoertDokumentReference>
    ): DokumentViewWithList {
        val firstDokumentView = mapToDokumentView(dokumentUnderArbeidList.first())

        return DokumentViewWithList(
            id = firstDokumentView.id,
            tittel = firstDokumentView.tittel,
            dokumentTypeId = firstDokumentView.dokumentTypeId,
            opplastet = firstDokumentView.opplastet,
            newOpplastet = firstDokumentView.newOpplastet,
            created = firstDokumentView.created,
            type = firstDokumentView.type,
            isSmartDokument = firstDokumentView.isSmartDokument,
            templateId = firstDokumentView.templateId,
            version = firstDokumentView.version,
            isMarkertAvsluttet = firstDokumentView.isMarkertAvsluttet,
            parent = firstDokumentView.parent,
            parentId = firstDokumentView.parentId,
            journalfoertDokumentReference = firstDokumentView.journalfoertDokumentReference,
            alteredDocuments = dokumentUnderArbeidList.map { mapToDokumentView(it) },
            duplicateJournalfoerteDokumenter = duplicateJournalfoerteDokumenter,
        )
    }

    fun mapToSmartEditorDocumentView(
        dokumentUnderArbeid: DokumentUnderArbeid,
        smartEditorDocument: DocumentOutput,
    ): SmartEditorDocumentView {
        return SmartEditorDocumentView(
            id = dokumentUnderArbeid.id,
            tittel = dokumentUnderArbeid.name,
            dokumentTypeId = dokumentUnderArbeid.dokumentType!!.id,
            templateId = dokumentUnderArbeid.smartEditorTemplateId,
            version = dokumentUnderArbeid.smartEditorVersion,
            parent = dokumentUnderArbeid.parentId,
            parentId = dokumentUnderArbeid.parentId,
            content = jacksonObjectMapper().readTree(smartEditorDocument.json),
            created = smartEditorDocument.created,
            modified = smartEditorDocument.modified,
        )
    }
}