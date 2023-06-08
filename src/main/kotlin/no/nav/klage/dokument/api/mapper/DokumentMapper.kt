package no.nav.klage.dokument.api.mapper

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import no.nav.klage.dokument.api.view.DokumentView
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
            opplastet = dokumentUnderArbeid.opplastet,
            isSmartDokument = dokumentUnderArbeid.smartEditorId != null,
            templateId = dokumentUnderArbeid.smartEditorTemplateId,
            version = dokumentUnderArbeid.smartEditorVersion,
            isMarkertAvsluttet = dokumentUnderArbeid.markertFerdig != null,
            parent = dokumentUnderArbeid.parentId,
            type = type,
            journalfoertDokumentReference = dokumentUnderArbeid.journalfoertDokumentReference?.let {
                DokumentView.JournalfoertDokumentReference(
                    journalpostId = it.journalpostId,
                    dokumentInfoId = it.dokumentInfoId
                )
            }
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
            content = jacksonObjectMapper().readTree(smartEditorDocument.json),
            created = smartEditorDocument.created,
            modified = smartEditorDocument.modified,
        )
    }
}