package no.nav.klage.dokument.api.mapper

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import no.nav.klage.dokument.api.view.DokumentView
import no.nav.klage.dokument.api.view.SmartEditorDocumentView
import no.nav.klage.dokument.clients.kabalsmarteditorapi.model.response.DocumentOutput
import no.nav.klage.dokument.domain.MellomlagretDokument
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

    fun mapToByteArray(mellomlagretDokument: MellomlagretDokument): ResponseEntity<ByteArray> =
        ResponseEntity(
            mellomlagretDokument.content,
            HttpHeaders().apply {
                contentType = mellomlagretDokument.contentType
                add("Content-Disposition", "inline; filename=${mellomlagretDokument.title}")
            },
            HttpStatus.OK
        )

    fun mapToDokumentView(dokumentUnderArbeid: DokumentUnderArbeid): DokumentView {
        val type = getType(dokumentUnderArbeid)
        val tittel = if (type == DokumentView.DokumentUnderArbeidType.JOURNALFOERT) {
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
            type = type
        )
    }

    private fun getType(dokumentUnderArbeid: DokumentUnderArbeid): DokumentView.DokumentUnderArbeidType {
        return when {
            dokumentUnderArbeid.smartEditorId != null -> DokumentView.DokumentUnderArbeidType.SMART
            dokumentUnderArbeid.journalfoertDokumentReference != null -> DokumentView.DokumentUnderArbeidType.JOURNALFOERT
            else -> DokumentView.DokumentUnderArbeidType.UPLOADED
        }
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